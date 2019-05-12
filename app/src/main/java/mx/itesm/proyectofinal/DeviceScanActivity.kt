package mx.itesm.proyectofinal

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_device_scan.*
import android.content.Intent
import mx.itesm.proyectofinal.BLE.BLEConnectionManager
import mx.itesm.proyectofinal.BLE.BLEDeviceManager
import mx.itesm.proyectofinal.BLE.BleDeviceData
import mx.itesm.proyectofinal.PatientList.Companion.BLUETOOTH_ADDRESS

//OnDeviceScanListener
//CustomDeviceClickListener
class DeviceScanActivity : AppCompatActivity(), OnDeviceScanListener{

    private lateinit var adapter: DeviceListAdapter
    private var TAG = "ASD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        // Create Adapter
        adapter = DeviceListAdapter(this, this)
        adapter.notifyDataSetChanged() // Prepares list to notify if it suffers any change
        list_devices.adapter = adapter

        val buttonScan: View = findViewById(R.id.button_scan)

        BLEDeviceManager.setListener(this)
        BLEConnectionManager.initBLEService(this@DeviceScanActivity)
        buttonScan.setOnClickListener { BLEDeviceManager.scanBLEDevice(false) }
    }

    override fun onDestroy() {
        super.onDestroy()
        BLEDeviceManager.stopScan(null)
        BLEConnectionManager.unBindBLEService(this@DeviceScanActivity)
        BLEConnectionManager.disconnect()
//        unRegisterServiceReceiver() TODO : check this
    }

    override fun onScanCompleted(deviceData: BleDeviceData) {
        adapter.addDevice(deviceData)
        adapter.notifyDataSetChanged()
    }

    /**
     *
     * funcion de la interface CustomItemClickListener a implementar
     * donde nino es el item al que le dieron clic
     */
    override fun onDeviceClick(dataDevice: BleDeviceData) {
        val intent: Intent = Intent()
        intent.putExtra(PatientList.BLUETOOTH_ADDRESS, dataDevice)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }




//    /**
//     * Connect the application with BLE device with selected device address.
//     */
//    private fun connectDevice(mDeviceAddress : String) {
//        Handler().postDelayed({
//            BLEConnectionManager.initBLEService(this@DeviceScanActivity)
//            if (BLEConnectionManager.connect(mDeviceAddress)) {
//                Toast.makeText(this@DeviceScanActivity, "DEVICE CONNECTED", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this@DeviceScanActivity, "DEVICE CONNECTION FAILED", Toast.LENGTH_SHORT).show()
//            }
//        }, 100)
//    }
//
//    /**
//     * Register GATT update receiver
//     */
//    private fun registerServiceReceiver() {
//        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
//    }
//
//    private val mGattUpdateReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//            when {
//                BLEConstants.ACTION_GATT_CONNECTED.equals(action) -> {
//                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
//                    BLEConnectionManager.findBLEGattService(this@DeviceScanActivity)
//                }
//                BLEConstants.ACTION_GATT_DISCONNECTED.equals(action) -> {
//                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
//                }
//                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action) -> {
//                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
//                    try {
//                        Thread.sleep(500)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                    BLEConnectionManager.findBLEGattService(this@DeviceScanActivity)
//                }
//                BLEConstants.ACTION_DATA_AVAILABLE.equals(action) -> {
//                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
//                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)
//                    Log.i(TAG, "ACTION_DATA_AVAILABLE $data")
//
//                }
//                BLEConstants.ACTION_DATA_WRITTEN.equals(action) -> {
//                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
//                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
//                }
//            }
//        }
//    }
//
//    /**
//     * Intent filter for Handling BLEService broadcast.
//     */
//    private fun makeGattUpdateIntentFilter(): IntentFilter {
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED)
//        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED)
//        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED)
//        intentFilter.addAction(BLEConstants.ACTION_DATA_AVAILABLE)
//        intentFilter.addAction(BLEConstants.ACTION_DATA_WRITTEN)
//
//        return intentFilter
//    }
//
//    /**
//     * Unregister GATT update receiver
//     */
//    private fun unRegisterServiceReceiver() {
//        try {
//            this.unregisterReceiver(mGattUpdateReceiver)
//        } catch (e: Exception) {
//            //May get an exception while user denies the permission and user exists the app
//            Log.e(TAG, e.message)
//        }
//
//    }
//
//    private fun writeEmergency() {
//        BLEConnectionManager.writeEmergencyGatt("0xfe");
//    }
//
//    private fun writeBattery() {
//        BLEConnectionManager.writeBatteryLevel("100")
//    }
//
//    private fun writeMissedConnection() {
//        BLEConnectionManager.writeMissedConnection("0x00")
//    }
//
//    private fun readMissedConnection() {
//        BLEConnectionManager.readMissedConnection(getString(R.string.char_uuid_missed_calls))
//    }
//
//    private fun readBatteryLevel() {
//        BLEConnectionManager.readBatteryLevel(getString(R.string.char_uuid_emergency))
//    }
//
//    private fun readEmergencyGatt() {
//        BLEConnectionManager.readEmergencyGatt(getString(R.string.char_uuid_emergency))
//    }
}
