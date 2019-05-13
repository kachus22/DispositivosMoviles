package mx.itesm.proyectofinal

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_device_scan.*
import android.content.Intent
import android.os.Handler
import android.widget.Button
import mx.itesm.proyectofinal.BLE.BLEConnectionManager
import mx.itesm.proyectofinal.BLE.BLEConstants
import mx.itesm.proyectofinal.BLE.BLEDeviceManager
import mx.itesm.proyectofinal.BLE.BleDeviceData
import mx.itesm.proyectofinal.PatientList.Companion.BLUETOOTH_ADDRESS

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
        buttonScan.setOnClickListener { startScan() }
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEDeviceManager.stopScan(null)
        BLEConnectionManager.unBindBLEService(this@DeviceScanActivity)
        BLEConnectionManager.disconnect()
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
}
