package mx.itesm.proyectofinal

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_device_scan.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import mx.itesm.proyectofinal.BLE.BLEConnectionManager
import mx.itesm.proyectofinal.BLE.BLEConstants
import mx.itesm.proyectofinal.BLE.BLEDeviceManager
import mx.itesm.proyectofinal.BLE.BleDeviceData
import mx.itesm.proyectofinal.PatientList.Companion.BLUETOOTH_ADDRESS
import mx.itesm.proyectofinal.PatientList.Companion.bluetoothOn

//OnDeviceScanListener
//CustomDeviceClickListener
class DeviceScanActivity : AppCompatActivity(), OnDeviceScanListener{

    private lateinit var adapter: DeviceListAdapter
    private lateinit var buttonScan: Button
    private var TAG = "ScanAct"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        // Create Adapter
        adapter = DeviceListAdapter(this, this)
        adapter.notifyDataSetChanged() // Prepares list to notify if it suffers any change
        list_devices.adapter = adapter

        buttonScan = findViewById(R.id.button_scan)

        BLEDeviceManager.setListener(this)
        BLEConnectionManager.initBLEService(this@DeviceScanActivity)
        buttonScan.setOnClickListener { onClick() }
        initBLEModule()
    }

    override fun onDestroy() {
        BLEDeviceManager.stopScan(null)
        BLEConnectionManager.unBindBLEService(this@DeviceScanActivity)
        BLEConnectionManager.disconnect()
        super.onDestroy()
    }

    fun onClick(){
        if (bluetoothOn){
            startScan()
        }
        else{
            initBLEModule()
        }
    }

    fun startScan(){
        buttonScan.text = resources.getString(R.string.bluetooth_scan_searching)
        buttonScan.isEnabled = false

        Handler().postDelayed({
            buttonScan.text = resources.getString(R.string.bluetooth_scan_start)
            buttonScan.isEnabled = true
        }, BLEConstants.SCAN_PERIOD)
        BLEDeviceManager.scanBLEDevice(false)
    }

    override fun onScanCompleted(deviceData: BleDeviceData) {
        adapter.addDevice(deviceData)
        adapter.notifyDataSetChanged()
    }

    /**
     *
     * Function de la interface CustomItemClickListener a implementar
     * donde BleDeviceData es el item al que le dieron clic
     */
    override fun onDeviceClick(dataDevice: BleDeviceData) {
        val intent: Intent = Intent()
        intent.putExtra(PatientList.BLUETOOTH_ADDRESS, dataDevice)
        setResult(Activity.RESULT_OK, intent)
        finish()
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
            startActivityForResult(enableBtIntent, PatientList.REQUEST_ENABLE_BT)
        }
        else{
            bluetoothOn = true
        }
    }

    // When receiving information from the measurement class
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // User chose not to enable Bluetooth.
            PatientList.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bluetooth_permission_granted,
                            Toast.LENGTH_LONG).show()
                    bluetoothOn = true
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.bluetooth_permission_not_granted,
                            Toast.LENGTH_LONG).show()
                    bluetoothOn = false
                } else {
                    Toast.makeText(this, R.string.bluetooth_permission_not_granted,
                            Toast.LENGTH_LONG).show()
                    bluetoothOn = false
                }
            }
        }
    }
}
