package icesi.movil.astrapp.astrapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Point;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class ActivityCrear extends Activity {
    private View barraColores;
    private View barraHerramientas;

    private ImageButton btnColorAmarillo;
    private ImageButton btnColorNaranja;
    private ImageButton btnColorRojo;
    private ImageButton btnColorVerde;
    private ImageButton btnColorAzul;
    private ImageButton btnColorVioleta;
    private ImageButton btnColorGris;

    
    private ImageButton btnPincel;
    private ImageButton btnFinalizar;
    private ImageButton btnRefrescar;
    private ImageButton btnAyuda;


    /////////----CONEXION BLUETOOTH----//////////////////////////
    boolean conectado = false;
    private TextView estado;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private String macBluetooth = "00:06:66:4D:72:1A";
    ///////////////////////////////////////////////////////////
    private ComunicacionTCP comTCP;
    private int contEnvio;

    @Override
    protected     void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_crear);
        botonesColores(); //inicializo los botones
        botonesHerramientas();
        barraHerramientas= findViewById(R.id.barra_herramientas);
        barraColores= findViewById(R.id.barra_de_colores);
       // barraHerramientas.setTranslationX(-100f);
        comTCP=null;
        comTCP= ComunicacionTCP.geComunicacion();
      // comTCP.start();

        ////--------------BLUETOOTH------------------///
        setUI();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            estado.setText("Bluetooth no disponible");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            estado.setText("Encendiendo Bluetooth");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        Connect();
        ///////////////////////////////////////////////////

    }
    ///********///****UUUUDDDDPPPPP****///*****////
    //asynctask encargada de la comunicacion
  /*  public class EnviarTarea extends AsyncTask<String, Void, Boolean> {
        boolean resultado = false;
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                DatagramSocket ds = new DatagramSocket(5236); //172.30.187.17
                byte[] buf = params[0].getBytes();
                DatagramPacket pq = new DatagramPacket(buf, buf.length, InetAddress.getByName("172.30.175.109"), 5235);
                ds.send(pq);
                System.out.print(params[0]);
            } catch (UnknownHostException uhe) {
                //  println(uhe);
            } catch (IOException io) {
                //   println(io);
            }
            return resultado;
        }
    }*/
    ///-------------------BLUETOOTH--------------------------------///
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Configura la interfas de usuario
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setUI() {
        estado = (TextView)findViewById(R.id.estado);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Verifica que exista bluetooth, que este habilitado y que el dispositivo que se va a agreagr este vinculado
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    void findBT() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mmDevice = mBluetoothAdapter.getRemoteDevice(macBluetooth);
        if (pairedDevices.contains(mmDevice)) {
            estado.setText("El dispositivo " + mmDevice.getName() + " ya esta vinculado.");
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Abre un socket de comunicacion para conectarse al modulo bluetooth
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    void conectarBT() throws IOException {
        if (!conectado) {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard //SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            enEsperaDeInstrucciones();
            estado.setText("Conectado a: " + mmDevice.getName());
            conectado = true;

            Log.println(1,"conexion","se conecto exitosamente"); //revisar en logcat, print para debugear
        }
    }
    void enEsperaDeInstrucciones() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            estado.setText(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }
    void Connect() {
        try {
            mostrarMensaje("Conectando....");
            findBT();
            conectarBT();
            if (conectado)
                mostrarMensaje("Conectado a Lufter");
        } catch (IOException ex) {
        }
    }
    void desconectarBT() throws IOException {
        if (conectado) {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            conectado = false;
            estado.setText("Conexion Terminada");
        }
    }
    void enviarDatos(String mensaje) {
        try {
            if (conectado)
                mmOutputStream.write(mensaje.getBytes());
        } catch (IOException e) {
            // 	TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void mostrarMensaje(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, Toast.LENGTH_SHORT);
        msg.show();
    }
    ///////////////////////////////////////////////
    void enviarPos(int mensaje) {
        try {
            if (conectado)
                mmOutputStream.write(mensaje);
        } catch (IOException e) {
            // 	TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onResume(){
        super.onResume();

    }
    public boolean onTouchEvent(MotionEvent e) {
        int x = (int)e.getX();
        int y = (int)e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                    comTCP.enviarPos("iniciar");
                    comTCP.enviarPos("posiciones");
                    comTCP.enviarPos(+x + "," + y + ",");
                    contEnvio=0;
                //envio "i" para indicar que comience a dibujar
             //   enviarDatos("i");
               /* enviarDatos("x");
                String temp = 385+"";
                char[] tempChar= temp.toCharArray();
                //for(int i =0; i<tempChar.length;i++) {
                int a = temp.charAt(0);
                int b = temp.charAt(1);
                int c = temp.charAt(2);
                    enviarPos(a);
                    enviarPos(b);
                    enviarPos(c);
                */
                System.out.println("inicio tap");
                break;
            case MotionEvent.ACTION_UP:
                comTCP.enviarPos("terminar");
                //envio "d" para indicar que termine de dibujar
                //enviarDatos("d");

                //System.out.println("termino tap");
                break;
            case MotionEvent.ACTION_MOVE:
                //enviar por bluetooth variables x  y
                contEnvio++;
                if(contEnvio>=8) {
                    comTCP.enviarPos("posiciones");
                    comTCP.enviarPos(+x + "," + y + ",");
                    System.out.println("envie" + x + "," + y);
                    contEnvio=0;
                }
              /*  EnviarTarea t = (EnviarTarea) new EnviarTarea().execute("pos,"+x+","+y);
                try {
                    boolean d =  t.get();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }*/
               /* enviarDatos("x");
                String temp = x+"";
                char[] tempChar= temp.toCharArray();
                for(int i =0; i<tempChar.length;i++) {
                    enviarPos(tempChar[i]);
                }*/
                break;
        }
        return true;
    }

    //----------------BARRA DE HERRAMIENTAS----------------------//
    public void botonesHerramientas(){
        btnPincel= (ImageButton) findViewById(R.id.pincel);
        btnRefrescar= (ImageButton) findViewById(R.id.refrescar);
        btnFinalizar= (ImageButton) findViewById(R.id.finalizar);
        btnAyuda= (ImageButton) findViewById(R.id.ayuda);

        btnPincel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarDatos("p");
                System.out.println("envie pincel");
            }
        });
        btnRefrescar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarDatos("q");
                System.out.println("envie refrescar");
            }
        });
        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cambio de pantalla
                System.out.println("envie finalizar");
            }
        });
        btnAyuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("envie ayuda");
            }
        });

    }

    //-----------------BARRA DE BOTONES SELECCION DE COLOR----------------//
    public void botonesColores(){
        btnColorAmarillo= (ImageButton) findViewById(R.id.btn_paleta_amarillo);
        btnColorNaranja= (ImageButton) findViewById(R.id.btn_paleta_naranja);
        btnColorRojo= (ImageButton) findViewById(R.id.btn_paleta_rojo);
        btnColorVerde= (ImageButton) findViewById(R.id.btn_paleta_verde);
        btnColorAzul= (ImageButton) findViewById(R.id.btn_paleta_azul);
        btnColorVioleta= (ImageButton) findViewById(R.id.btn_paleta_violeta);
        btnColorGris= (ImageButton) findViewById(R.id.btn_paleta_gris);

        btnColorAmarillo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //enviar amarillo

                enviarDatos("a");
                System.out.println("envie amarillo");
            }
        });

        btnColorNaranja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar naranja

                enviarDatos("n");

                System.out.println("envie naranja");
            }
        });

        btnColorRojo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar rojo

                enviarDatos("r");
                System.out.println("envie rojo");
            }
        });

        btnColorVerde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar verde

                enviarDatos("v");
                System.out.println("envie verde");
            }
        });

        btnColorAzul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar azul
                enviarDatos("b");
                System.out.println("envie azul");

            }
        });

        btnColorVioleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar violeta
                enviarDatos("m");
                System.out.println("envie violeta");
            }
        });

        btnColorGris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enviar gris
                enviarDatos("g");
                System.out.println("envie gris");
            }
        });
    }
}
