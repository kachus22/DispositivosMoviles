package mx.itesm.proyectofinal.BLE

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log



object BLEConnectionManager {

    private const val TAG = "uuidBLECManager"
    private var mBLEService: BLEService? = null
    private var isBind = false
    private var mDataBLEForEmergency: BluetoothGattCharacteristic? = null

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBLEService = (service as BLEService.LocalBinder).getService()

            if (!mBLEService?.initialize()!!) {
                Log.e(TAG, "Unable to initialize")
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            mBLEService = null
        }
    }

    /**
     * Initialize Bluetooth service.
     */
    fun initBLEService(context: Context) {
        try {

            if (mBLEService == null) {
                val gattServiceIntent = Intent(context, BLEService::class.java)

                if (context != null) {
                    isBind = context.bindService(gattServiceIntent, mServiceConnection,
                            Context.BIND_AUTO_CREATE)
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }

    }

    /**
     * Unbind BLE Service
     */
    fun unBindBLEService(context: Context) {

        if (mServiceConnection != null && isBind) {
            context.unbindService(mServiceConnection)
        }

        mBLEService = null
    }

    /**
     * Connect to a BLE Device
     */
    fun connect(deviceAddress: String): Boolean {
        var result = false

        if (mBLEService != null) {
            result = mBLEService!!.connect(deviceAddress)
        }

        return result

    }

    /**
     * Disconnect
     */
    fun disconnect() {
        if (null != mBLEService) {
            mBLEService!!.disconnect()
            mBLEService = null
        }

    }

//    fun writeEmergencyGatt(value: ByteArray) {
//        if (mDataBLEForEmergency != null) {
//            mDataBLEForEmergency!!.value = value
//            writeBLECharacteristic(mDataBLEForEmergency)
//        }
//    }

    // TODO check this
    fun writeMissedConnection(value: String) {
        val copy =mDataBLEForEmergency
        copy!!.value = byteArrayOf(1, 1)
//        copy!!.value = value.toByteArray()
        writeBLECharacteristic(mDataBLEForEmergency)
//        var gattCharacteristic =  BluetoothGattCharacteristic(java.util.UUID.
//                fromString("0000ffe1-0000-1000-8000-00805f9b34fb"), PROPERTY_WRITE, PERMISSION_WRITE)
//        if (gattCharacteristic != null) {
//            gattCharacteristic.setValue(value)
//            writeBLECharacteristic(gattCharacteristic)
//        }
    }
    /**
     * Write BLE Characteristic.
     */
    private fun writeBLECharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (null != characteristic) {
            if (mBLEService != null) {
                mBLEService?.writeCharacteristic(characteristic)
            }
        }
    }

    fun readMissedConnection(UUID: String) {
        var gattCharacteristic =  BluetoothGattCharacteristic(java.util.UUID.
                fromString(UUID), PROPERTY_READ, PERMISSION_READ)
        if (gattCharacteristic != null) {
            readMLDPCharacteristic(gattCharacteristic)
        }
    }

    fun readBatteryLevel(UUID: String) {
        var gattCharacteristic =  BluetoothGattCharacteristic(java.util.UUID.
                fromString(UUID), PROPERTY_READ, PERMISSION_READ)
        if (gattCharacteristic != null) {
            readMLDPCharacteristic(gattCharacteristic)
        }
    }

    fun readEmergencyGatt(UUID: String) {
//        var gattCharacteristic =  BluetoothGattCharacteristic(java.util.UUID.
//                fromString(UUID), PROPERTY_READ, PERMISSION_READ)
        var gattCharacteristic =  BluetoothGattCharacteristic(java.util.UUID.
                fromString("0000ffe1-0000-1000-8000-00805f9b34fb"), PROPERTY_READ, PERMISSION_READ)
        if (gattCharacteristic != null) {
            readMLDPCharacteristic(gattCharacteristic)
        }
    }

    /**
     * Write MLDP Characteristic.
     */
    private fun readMLDPCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (null != characteristic) {
            if (mBLEService != null) {
                mBLEService?.readCharacteristic(characteristic)
            }
        }
    }


    /**
     * findBLEGattService
     */
    fun findBLEGattService(mContext: Context) {

        if (mBLEService == null) {
            return
        }

        if (mBLEService!!.getSupportedGattServices() == null) {
            return
        }

        var uuid: String
        mDataBLEForEmergency = null
        // Search for services available in bluetooth device
        val serviceList = mBLEService!!.getSupportedGattServices()
        if (serviceList != null) {
            for (gattService in serviceList) {
                // Lookup for the service we are interested in
                if (gattService.uuid.toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb",
                                ignoreCase = true)) {
                    val gattCharacteristics = gattService.characteristics

                    // Loop for the characterisctics of our desired service
                    for (gattCharacteristic in gattCharacteristics) {
                        uuid = if (gattCharacteristic.uuid != null) gattCharacteristic.uuid.toString() else ""
                        if (uuid == "0000ffe1-0000-1000-8000-00805f9b34fb") {
                        // Check if the characterisctic we are interested in is on the list
//                        if (uuid.equals("0000ffe0-0000-1000-8000-00805f9b34fb",
//                                        ignoreCase = true)){
                            var newChar = gattCharacteristic
                            var asd = gattCharacteristic.properties
                            newChar = setProperties(newChar)
                            mDataBLEForEmergency = newChar
                        }
                    }
                }

            }
        }

    }

    private fun setProperties(gattCharacteristic: BluetoothGattCharacteristic):
            BluetoothGattCharacteristic {
        val characteristicProperties = gattCharacteristic.properties

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            Log.i(TAG, "notify activaed")
            mBLEService?.setCharacteristicNotification(gattCharacteristic, true)

        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
            Log.i(TAG, "indicate")
            mBLEService?.setCharacteristicIndication(gattCharacteristic, true)
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
            Log.i(TAG, "write activaed")
            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }

        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
            Log.i(TAG, "write no repsonse caticasd")
            gattCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }
        if (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            Log.i(TAG, "read activaed")
            gattCharacteristic.writeType = BluetoothGattCharacteristic.PROPERTY_READ
        }
        return gattCharacteristic
    }


    fun writeHEART(value: String) {
        var mDataMDLPForEmergency =  BluetoothGattCharacteristic(java.util.UUID.
                fromString("0000ffe1-0000-1000-8000-00805f9b34fb"), PROPERTY_WRITE, PERMISSION_WRITE)
        if (mDataMDLPForEmergency != null) {
            mDataMDLPForEmergency.setValue("asd")
            writeBLECharacteristic(mDataMDLPForEmergency)
        }
    }

}