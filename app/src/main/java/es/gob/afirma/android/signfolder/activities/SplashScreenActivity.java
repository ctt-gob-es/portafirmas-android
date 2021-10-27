package es.gob.afirma.android.signfolder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;

/**
 * Pantalla de splash.
 */
public class SplashScreenActivity extends AppCompatActivity {

    /** Tiempo que se mostrara el splash. */
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Para pruebas del splash y asistente
        //AppPreferences.getInstance().setFirstExecution(true);


        // En la primera ejecucion de la aplicacion mostraremos la pantalla de splash y el
        // asistente.
        if (AppPreferences.getInstance().isFirstExecution()) {

            // Ocultamos la barra de navegacion y la barra de estado
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            // Establecemos el layout
            setContentView(R.layout.activity_splash_screen);

            // Una vez se complete la espera del splash, se ejecutaran la siguiente accion
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Lanzamos el wizard
                    Intent i = new Intent(SplashScreenActivity.this, WizardActivity.class);
                    startActivity(i);
                    // Cerramos la actividad
                    finish();
                }
            }, SPLASH_DELAY);
        }
        // En las siguientes ejecuciones se mostrara directamente la pantalla principal
        else {
            // Lanzamos la actividad de la pantalla principal
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            // Cerramos esta actividad
            finish();
        }
    }
}
