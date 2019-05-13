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

    /**
     * Companion object that tells the type of account of the user
     */
    companion object {
        val TYPE:String = "tipo"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegir_tipo)
        button_paciente.setOnClickListener { signInPaciente() }
        button_clinica.setOnClickListener { signInClinica() }
    }

    /**
     * Every time the activity starts
     */
    override fun onStart() {
        super.onStart()
        button_paciente.setOnClickListener { signInPaciente() }
        button_clinica.setOnClickListener { signInClinica() }
    }

    /**
     * Function executed when the user chooses the option of sign in as a clinic/doctor, putting in
     * the intent the extra of clinic selected as TYPE
     */
    fun signInClinica(){
        val StartAppIntent = Intent(this,signInActivity::class.java)
        StartAppIntent.putExtra(TYPE,"clinica")
        startActivity(StartAppIntent)
    }

    /**
     * Function executed when the user chooses the option of sign in as a patient, putting in
     * the intent the extra of patient selected as TYPE
     */
    fun signInPaciente(){
        val StartAppIntent = Intent(this,signInActivity::class.java)
        StartAppIntent.putExtra(TYPE,"paciente")
        startActivity(StartAppIntent)
    }

    /**
     * Function that handles the back pressed event on the device, finishing the current activity
     */
    override fun onBackPressed() {
        // Do Here what ever you want do on back press;
        finish()
    }
}
