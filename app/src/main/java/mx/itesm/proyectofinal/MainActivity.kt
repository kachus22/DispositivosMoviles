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

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import de.nitri.gauge.Gauge
import kotlinx.android.parcel.Parcelize
import mx.itesm.proyectofinal.BLE.BLEConnectionManager
import mx.itesm.proyectofinal.BLE.BLEConstants
import mx.itesm.proyectofinal.BLE.BleDeviceData
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData


/*
 * MainActivity class declares the activity and inflates the view.
 */
class MainActivity : AppCompatActivity() {

    private val TAG: String = "MainA"

    private var mDevice: BleDeviceData = BleDeviceData("","")

    var dataList: MutableList<Data> = mutableListOf()

    private lateinit var chart: LineChart
    private lateinit var gauge: Gauge

    var entries: MutableList<Entry> = mutableListOf()

    private var started: Boolean = false
    private var valid: Boolean = false
    private var stringData: String = ""
    private var time: Double = 0.0


    companion object {
        const val LIST_ID = "DataList"
    }

    // Creates the view and initialized the run method for the connected thread.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val extras = intent.extras?:return
        mDevice = extras.getParcelable(PatientList.BLUETOOTH_ADDRESS)!!

        registerServiceReceiver()
        BLEConnectionManager.initBLEService(this@MainActivity)
        connectDevice(mDevice.mDeviceAddress)

        gauge = findViewById(R.id.gauge)
        chart = findViewById(R.id.chart)
        launchRefreshUiCheck()

        chart.setNoDataText(resources.getString(R.string.chart_nodata))
        chart.setNoDataTextColor(Color.GRAY)
        chart.setDrawBorders(false)
        chart.isKeepPositionOnRotation = true
        chart.description.isEnabled = false

        launchRefreshUiCheck()
    }


    /**
     * Disconnect the bluetooth helper classes before finishing
     */
    override fun onDestroy() {
        unRegisterServiceReceiver()
        BLEConnectionManager.unBindBLEService(this@MainActivity)
        BLEConnectionManager.disconnect()
        super.onDestroy()
    }

    /**
     *  Handles redirecting and passing information to the ResultsActivity
     */
    fun goToDetail() {
        val intent = Intent(this, ResultsActivity::class.java)
        var max = 0
        val actualData = ArrayList<Data>()
        var holder = dataList
        for (i in 0 until holder.size-1) {
            if (holder[max].mmHg < holder[i].mmHg)
                max = i
        }
        val firstTime = holder[max].timer
        for (i in max until holder.size-1) {
            holder[i].timer -= firstTime
            actualData.add(holder[i])
        }
        if(actualData.size > 20) {
            intent.putExtra(LIST_ID, actualData)
            startActivityForResult(intent, 2)
        }
    }

    /**
     * Refresh the UI for a new take of pressure
     */
    fun launchRefreshUiCheck() {
        registerServiceReceiver()
        entries.clear()
        chart.invalidate() // refresh chart
        chart.clear() // clear chart
        started = false
        valid = false
        stringData = ""
        time = 0.0

    }

    // Handles receiving information from the ResultsActivity and either restarting measurement or
    // redirecting to PatientsList
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            launchRefreshUiCheck()
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        } else if (requestCode == 2 && resultCode == Activity.RESULT_CANCELED) {
            launchRefreshUiCheck()
        }
    }

     /**
     * Connect the application with BLE device with selected device address.
     */
    private fun connectDevice(mDeviceAddress : String) {
        Handler().postDelayed({
            if (BLEConnectionManager.connect(mDeviceAddress)) {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTED", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTION FAILED", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
            }
        }, 500)
    }

    /**
     * Register GATT update receiver
     */
    private fun registerServiceReceiver() {
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when {
                BLEConstants.ACTION_GATT_CONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
                    BLEConnectionManager.findBLEGattService(this@MainActivity)
                }
                BLEConstants.ACTION_GATT_DISCONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
                    BLEConnectionManager.disconnect()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    BLEConnectionManager.findBLEGattService(this@MainActivity)
                }
                BLEConstants.ACTION_DATA_AVAILABLE.equals(action) -> {
                    /**
                     * It's a notification from the external BLE device.
                     * From an intent we get the data and UUID of the service that send it.
                     */
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)

                    if(data != null ){
                        // If it already found an end of a dataset to begin recording datasets
                        if(started){
                            val endDataSet = stringData.indexOf("\r\n") // Find for end of dataset
                            time += 5
                            if(endDataSet != -1){
                                val dataString = stringData.substring(0,endDataSet) // Get dataset
                                var values: List<Float>
                                // Try and catch, inscae it's not possible to convert to Float
                                try{
                                    values = dataString.split(';').map { it.toFloat() }
                                    if(values.size == 3){ // Check if it fulfills being the three values
                                        /**
                                         * Since we have the three values for this dataset,
                                         * it's time to assign them to the different representations or
                                         * structures that we would be using.
                                         */
                                        Log.d("DATA", values.toString())
                                        // Add to the list of every dataset
                                        dataList.add(Data(values[0], values[1], values[2]))
                                        // Move gauge
                                        gauge.moveToValue(values[1])
                                        if(valid){
                                            // Add to graph
                                            // Turn your data into Entry objects
                                            entries.add(Entry(time.toFloat(), values[1]))
                                            val dataSet = LineDataSet(entries, resources.getString(R.string.chart_label)) // add entries to dataset
                                            dataSet.color = resources.getColor(R.color.colorButton)
                                            dataSet.setDrawCircles(false)

                                            Log.d("Data", values.toString())

                                            val lineData = LineData(dataSet)
                                            chart.data = lineData
                                            chart.notifyDataSetChanged() // let the chart know it's data changed
                                            chart.invalidate() // refresh chart
                                            if(values[1] < 30){
                                                valid = false
                                                unRegisterServiceReceiver()
                                                goToDetail()
                                            }
                                        }
                                        else if(values[1] > 140){
                                            valid = true
                                        }
                                    }
                                }
                                catch (e: NumberFormatException){
                                    null
                                }

                                stringData = stringData.substring(endDataSet+2) // Remove dataset used
                            }
                            stringData += data // Add new data to the string
                        }
                        // Haven't found the end of a dataset, keep looking for it
                        else{
                            val endDataSet = data.indexOf("\r\n") // Find for end of dataset
                            if(endDataSet != -1){
                                val endChar = data.indexOf("\n") // Find for end of dataset
                                stringData += data.substring(endChar+1)
                                started = true // Found the end, next dataset most likely to be complete
                            }
                        }
                    }
                }
                BLEConstants.ACTION_DATA_WRITTEN.equals(action) -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }

    /**
     * Intent filter for Handling BLEService broadcast.
     */
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEConstants.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BLEConstants.ACTION_DATA_WRITTEN)

        return intentFilter
    }

    /**
     * Unregister GATT update receiver
     */
    private fun unRegisterServiceReceiver() {
        try {
            this.unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            //May get an exception while user denies the permission and user exists the app
            Log.e(TAG, e.message)
        }
    }

    private fun writeMissedConnection() {
        BLEConnectionManager.writeMissedConnection("A")
    }

    // Handles clicking the back button
    override fun onBackPressed() {
        unRegisterServiceReceiver()
        BLEConnectionManager.disconnect()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}

// Data class. An ArrayList of this type is sent to ResultsActivity
@Parcelize
data class Data(var timer: Float, var mmHg: Float, var pulse: Float) : Parcelable


