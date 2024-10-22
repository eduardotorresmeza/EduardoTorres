package icesi.movil.astrapp.astrapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class Inicio extends Activity {
    private ImageButton iniciar;
    private ImageView logo;
    private RelativeLayout fondo;
    AnimationDrawable animacionLogo;
    AnimationDrawable animacionFondo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        iniciar= (ImageButton) findViewById(R.id.btn_inicio);
        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siguiente();
            }
        });
        logo= (ImageView) findViewById(R.id.logoAnimado);
       // logo.setBackgroundResource(R.drawable.logo_animado);
        //animacionLogo = (AnimationDrawable) logo.getBackground();

        //setear animacion del fondo
        //fondo = (RelativeLayout) findViewById(R.id.fondo_inicio);
        //fondo.setBackgroundResource(R.drawable.fondo_inicio_animacion);
        //animacionFondo = (AnimationDrawable) fondo.getBackground();
       // animacionLogo = (AnimationDrawable) logo.getBackground();

    }


    public void onStart(){
        super.onStart();
        //setear animacion del logo

        //animacionLogo.start();
        //animacionFondo.start();
    }

    public void siguiente(){
        Intent irCreacion = new Intent(getApplicationContext(),ActivityCrear.class);
        startActivity(irCreacion);
    }

}
