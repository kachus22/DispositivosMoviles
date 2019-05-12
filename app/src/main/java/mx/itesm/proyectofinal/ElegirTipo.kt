package mx.itesm.proyectofinal

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_elegir_tipo.*

class ElegirTipo : AppCompatActivity() {

    companion object {
        val TYPE:String = "tipo"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegir_tipo)
        button_paciente.setOnClickListener { signInPaciente() }
        button_clinica.setOnClickListener { signInClinica() }
    }

    fun signInClinica(){
        val StartAppIntent = Intent(this,signInActivity::class.java)
        StartAppIntent.putExtra(TYPE,"clinica")
        startActivity(StartAppIntent)
    }

    fun signInPaciente(){
        val StartAppIntent = Intent(this,signInActivity::class.java)
        StartAppIntent.putExtra(TYPE,"paciente")
        startActivity(StartAppIntent)
    }

    override fun onBackPressed() {
        // Do Here what ever you want do on back press;
        finish()
    }
}
