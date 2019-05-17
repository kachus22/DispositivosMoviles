package mx.itesm.proyectofinal

import Database.Medicion
import Database.MedicionDatabase
import Database.Patient
import NetworkUtility.ConnectivityReceiver
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.zxing.integration.android.IntentIntegrator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_clinic_list.*
import mx.itesm.proyectofinal.Utils.CustomItemClickListener2
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import NetworkUtility.NetworkConnection
import android.app.SearchManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.design.widget.Snackbar
import com.android.volley.RequestQueue
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import java.nio.charset.Charset

/**
 * Activity of Clinic list
 * Main clinic activity where all the patients linked to the clinic profile that is executing the
 * activity are shown.
 *
 */
class Clinic_list : AppCompatActivity(), CustomItemClickListener2, ConnectivityReceiver.ConnectivityReceiverListener  {

    //listener if the device is connected
    private var hasConnection: Int = 0
    //Bar to display if not connected
    private var mSnackBar: Snackbar? = null
    //Values that are saved in the app as session variables
    lateinit var sharedPreference:SharedPreference
    // Database variable initialization
    lateinit var instanceDatabase: MedicionDatabase
    //The profile object of the clinic
    lateinit var profile: signInActivity.Companion.Profile
    lateinit var queue: RequestQueue


    private var searchView: SearchView? = null
    lateinit var displayListPatients:MutableList<Patient>
    lateinit var fullListPatients:MutableList<Patient>


    // The RecyclerView adapter declaration
    var adapter = PatientAdapter(this, this)

