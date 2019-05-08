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
import kotlinx.android.synthetic.main.activity_elegir_tipo.*

class ElegirTipo : AppCompatActivity() {

    var nombre : String = ""
    var mail : String = ""
    var photo : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegir_tipo)

        val extras = intent.extras?: return

        nombre = extras.getString(PatientList.ACCOUNT_NAME)
        mail   = extras.getString(PatientList.ACCOUNT_MAIL)
        photo  = extras.getString(PatientList.ACCOUNT_IMG)

        button_paciente.setOnClickListener { irPaciente() }
        button_clinica.setOnClickListener { irClinica() }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tipo, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, ConfigurationActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                val builder = AlertDialog.Builder(this@ElegirTipo)

                builder.setTitle("Cerrar sesión")

                builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")

                builder.setPositiveButton("Cerrar sesión") { dialog, which ->
                    signOut()
                }

                // Display a negative button on alert dialog
                builder.setNegativeButton("Cancelar") { dialog, which ->
                }

                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()
                true
            }
            else -> {
                false
            }
        }
    }
    fun irClinica(){
        val StartAppIntent = Intent(this,Clinic_list::class.java)
        StartAppIntent.putExtra(PatientList.ACCOUNT_MAIL,mail)
        StartAppIntent.putExtra(PatientList.ACCOUNT_NAME,nombre)
        StartAppIntent.putExtra(PatientList.ACCOUNT_IMG,photo)
        startActivity(StartAppIntent)
    }

    fun irPaciente(){
        val StartAppIntent = Intent(this,PatientList::class.java)
        StartAppIntent.putExtra(PatientList.ACCOUNT_MAIL,mail)
        StartAppIntent.putExtra(PatientList.ACCOUNT_NAME,nombre)
        StartAppIntent.putExtra(PatientList.ACCOUNT_IMG,photo)
        startActivity(StartAppIntent)
    }
    private fun signOut() {
        Toast.makeText(applicationContext,"Cerrar sesión.", Toast.LENGTH_SHORT).show()
        //finish()
        PatientList.STATUS = "si"
        finish()
    }

    override fun onBackPressed() {
        // Do Here what ever you want do on back press;
    }
}
