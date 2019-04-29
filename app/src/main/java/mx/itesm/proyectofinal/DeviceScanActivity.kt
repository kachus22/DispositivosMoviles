package mx.itesm.proyectofinal

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_device_scan.*
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothGattCallback
import android.util.Log


class DeviceScanActivity : AppCompatActivity(), CustomDeviceClickListener {

    private lateinit var mBluetoothHelper: BluetoothHelper
    private lateinit var mBluetoothScanner: BluetoothLeScanner
    private lateinit var adapter: DeviceListAdapter

    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        mBluetoothHelper = PatientList.bluetoothHelper!!
        mHandler = Handler()
        mBluetoothScanner = mBluetoothHelper.mBluetoothAdapter!!.bluetoothLeScanner

        // Create Adapter
        adapter = DeviceListAdapter(this, this)
        adapter.notifyDataSetChanged() // Prepares list to notify if it suffers any change
        list_devices.adapter = adapter

        val buttonScan: View = findViewById(R.id.button_scan)
        buttonScan.setOnClickListener { scanLeDevice(!mScanning) }

    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler?.postDelayed(Runnable {
                button_scan.text = getString(R.string.bluetooth_scan_start)
                mScanning = false
//                button_scan.isEnabled = true
//                AsyncTask.execute { mBluetoothScanner?.startScan(mLeScanCallback) }
                mBluetoothScanner.stopScan(mLeScanCallback)
//                invalidateOptionsMenu()
            }, SCAN_PERIOD)

//            button_scan.isEnabled = false
            button_scan.text = getString(R.string.bluetooth_scan_stop)
            mScanning = true
            mBluetoothScanner.startScan(mLeScanCallback)
        } else {
            mScanning = false
            mBluetoothScanner.stopScan(mLeScanCallback)
//            button_scan.isEnabled = true
        }
//        invalidateOptionsMenu()
    }

    // Device scan callback.
    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            adapter.addDevice(result.device)
            adapter.notifyDataSetChanged()
//            // auto scroll for text view
//            val scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight()
//            // if there is no need to scroll, scrollAmount will be <=0
//            if (scrollAmount > 0)
//                peripheralTextView.scrollTo(0, scrollAmount)
        }
    }

//    private val mLeScanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//        }
//
//        override fun onBatchScanResults(results: List<ScanResult>) {
//            super.onBatchScanResults(results)
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//        }
//    }

    /**
     *
     * funcion de la interface CustomItemClickListener a implementar
     * donde nino es el item al que le dieron clic
     */
    override fun onCustomDeviceClick(btDevice: BluetoothDevice) {
        mBluetoothHelper.mBluetoothDevice = btDevice

        var mBluetoothGatt = btDevice.connectGatt(this, false, mGattCallback)
        mBluetoothGatt.connect()
        Log.i("asd","asd")
//        val intent: Intent = Intent()
//        intent.putExtra(MainActivity.EXTRA_BOOK, btDevice)


//        setResult(Activity.RESULT_OK)
//        finish()
    }

    private val mGattCallback = object : BluetoothGattCallback() {

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.i("asd","asd")
            super.onCharacteristicWrite(gatt, characteristic, status)
            //            updateStatus(characteristic);
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int,
                                             newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("asdasd", "Connected to GATT server.")
//                intentAction = ACTION_GATT_CONNECTED
//                mConnectionState = STATE_CONNECTED
//                broadcastUpdate(intentAction)
//                Log.i(FragmentActivity.TAG, "Connected to GATT server.")
//                // Attempts to discover services after successful connection.
//                Log.i(FragmentActivity.TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices())
//                val task = object : TimerTask() {
//                    fun run() {
//                        if (mBluetoothGatt != null)
//                            mBluetoothGatt.readRemoteRssi()
//                    }
//                }
//                val mRssiTimer = Timer()
//                mRssiTimer.schedule(task, 1000, 1000)

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("asda", "Disconnected from GATT server.")
//                intentAction = ACTION_GATT_DISCONNECTED
//                mConnectionState = STATE_DISCONNECTED
//
//                Log.i(FragmentActivity.TAG, "Disconnected from GATT server.")
//                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            Log.i("asd","asd")
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
//            } else {
//                Log.w(FragmentActivity.TAG, "onServicesDiscovered received: $status")
//            }
//
//            val mCustomService = mBluetoothGatt.getService(MY_UUID_RN4020_SERVICE)
//
//            val mReadCharacteristic = mCustomService.getCharacteristic(MY_UUID_RN4020_CHARACTERISTIC_READ)
//
//            gatt.readCharacteristic(mReadCharacteristic)
//            gatt.setCharacteristicNotification(mReadCharacteristic, true)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.i("asd","asd")
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                gatt.readCharacteristic(characteristic)
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
//            }
        }
//
        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            Log.i("asd","asd")
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

    }
}