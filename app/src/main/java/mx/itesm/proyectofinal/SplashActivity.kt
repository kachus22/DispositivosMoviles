package mx.itesm.proyectofinal

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Hiding Tittle project */
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        /**
         * Delayed splashscreen
         */
        Handler().postDelayed({
            lateinit var startAppIntent:Intent
            val sharedPreference:SharedPreference=SharedPreference(this)
            if(sharedPreference.getValueString("TIPO_USUARIO") == "clinica"){
                Toast.makeText(this,"Ya se tiene una clinica iniciada", Toast.LENGTH_LONG)
                startAppIntent = Intent(this@SplashActivity,Clinic_list::class.java)
                startAppIntent.putExtra(PatientList.ACCOUNT, sharedPreference.getValueProfile("ACCOUNT"))
                startActivity(startAppIntent)
                PatientList.ACTIV = "sign"
            }else{
                if(sharedPreference.getValueString("TIPO_USUARIO") == "paciente"){
                    Toast.makeText(this,"Ya se tiene un paciente iniciada", Toast.LENGTH_LONG)
                    startAppIntent = Intent(this@SplashActivity,PatientList::class.java)
                    startAppIntent.putExtra(PatientList.ACCOUNT, sharedPreference.getValueProfile("ACCOUNT"))
                    startActivity(startAppIntent)
                }else{
                    Toast.makeText(this,"No se tiene nada iniciado", Toast.LENGTH_LONG)
                    startActivity(Intent(this@SplashActivity, ElegirTipo::class.java))
                }
            }
            finish()
        },1000)
    }
}