    /**
     * Function that executes every time the activity is created, setting the layout and getting the
     * extra data from previous activities
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference=SharedPreference(this)
        setContentView(R.layout.activity_clinic_list)
        registerReceiver(ConnectivityReceiver(),
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        queue = Volley.newRequestQueue(this)
        val extras = intent.extras?: return
        profile = extras.getParcelable(PatientList.ACCOUNT)!!

        textView_nombre.text = "Clinica/Doctor: ${profile.name}"

        val layoutManager = LinearLayoutManager(this)
        lista_clinica.layoutManager = layoutManager

        this.instanceDatabase = MedicionDatabase.getInstance(this)

        lista_clinica.adapter = adapter

        loadPacientes()

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                Log.d("asd",query)
                Log.d("asd","asd")
            }
        }


    }

//    /**
//     * Create the options Menu, that includes the qr reader and the sign out options
//     */
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_clinic, menu)
//        return true
//    }

    /**
     * Loads measurements from database
     */
    private fun loadPacientes() {
        val url = "https://heart-app-tec.herokuapp.com/clinics/"+ profile.mail
        val jRequest =  StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    val str =  String(response.toByteArray(), charset("UTF-8"));
                    val patients = parseJsonPats(str, profile.mail)
                    displayListPatients = patients
                    fullListPatients = patients
                    adapter.setPatient(patients)
                    if (adapter.itemCount == 0) {
                        tv_vacia.visibility = View.VISIBLE
                    } else {
                        tv_vacia.visibility = View.GONE
                    }
                    lista_clinica.adapter = adapter
                    lista_clinica.adapter?.notifyDataSetChanged()
                },
                Response.ErrorListener {error->
                    //Toast.makeText(applicationContext,"No se pudo cargar pacientes.", Toast.LENGTH_SHORT).show()
                })
        jRequest.tag = "Load"
        queue.add(jRequest)
    }

    /**
     * Function to parse Json of patients that is received from the web service
     */
    fun parseJsonPats(jsonString: String?, clinicPat : String): MutableList<Patient>{
        var patients : MutableList<Patient> = mutableListOf()
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
                patients.add(pat)
            }
        }catch (e: JSONException) {
            e.printStackTrace()
            throw IOException("JSONException")
        }
        return patients
    }

    // Inserts a new measurements to the list in DB
    /*fun insertPacientes(context: Context){
        var patients:List<Patient>
        doAsync {
            patients = Medicion.populatePatients(applicationContext, profile.mail)
            this@Clinic_list.instanceDatabase.pacienteDao().insertartListaPacientes(patients)
            loadPacientes()
        }
        ioThread {
        //Llenar lista con las mediciones del servicio web
        //instanceDatabase.medicionDao().insertartListaMediciones(measurements)
        //loadMediciones()
        }
    }*/

    /**
    ** Custom item click listener for each measurement
     *
     **/
    override fun onCustomItemClick(patient: Patient) {
        //val intent = Intent(this, ::class.java)
        //intent.putExtra(PatientList.PATIENT_KEY, patient._idP)
        //startActivityForResult(intent, 3)
        val startAppIntent = Intent(this,PatientList::class.java)
        PatientList.ACTIV = "clinic"
        startAppIntent.putExtra(PatientList.ACCOUNT, patient)
        startAppIntent.putExtra(PatientList.ACCOUNT_TYPE, 0)
        startActivity(startAppIntent)
    }

    /**
     * Function to handle options menu item selection
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.action_sacnQR ->{
                if(hasConnection==1){
                    startQR()
                }else{
                    Toast.makeText(applicationContext,R.string.internet_no_title, Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_logout -> {
                val builder = AlertDialog.Builder(this@Clinic_list)

                builder.setTitle("Cerrar sesión")

                builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")

                builder.setPositiveButton("Cerrar sesión") { _, _ ->
                    signOut()
                }
                // Display a negative button on alert dialog
                builder.setNegativeButton("Cancelar") { _, _ ->
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

    /**
     * Starts QR intent
     */
    private fun startQR() {
        IntentIntegrator(this).initiateScan()
    }

    /**
     * Starts activity for pressure register
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
            }
            else {
                // If QRCode contains data.
                val email = result.contents
                val url = NetworkConnection.buildStringPatients(email)
                val map: HashMap<String, Any> = hashMapOf("clinic" to profile.mail, "sex" to "M", "age" to 100)
                println(JSONObject(map))
                val jRequest =  JsonObjectRequest(Request.Method.POST, url, JSONObject(map),
                        Response.Listener<JSONObject> { response ->
                            // Display the first 500 characters of the response string.
                            loadPacientes()
                        },
                        Response.ErrorListener {error->
                            //Toast.makeText(applicationContext,"No se pudo agregar paciente.", Toast.LENGTH_SHORT).show()
                        })
                queue.add(jRequest)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Function to sign out the google account in the app, cleaning all the session variables
     * and going back to the type of account choosing activity
     */
    private fun signOut() {
        sharedPreference.clearSharedPreference()
        Toast.makeText(applicationContext,"Cerrar sesión.", Toast.LENGTH_SHORT).show()
        PatientList.STATUS = "si"
        val StartAppIntent = Intent(this,ElegirTipo::class.java)
        startActivity(StartAppIntent)
        finish()
    }

    //Value to check if double tapped the back button
    private var doubleBackToExitPressedOnce = false
    /**
     * Function to retain the user in the current activity if back pressed, but when double back
     * pressed, we exit the app.
     */
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            finish()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presione atrás otra vez para salir", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    /**
     * Handles a search and updates the UI
     */
    fun handleSearch(query: String){
        displayListPatients = doMySearch(query).toMutableList()
        adapter.setPatient(displayListPatients)
        lista_clinica.adapter = adapter
        lista_clinica.adapter?.notifyDataSetChanged()
    }


    /**
     * Filter by query string
     */
    fun doMySearch(query:String) = fullListPatients.filter { patient -> patient.FNameP!!.contains(query,true) }

    /**
     * onCreateOptionsMenu. crea un menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_clinic, menu)

        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager

        // associate search item to SearchView
        searchView = menu.findItem(R.id.item_search).actionView as SearchView

        // assigns a hint into SearchView query text
        searchView?.queryHint = getString(R.string.search_hint)

        // searchableInfo object represents the searchable configuration
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        //searchview object to auto update each time text changes on when submit search is pressed
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                handleSearch(newText)
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                handleSearch(query)
                return false
            }
        })

        searchView?.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener {

            override fun onFocusChange(view: View, queryTextFocused: Boolean) {
                if (!queryTextFocused) {
                    searchView?.setIconified(true)
                }
            }
        })
        // Actualiza lista con todos los elementos al cerrar el SearchView
        searchView?.setOnCloseListener(object : SearchView.OnCloseListener {

            override fun onClose(): Boolean {


                searchView?.onActionViewCollapsed()

                adapter.setPatient(fullListPatients)
                lista_clinica.adapter = adapter
                lista_clinica.adapter?.notifyDataSetChanged()


                return false
            }
        })


        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Function to execute when the network state is changed
     */
    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            hasConnection = 0
            val messageToUser = R.string.internet_no_title
            mSnackBar = Snackbar.make(findViewById(R.id.rootLayout), messageToUser, Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
            mSnackBar?.duration = Snackbar.LENGTH_INDEFINITE
            mSnackBar?.show()
        } else {
            hasConnection = 1
            loadPacientes()
            mSnackBar?.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    /**
     * Function to detect when the network state is changed
     */
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }

    /**
     * Function to save temporary the data of the activity
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        var listaAux : Array<Patient>
        if(adapter.patients!=null){
            listaAux = adapter.getValues().toTypedArray()
            savedInstanceState?.putParcelableArray("VALUES", listaAux)
        }
    }

    /**
     * Function to retreive the temporary stored data for the activity
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if(savedInstanceState!=null){
            if(savedInstanceState.getParcelableArray("VALUES")!=null){
                var listaAux : Array<Patient> = savedInstanceState.getParcelableArray("VALUES") as Array<Patient>
                if(listaAux!=null){
                    adapter.setPatient(listaAux.toMutableList())
                }
            }
        }
    }

}