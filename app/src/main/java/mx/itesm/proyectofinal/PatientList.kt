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

import Database.Medicion
import Database.MedicionDatabase
import Database.ioThread
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.activity_patient_list.*
import org.jetbrains.anko.doAsync
import mx.itesm.proyectofinal.BLE.*
import mx.itesm.proyectofinal.Utils.CustomItemClickListener
import kotlin.system.exitProcess

/**
 * Declares the patient measurements list. This is the first and main page of the application
 */
class PatientList : AppCompatActivity(), CustomItemClickListener {

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

    lateinit var sharedPreference:SharedPreference
    lateinit var account: GoogleSignInAccount
    // Database variable initialization
    lateinit var instanceDatabase: MedicionDatabase

    // The RecyclerView adapter declaration
    val adapter = MeditionAdapter(this, this)
    private val TAG = "PATIENTLIST"

    lateinit var profile: signInActivity.Companion.Profile
    private var mDevice: BleDeviceData = BleDeviceData("","")

    /**
     * Creates the Patient List activity and inflates the view. Also initializes database calls.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference=SharedPreference(this)
        this.title = "Registros"
        setContentView(R.layout.activity_patient_list)
        val extras = intent.extras?: return

        profile = extras.getParcelable(ACCOUNT)!!
        profilePatient = profile

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

        this.instanceDatabase = MedicionDatabase.getInstance(this)
        lista_pacientes.adapter = adapter

         // Local Database load
        ioThread {
            val measureNum = instanceDatabase.medicionDao().getAnyMedicion(profile.mail)
//            insertMeasurements(this)
            if(measureNum == 0){
                insertMeasurements(this)
            } else{
                loadMediciones()
            }
        }

        floatingActionButton.setOnClickListener { onPress() }
        checkLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEConnectionManager.disconnect()
//        unRegisterServiceReceiver() TODO : check this
    }

    /*
     * Inflates FAB button
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(ACTIV == "clinic"){
            menuInflater.inflate(R.menu.menu_cpat, menu)
        }
        else{
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    // Starts the MainActivity, which starts measuring data from the bluetooth device.
    private fun onPress() {
//        scanDevice(false)
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
//                    finish()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.bluetooth_permission_not_granted,
                            Toast.LENGTH_LONG).show()
//                    finish()
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
                            instanceDatabase.medicionDao().borrarMedicion(data.getIntExtra(DELETE_ID, 0))
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

        intent.putExtra(PATIENT_KEY, medicion._id)
        startActivityForResult(intent, LOAD_MEASURE)
    }

    // Loads measurements from database
    private fun loadMediciones() {
        val measurements = this.instanceDatabase.medicionDao().cargarMeciciones(profile.mail)

        measurements.observe(this, object: Observer<List<Medicion>> {
            override fun onChanged(t: List<Medicion>?) {
                adapter.setMedicion(t!!)
                if(adapter.itemCount == 0){
                    tv_vacia_med.visibility = View.VISIBLE
                }else{
                    tv_vacia_med.visibility = View.GONE
                }
                lista_pacientes.adapter = adapter
                lista_pacientes.adapter?.notifyDataSetChanged()
            }
        })
    }

    // Inserts a new measurements to the list in DB
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
    }

    // Handles clicking options item
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.action_settings ->{
                val intent = Intent(this, ConfigurationActivity::class.java)
                startActivity(intent)
                true
            }
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

    private fun signOut() {
        sharedPreference.clearSharedPreference()
        Toast.makeText(applicationContext,"Cerrar sesión.", Toast.LENGTH_SHORT).show()
        //finish()
        PatientList.STATUS = "si"
        finish()
    }

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

}
