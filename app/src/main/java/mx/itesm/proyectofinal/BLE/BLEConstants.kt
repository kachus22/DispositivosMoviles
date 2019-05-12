package mx.itesm.proyectofinal.BLE

class BLEConstants {

    companion object {
        const val ACTION_GATT_CONNECTED = "mx.itesm.proyectofinal.ACTION_GATT_CONNECTED" //Strings representing actions to broadcast to activities
        const val ACTION_GATT_DISCONNECTED = "mx.itesm.proyectofinal.ACTION_GATT_DISCONNECTED" // Old com.zco.ble
        const val ACTION_GATT_SERVICES_DISCOVERED = "mx.itesm.proyectofinal.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "mx.itesm.proyectofinal.ACTION_DATA_AVAILABLE"
        const val ACTION_DATA_WRITTEN = "mx.itesm.proyectofinal.ACTION_DATA_WRITTEN"
        const val EXTRA_DATA = "mx.itesm.proyectofinal.EXTRA_DATA"
        const val EXTRA_UUID = "mx.itesm.proyectofinal.EXTRA_UUID"


        const val LE_DATA_PRIVATE_CHAR = "beb5483e-36e1-4688-b7f5-ea07361b26a8" //Characteristic for MLDP Data, properties - notify, write

        const val CHARACTERISTIC_DEVICE_NAME = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_APPEARANCE = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_DEVICE_INFORMATION = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_MANUFACTURE_NAME = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_MODEL_NUMBER = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_SYSTEM_ID = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_BATTERY_SERVICE = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_BATTERY_LEVEL = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_EMERGENCY_ALERT = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_EMERGENCY_GATT = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_MISSED_CONNECTION = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_CATCH_ALL = ""    //Special UUID for get the device Name
        const val CHARACTERISTIC_CATCH = ""    //Special UUID for get the device Name
        const val SCAN_PERIOD: Long = 10000     // Time in milliseconds
    }
}