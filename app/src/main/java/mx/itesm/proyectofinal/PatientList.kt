/*
    Copyright (C) 2018 - ITESM

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mx.itesm.proyectofinal

import Database.*
import NetworkUtility.ConnectivityReceiver
import NetworkUtility.NetworkConnection.Companion.buildStringPatientsPressures
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.activity_patient_list.*
import org.jetbrains.anko.doAsync
import mx.itesm.proyectofinal.BLE.*
import mx.itesm.proyectofinal.Utils.CustomItemClickListener
import kotlin.system.exitProcess

/**
 * Declares the patient measurements list. This is the first and main page of the application
 */
class PatientList : AppCompatActivity(), CustomItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    /*
     * Companion objects. Since this is the first activity to execute, this one declares the
     * bluetooth helper and initializes it.
     */
    companion object {
        const val ACCOUNT:String = "account"
        const val ACCOUNT_TYPE:String = "account_type"
        var ACTIV:String = "sign"
        var STATUS:String = "no"
        const val DELETE_ID: String = "id"
        const val DEL: String = "Borrar ?"
        const val PATIENT_KEY: String = "Medicion"
        const val REQUEST_ENABLE_BT: Int = 10
        const val REQUEST_COARSE_LOCATION_PERMISSION: Int = 11
        const val BLUETOOTH_DEVICE = 5
        const val BLUETOOTH_ADDRESS = "Address"
        const val LOAD_MEASURE = 4
        const val TAKE_MEASURE = 3
        var profilePatient: signInActivity.Companion.Profile? = null

    }

    private var mSnackBar: Snackbar? = null
    lateinit var sharedPreference:SharedPreference
    lateinit var account: GoogleSignInAccount
    private var searchView: SearchView? = null
    // Database variable initialization
    lateinit var queue: RequestQueue

    // The RecyclerView adapter declaration
    val adapter = MeditionAdapter(this, this)
    private val TAG = "PATIENTLIST"

    lateinit var displayListMediciones:MutableList<Medicion>
    lateinit var fullListMediciones:MutableList<Medicion>

    lateinit var profile: signInActivity.Companion.Profile
    private var mDevice: BleDeviceData = BleDeviceData("","")

    /**
     * Creates the Patient List activity and inflates the view. Also initializes database calls.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference=SharedPreference(this)
        registerReceiver(ConnectivityReceiver(),
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        this.title = "Registros"
        queue = Volley.newRequestQueue(this)
        setContentView(R.layout.activity_patient_list)
        val extras = intent.extras?: return
        if(ACTIV == "clinic") {
            val perf = extras.getParcelable<Patient>(ACCOUNT)
            this.profile = signInActivity.Companion.Profile(perf.mailC!!, perf.FNameP!!, ""!!)
        }
        else{
            profile = extras.getParcelable(ACCOUNT)!!
            profilePatient = profile
        }

        val type = extras.getInt(ACCOUNT_TYPE)

//        actionBar.setTitle("Hello world App")
        if(type == 1){
            supportActionBar?.setTitle(R.string.type_patient)
        }
        else{
            supportActionBar?.setTitle(R.string.type_clinic)
        }

        textView_nombre.text = "Paciente: ${profile.name}"

        val layoutManager = LinearLayoutManager(this)
        lista_pacientes.layoutManager = layoutManager

        lista_pacientes.adapter = adapter

        loadMediciones()
        floatingActionButton.setOnClickListener { onPress() }
        checkLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEConnectionManager.disconnect()
//        unRegisterServiceReceiver() TODO : check this
    }

    /**
     * Function to handle options menu item selection
     * checking the type of account that is in the activity, to decide whether dislaying or not
     * the logout option
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(ACTIV == "clinic"){
            menuInflater.inflate(R.menu.menu_cpat, menu)
        }
        else{
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager

        // associate search item to SearchView
        searchView = menu!!.findItem(R.id.item_search).actionView as SearchView

        // assigns a hint into SearchView query text
        searchView?.queryHint = getString(R.string.search_hintM)

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

                adapter.setMedicion(fullListMediciones)
                lista_pacientes.adapter = adapter
                lista_pacientes.adapter?.notifyDataSetChanged()
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    // Starts the MainActivity, which starts measuring data from the bluetooth device.
    private fun onPress() {
        if (mDevice.mDeviceAddress == "") {
            val intent = Intent(this, DeviceScanActivity::class.java)
            startActivityForResult(intent, BLUETOOTH_DEVICE)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(BLUETOOTH_ADDRESS, mDevice)
            startActivityForResult(intent, TAKE_MEASURE)
        }
    }

    // When receiving information from the measurement class
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // User chose not to enable Bluetooth.
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bluetooth_permission_granted,
                            Toast.LENGTH_LONG).show()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.bluetooth_permission_not_granted,
                            Toast.LENGTH_LONG).show()
                }
            }
            BLUETOOTH_DEVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bluetooth_device_connected,
                            Toast.LENGTH_LONG).show()

                    if( data != null){
                        val extras = data.extras
                        mDevice = extras?.getParcelable(BLUETOOTH_ADDRESS)!!
                        if(mDevice.mDeviceAddress != ""){
                            floatingActionButton.setImageResource(R.drawable.ic_heartplus)
                        }
                    }
                }
            }
            TAKE_MEASURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bluetooth_device_disconnected_ok,
                            Toast.LENGTH_LONG).show()
                    loadMediciones()
                }
                else{
                    Toast.makeText(this, R.string.bluetooth_device_disconnected_cancel,
                            Toast.LENGTH_LONG).show()
                    floatingActionButton.setImageResource(R.drawable.ic_bluetooth_searching)
                    mDevice.mDeviceAddress = ""
                }
            }
            LOAD_MEASURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.getBooleanExtra(DEL, false) == true) {
                        ioThread {
                            //instanceDatabase.medicionDao().borrarMedicion(data.getIntExtra(DELETE_ID, 0))
                        }
                        Toast.makeText(this, R.string.pressure_del,
                                Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Custom item click listener for each measurement
    override fun onCustomItemClick(medicion: Medicion) {
        val intent = Intent(this, ActivityDetail::class.java)
        intent.putExtra(PATIENT_KEY, medicion)
        startActivityForResult(intent, LOAD_MEASURE)
    }

    // Loads measurements from api
    private fun loadMediciones() {
        val url = buildStringPatientsPressures(profile.mail)
        val jRequest =  StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    val t = parseJsonMeds(response)
                    displayListMediciones = t
                    fullListMediciones = t
                    adapter.setMedicion(t)
                    if(adapter.itemCount == 0){
                        tv_vacia_med.visibility = View.VISIBLE
                    }else{
                        tv_vacia_med.visibility = View.GONE
                    }
                    lista_pacientes.adapter = adapter
                    lista_pacientes.adapter?.notifyDataSetChanged()
                },
                Response.ErrorListener {error->
                    //Toast.makeText(applicationContext,"No se pudo cargar pacientes.", Toast.LENGTH_SHORT).show()
                })
        jRequest.tag = "Load"
        queue.add(jRequest)
    }

    /* Inserts a new measurements to the list in DB
    fun insertMeasurements(context: Context){
        var measurements:List<Medicion>
        doAsync {
            measurements = Medicion.populateMeds(applicationContext, this@PatientList.profile.mail)
            this@PatientList.instanceDatabase.medicionDao().insertartListaMediciones(measurements)
            loadMediciones()
        }
        //ioThread {
        /*
        * Llenar lista con las mediciones del servicio web
        * */
        ///instanceDatabase.medicionDao().insertartListaMediciones(measurements)
        ///loadMediciones()
        //}
    }*/

    /**
     * Handles a search and updates the UI
     */
    fun handleSearch(query: String){
        displayListMediciones = doMySearch(query).toMutableList()
        adapter.setMedicion(displayListMediciones)
        lista_pacientes.adapter = adapter
        lista_pacientes.adapter?.notifyDataSetChanged()
    }


    /**
     * Filter by query string
     */
    fun doMySearch(query:String) = fullListMediciones.filter { medicion -> medicion.fecha!!.contains(query,true) }

    /**
     * Function to handle options menu item selection
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.action_perfil ->{
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra(PatientList.ACCOUNT,profile)
                intent.putExtra(PatientList.ACCOUNT_TYPE,0)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                val builder = AlertDialog.Builder(this@PatientList)

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

    /**
     * Check the Location Permission before calling the BLE API's
     */
    private fun checkLocationPermission() {
        if(isAboveMarshmallow()){
            when {
                isLocationPermissionEnabled() -> initBLEModule()
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
                else -> requestLocationPermission()
            }
        }
        else {
            initBLEModule()
        }
    }

    /**
     * The location permission is incorporated in Marshmallow and Above
     */
    private fun isAboveMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * Check with the system- If the permission already enabled or not
     */
    private fun isLocationPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request Location API
     * If the request go to Android system and the System will throw a dialog message
     * user can accept or decline the permission from there
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_COARSE_LOCATION_PERMISSION)
    }

    /**
     * If the user decline the Permission request and tick the never ask again message
     * Then the application can't proceed further steps
     * In such situation- App need to prompt the user to do the change form Settings Manually
     */
    private fun displayRationale() {
        AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_not_granted)
                .setMessage(R.string.location_permission_disabled)
                .setPositiveButton(R.string.ok
                ) { _, _ -> requestLocationPermission() }
                .setNegativeButton(R.string.cancel
                ) { _, _ -> }
                .show()
    }

    /**
     * If the user either accept or reject the Permission- The requested App will get a callback
     * Form the call back we can filter the user response with the help of request key
     * If the user accept the same- We can proceed further steps
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_COARSE_LOCATION_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting location permission.")
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, R.string.location_permission_granted,
                            Toast.LENGTH_LONG).show()
                    initBLEModule()
                } else {
                    Toast.makeText(this, R.string.location_permission_not_granted,
                            Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    /**
     *After receive the Location Permission, the Application need to initialize the
     * BLE Module and BLE Service
     */
    private fun initBLEModule() {
        // BLE initialization
        if (!BLEDeviceManager.init(this)) {
            Toast.makeText(this, R.string.bluetooth_le_not_supported, Toast.LENGTH_SHORT).show()
            return
        }

        if (!BLEDeviceManager.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    /**
     * Function to sign out the google account in the app, cleaning all the session variables
     * and going back to the type of account choosing activity
     */
    private fun signOut() {
        sharedPreference.clearSharedPreference()
        Toast.makeText(applicationContext,"Cerrar sesión.", Toast.LENGTH_SHORT).show()
        //finish()
        PatientList.STATUS = "si"
        val StartAppIntent = Intent(this,ElegirTipo::class.java)
        startActivity(StartAppIntent)
        finish()
    }

    /**
     * Function to retain the user in the current activity if back pressed, but checks type of user
     * before, to decide whether exit app or go back
     */
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if(sharedPreference.getValueString("TIPO_USUARIO")=="clinica"){
            super.onBackPressed()
        }else{
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true);
                finish()
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Presione atrás otra vez para salir", Toast.LENGTH_SHORT).show()

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    /**
     * Function to execute when the network state is changed
     */
    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            val messageToUser = R.string.internet_no_title
            mSnackBar = Snackbar.make(findViewById(R.id.rootLayout), messageToUser, Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
            mSnackBar?.duration = Snackbar.LENGTH_INDEFINITE
            mSnackBar?.show()
        } else {
            loadMediciones()
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
        var listaAux : Array<Medicion>
        if(adapter.mediciones!=null){
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
                var listaAux : Array<Medicion> = savedInstanceState.getParcelableArray("VALUES") as Array<Medicion>
                if(listaAux!=null){
                    adapter.setMedicion(listaAux.toMutableList())
                }
            }
        }
    }

}
