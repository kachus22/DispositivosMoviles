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

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.row_bluetooth.*
import mx.itesm.proyectofinal.BLE.BleDeviceData


/*
 * Bluetooth Helper. Handles connecting and disconnecting bluetooth communication to and from
 * external device. Declares sockets and selects device to connect to.
 * @params. activity. The activity that handles the bluetooth communication. Preferably an activity
 * that executes at the start of the application.
 */
class DeviceListAdapter(private val context: Context,
                        val listener: OnDeviceScanListener) : BaseAdapter() {
    private var mLeDevices: ArrayList<BleDeviceData> = arrayListOf()
    private var mAddresses: MutableSet<String> = mutableSetOf()

    fun addDevice(device: BleDeviceData) {
        if (!mAddresses.contains(device.mDeviceAddress)) {
            mAddresses.add(device.mDeviceAddress)
            mLeDevices.add(device)
        }
    }

    fun getDevice(position: Int): BleDeviceData {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(position: Int): BleDeviceData? {
        return mLeDevices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val bluetoothDeviceHolder: BluetoothDeviceViewHolder
        val rowView: View
//        val bluetoothDevice: BluetoothDevice = getItem(position) as BluetoothDevice
        val bluetoothDevice: BleDeviceData = getItem(position) as BleDeviceData

        // General ListView optimization code.
        // si no exsite una vista para el renglón,se crea
        if (convertView == null) {
            val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            rowView = inflater.inflate(R.layout.row_bluetooth, parent, false)

            bluetoothDeviceHolder = BluetoothDeviceViewHolder(rowView)

            rowView.tag = bluetoothDeviceHolder
        } else {

            rowView = convertView
            bluetoothDeviceHolder = convertView.tag as BluetoothDeviceViewHolder
        }
        bluetoothDeviceHolder.bind(bluetoothDevice)
        return rowView
    }

    /**
     * Clase interna que aloja los elementos de un renglón
     */
    inner class BluetoothDeviceViewHolder(override val containerView: View) : LayoutContainer {

        fun bind(deviceData: BleDeviceData) {
            text_name.text = deviceData.mDeviceName
            text_address.text = deviceData.mDeviceAddress
            containerView.setOnClickListener { view -> listener.onDeviceClick(deviceData) }
        }
    }


}