package mx.itesm.proyectofinal

import Database.Medicion
import Database.MedicionDatabase
import Database.Patient
import NetworkUtility.OkHttpRequest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_clinic_list.*
import mx.itesm.proyectofinal.R.id.action_logout
import mx.itesm.proyectofinal.Utils.CustomItemClickListener2
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Clinic_list : AppCompatActivity(), CustomItemClickListener2 {

    companion object {
        val ACCOUNT_MAIL:String = "account_mail"
        val ACCOUNT_NAME:String = "account_name"
        val ACCOUNT_IMG:String = "account_img"
    }

    var patients : MutableList<Patient> = mutableListOf()

    // Database variable initialization
    lateinit var instanceDatabase: MedicionDatabase
    lateinit var profile: Profile

    // The RecyclerView adapter declaration
    val adapter = PatientAdapter(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_list)
        val extras = intent.extras?: return
        profile = extras.getParcelable(PatientList.ACCOUNT)!!

        textView_nombre.text = "Clinica/Doctor: ${profile.name}"

        val layoutManager = LinearLayoutManager(this)
        lista_clinica.layoutManager = layoutManager

        this.instanceDatabase = MedicionDatabase.getInstance(this)

        lista_clinica.adapter = adapter

        loadPacientes()

    }


    //Menu con la opcion de escanear el qr del paciente
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_clinic, menu)
        return true
    }

    // Loads measurements from database
    private fun loadPacientes() {
        var client = OkHttpClient()
        var request= OkHttpRequest(client)
        val url = "https://heart-app-tec.herokuapp.com/clinics/"+ profile.mail
        request.GET(url, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                println(response.toString())
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        val t = parseJsonPats(responseData, profile.mail)
                        adapter.setPatient(t!!)
                        if (adapter.itemCount == 0) {
                            tv_vacia.visibility = View.VISIBLE
                        } else {
                            tv_vacia.visibility = View.GONE
                        }
                        lista_clinica.adapter = adapter
                        lista_clinica.adapter?.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("FAILURE", "REQUEST FAILURE")
            }
        })
    }
    fun parseJsonPats(jsonString: String?, clinicPat : String): MutableList<Patient>{
        //var patients : MutableList<Patient> = mutableListOf()
        var pat : Patient
        //Primero es array
        try {
            val dataJsonList : JSONArray = JSONArray(jsonString)
            for(i in 0 until dataJsonList.length()){
                val jsonPat : JSONObject = dataJsonList.getJSONObject(i)
                val patMail = jsonPat.getString("email")
                val name = jsonPat.getString("name")
                val patAge = jsonPat.getInt("age")
                val patSex = jsonPat.getString("sex")

                pat = Patient(patMail,name,"",patAge,patSex,clinicPat)
                this.patients.add(pat)
            }
        }catch (e: JSONException) {
            e.printStackTrace()
            throw IOException("JSONException")
        }
        return this.patients
    }

    // Inserts a new measurements to the list in DB
    fun insertPacientes(context: Context){
        var patients:List<Patient>
        doAsync {
            patients = Medicion.populatePatients(applicationContext, profile.mail)
            this@Clinic_list.instanceDatabase.pacienteDao().insertartListaPacientes(patients)
            loadPacientes()
        }
        //ioThread {
        /*
        * Llenar lista con las mediciones del servicio web
        * */
        ///instanceDatabase.medicionDao().insertartListaMediciones(measurements)
        ///loadMediciones()
        //}
    }

    // Custom item click listener for each measurement
    override fun onCustomItemClick(patient: Patient) {
        //val intent = Intent(this, ::class.java)
        //intent.putExtra(PatientList.PATIENT_KEY, patient._idP)
        //startActivityForResult(intent, 3)
        val StartAppIntent = Intent(this,PatientList::class.java)
        PatientList.ACTIV = "clinic"
        StartAppIntent.putExtra(ACCOUNT_NAME,patient.FNameP)
        StartAppIntent.putExtra(ACCOUNT_MAIL,patient.mailC)
        startActivity(StartAppIntent)
    }

    // Handles clicking options item
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.action_sacnQR ->{
                startQR()
                true
            }
            action_logout -> {
                val builder = AlertDialog.Builder(this@Clinic_list)

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

    private fun startQR() {
        IntentIntegrator(this).initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
            }
            else {
                // If QRCode contains data.
                val email = result.contents
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun signOut() {
        Toast.makeText(applicationContext,"Cerrar sesión.", Toast.LENGTH_SHORT).show()
        //finish()
        PatientList.STATUS = "si"
        finish()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            finish()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presione atrás otra vez para salir", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }



}

