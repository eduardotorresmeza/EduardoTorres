package icesi.movil.astrapp.astrapp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by JUAN CAMILO on 15/05/2015.
 */
public class ComunicacionTCP extends Thread{
    private Socket s;
    private int puerto=5100;
    private String mIp="192.168.1.104";
    static ComunicacionTCP ref;
    private ComunicacionTCP(){
        start();
    }
    public static ComunicacionTCP geComunicacion(){
        if(ref==null){
            ref= new ComunicacionTCP();
        }
        return ref;
    }
    public void run(){
        while (true){
            if(s==null){
                try{
                    s= new Socket(InetAddress.getByName(mIp),puerto);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else{
                try{
                    sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void enviarPos(String mensaje){
        DataOutputStream salida=null;
        try{
            salida= new DataOutputStream(s.getOutputStream());
            salida.writeUTF(mensaje);
            salida.flush();
            System.out.print("envie :"+mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
