package mx.itesm.proyectofinal.BLE

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BleDeviceData( var mDeviceName: String,
                          var mDeviceAddress: String) : Parcelable