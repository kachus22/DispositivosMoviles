package mx.itesm.proyectofinal

import android.bluetooth.BluetoothDevice
import mx.itesm.proyectofinal.BLE.BleDeviceData

interface OnDeviceScanListener {

    /**
     * Scan Completed -
     *
     * @param deviceDataList - Send available devices as a list to the init Activity
     * The List Contain, device name and mac address,
     */
    fun onScanCompleted(deviceDataList: BleDeviceData)

    fun onDeviceClick(deviceData: BleDeviceData)
}