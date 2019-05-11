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

import Database.Medicion
import Database.MedicionDatabase
import Database.ioThread
import NetworkUtility.NetworkConnection
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_results.*
import me.rohanjahagirdar.outofeden.Utils.FetchCompleteListener
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

/*
 * Resulsts activtiy. Creates the activity and inflates the view.
 */
class ResultsActivity : AppCompatActivity(), View.OnClickListener, FetchCompleteListener {

    private lateinit var detailsJSON: JSONObject

    companion object {
        const val SYSTOLIC_DEVICE = "systolic_results"
        const val DIASTOLIC_DEVICE = "diastolic_results"
        const val SYSTOLIC_MANUAL = "systolic_manual_results"
        const val DIASTOLIC_MANUAL = "diastolic_manual_results"
        const val VERIFIED_VALUE = "verified_check"
        const val SELECTED_ARM = "arm_id"
        const val GRAPH_ID = "generated_graph"
    }

    var systolicRes: Double = 0.0
    var diastolicRes: Double = 0.0
    var validateCheck: Boolean = false
    var selectedArm: String = ""
    var fecha: String? = ""
    lateinit var queue: RequestQueue


    private lateinit var chart: LineChart
    var entries: MutableList<Entry> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        this.title = "Resultados"
        queue = Volley.newRequestQueue(this)

        val extras = intent.extras?:return

        val dataList: List<Data> = extras.getParcelableArrayList(MainActivity.LIST_ID)!!

        chart = findViewById(R.id.chart)
        chart.setNoDataText(resources.getString(R.string.chart_nodata))
        chart.setNoDataTextColor(Color.GRAY)
        chart.setDrawBorders(false)
        chart.isKeepPositionOnRotation = true
        chart.description.isEnabled = false

        calculateResults(dataList)
        verifyNurseMode()

        button_accept.setOnClickListener { v -> onClick(v) }
        button_retry.setOnClickListener { v -> onClick(v) }

        checkBox_verify_results.setOnCheckedChangeListener { _, isChecked ->
            validateCheck = isChecked
        }
    }

    fun postPressure(){
        val url = NetworkConnection.buildStringPressures()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())

        val map: HashMap<String, Any> = hashMapOf(
                "date" to currentDate,
                "verified" to validateCheck.toString(),
                "systolic" to systolicRes.toInt().toString(),
                "diastolic" to diastolicRes.toInt().toString(),
                "manual_systolic" to edit_systolic_manual.text.toString(),
                "manual_diastolic" to edit_diastolic_manual.text.toString(),
                "arm" to selectedArm,
                "patient" to PatientList.profilePatient!!.mail
                )
        val jRequest =  JsonObjectRequest(Request.Method.POST, url, JSONObject(map),
                com.android.volley.Response.Listener<JSONObject> { response ->
                    // Display the first 500 characters of the response string.
                    fetchComplete()
                },
                com.android.volley.Response.ErrorListener { error->
                    Toast.makeText(applicationContext,"No se pudo agregar paciente.", Toast.LENGTH_SHORT).show()
                })
        queue.add(jRequest)
    }

    override fun onClick(view: View?) {
        when(view!!.id){
            R.id.button_accept -> {
                if(!(edit_diastolic_manual.text.isEmpty() || edit_systolic_manual.text.isEmpty() || selectedArm == "")) {

                    fecha = toString(Calendar.getInstance().time)

                    val instanceDatabase = MedicionDatabase.getInstance(this)
                    var graphString = ""
                    Log.d("asd",entries.size.toString())
                    for(i in 0..entries.size-1){
                        graphString += "${entries[i].x};${entries[i].y}"
                    }

                    Log.d("asd","asd")

                    ioThread {
                        instanceDatabase.medicionDao().insertarMedicion(Medicion(systolicRes.toInt().toString(),
                                diastolicRes.toInt().toString(),
                                edit_systolic_manual.text.toString(),
                                edit_diastolic_manual.text.toString(),
                                fecha,
                                validateCheck,
                                selectedArm,
                                graphString,
                                graphString))
                        // TODO borrar iniciales de bd
                    }
                    if (NetworkConnection.isNetworkConnected(this)) {
                        postPressure()
                    } else {
                        // alerta usando la librería de ANKO
                        alert(message = resources.getString(R.string.internet_no_desc), title = resources.getString(R.string.internet_no_title)) {
                            okButton {  }
                        }.show()
                    }
                }
                else {
                    Toast.makeText(this, "Llena todos los campos manuales", Toast.LENGTH_LONG).show()
                }
            }

            R.id.button_retry -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_arm_left ->
                    if (checked) {
                        selectedArm = "I"
                    }
                R.id.radio_arm_right ->
                    if (checked) {
                        selectedArm = "D"
                    }
            }
        }
    }

    private fun calculateResults(data: List<Data>){

        val fixed = arrayOfNulls<Double>(data.size)     //Arreglo para almacenar los valores de Fixed mmHg
        var fixedSum = 0.0                              //Suma de valores de Fixed mmHg, utilizado para cálculos de mmHg mov
        val mov = arrayOfNulls<Double>(data.size)       //Arreglo para los valores de mmHg mov
        val pend = arrayOfNulls<Double>(data.size)      //Arreglo para los valores de Pend
        var pendSum = 0.0                               //Variable para almacenar la suma que se usará para calcular el promedio de Pend
        val pendN = arrayOfNulls<Double>(data.size)     //Arreglo para almacenar los valores de Pend Norm
        val pendNMov = arrayOfNulls<Double>(data.size)  //Arreglo para almacenar los valores de Pend Norm mov
        val peaks = arrayOfNulls<Double>(data.size)     //Arreglo para almacenar la ocurrencia de picos
        val start = arrayOfNulls<Double>(data.size)     //Arreglo para almacenar la ocurrencia de start
        val startMem = arrayOfNulls<Double>(data.size)  //Arreglo para guardar los valores de Start Memory
        val startTime = arrayOfNulls<Long>(data.size)    //Arreglo para guardar los valores de Start Time
        val endMem = arrayOfNulls<Double>(data.size)    //Arreglo para guardar los valores de End Memory
        val endTime = arrayOfNulls<Long>(data.size)      //Arreglo para guardar los valores de End Time
        val cuff = arrayOfNulls<Double>(data.size)      //Arreglo donde se vayan registrando los Peak cuff
        val amp = arrayOfNulls<Double>(data.size)       //Arreglo donde se vayan registrando las amplitudes de los picos
        val systolic = arrayOfNulls<Double>(data.size)  //Arreglo para guardar los cálculos de presión sistólica
        val diastolic = arrayOfNulls<Double>(data.size) //Arreglo para guardar los cálculos de presión diastólica

        //Cálculo de primeros valores filtrados de mmHg
        fixed[0] = data[0].mmHg.toDouble()
        fixed[1] = data[1].mmHg.toDouble()

//        mSeries.appendData(DataPoint(data[0].timer.toDouble(), fixed[0]!!), false, 1000)
//        mSeries.appendData(DataPoint(data[1].timer.toDouble(), fixed[1]!!), false, 1000)

        entries.add(Entry(data[0].timer, fixed[0]!!.toFloat()))
        entries.add(Entry(data[1].timer, fixed[1]!!.toFloat()))
        val dataSet = LineDataSet(entries, resources.getString(R.string.chart_label)) // add entries to dataset
        dataSet.color = resources.getColor(R.color.colorButton)
        dataSet.setDrawCircles(false)

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.notifyDataSetChanged() // let the chart know it's data changed
        chart.invalidate() // refresh chart


        //Primer recorrido
        for(i in 2 until data.size){
            //Cálculo de los datos de presión filtrados
            if(abs(data[i].mmHg - data[i-1].mmHg) < 4){
                fixed[i] = data[i].mmHg.toDouble()
            } else if(abs(data[i-1].mmHg - data[i-2].mmHg) >= 4){
                fixed[i] = data[i].mmHg.toDouble()
            } else if(i+1>=data.size){
                fixed[i] = (data[i-1].mmHg.toDouble() + data[i+1].mmHg) / 2
            }else{
                fixed[i] = data[i-1].mmHg.toDouble()
            }

            entries.add(Entry(data[i].timer, fixed[i]!!.toFloat()))
            val dataSet = LineDataSet(entries, resources.getString(R.string.chart_label)) // add entries to dataset
            dataSet.color = resources.getColor(R.color.colorButton)
            dataSet.setDrawCircles(false)
            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.notifyDataSetChanged() // let the chart know it's data changed
            chart.invalidate() // refresh chart

            //Cálculo de mmHg mov (hasta n-5)
            fixedSum += fixed[i]!!
            if(i>7){
                mov[i-4] = fixedSum/9.0
                fixedSum -= fixed[i-8]!!

                //Cálculo de Pend
                if(i>8){
                    pend[i-4] = mov[i-4]!! - mov[i-5]!!
                    pendSum += pend[i-4]!!
                }
            }
        }

        //Cálculo para las últimas 4 posiciones de mmHg mov
        for(i in data.size-4 until data.size){
            mov[i] = fixedSum/(data.size-i+4)
            pend[i] = mov[i]!! - mov[i-1]!!

            //Cálculo de Pend
            pendSum += pend[i-4]!!
            fixedSum -= fixed[i-4]!!
        }

        val avgPend = pendSum / (data.size-5)   //Promedio de pendiente
        var sumPendN = 0.0                      //Variable que se usará para el cálculo de Pend Norm mov

        //Inicializar valores de Start en 0
        startMem[0] = 0.0
        startTime[0] = 0

        //Cálculo de pendiente normalizada
        for(i in 5 until data.size){
            pendN[i] = pend[i]!! - avgPend
            sumPendN += pendN[i]!!

            //Cálculo de pendiente normal mov
            if(i>12){
                pendNMov[i-4] = sumPendN/9
                sumPendN -= pendN[i-8]!!
            }

            if(i>13){
                //Cálculo de Peaks
                if(pendNMov[i-4]!! < pendNMov[i-5]!! && pendNMov[i-4]!! * pendNMov[i-5]!! < 0){
                    peaks[i-4] = mov[i-4]
                }

                //Cálculo de start
                if(pendNMov[i-4]!! > pendNMov[i-5]!! && pendNMov[i-5]!! * pendNMov[i-4]!! < 0){
                    start[i-4] = mov[i-4]
                }
            }

            //Cálculo de start memory y start time
            if(start[i-4]!=null){
                startMem[i-4] = start[i-4]
                startTime[i-4] = data[i-4].timer.toLong()
            } else{
                startMem[i-4] = startMem[i-5]
                startTime[i-4] = startTime[i-5]
            }
        }

        //Cálculo de últimas 4 posiciones para la pendiente normal mov
        for(i in data.size-4 until data.size){
            pendNMov[i] = sumPendN/(data.size-i+4)

            sumPendN -= pendN[i-4]!!

            //Cálculo de Peaks
            if(pendNMov[i]!! < pendNMov[i-1]!! && pendNMov[i-1]!! * pendNMov[i]!! < 0){
                peaks[i] = mov[i]
            }

            //Cálculo de start
            if(pendNMov[i]!! > pendNMov[i-1]!! && pendNMov[i-1]!! * pendNMov[i]!! < 0){
                start[i] = mov[i]
            }

            //Cálculo de start memory y start time
            if(start[i]!=null){
                startMem[i] = start[i]
                startTime[i] = data[i].timer.toLong()
            } else{
                startMem[i] = startMem[i-1]
                startTime[i] = startTime[i-1]
            }
        }

        //Inicializar valores de las últimas casillas para end memory y end time
        endMem[data.size-1] = 0.0
        endTime[data.size-1] = 0

        //Declarar una variable para calcular el máximo de un arreglo de amplitudes
        var ampMax = 0.0

        //Recorrido a la inversa para End memory y End Time
        for(i in data.size-2 downTo 5){
            if(start[i]!=null){
                endMem[i] = startMem[i]
                endTime[i] = data[i].timer.toLong()
            }else{
                endMem[i] = endMem[i+1]
                endTime[i] = endTime[i+1]
            }

            //Cálculo de Peak cuff y Peak Amplitude
            if(peaks[i]!=null){
                cuff[i]	= ((endMem[i]!! - startMem[i]!!)/(endTime[i]!! - startTime[i]!!))*(data[i].timer.toInt() - startTime[i]!!) + startMem[i]!!
                amp[i] = peaks[i]!! - cuff[i]!!

                //Encontrar la amplitud máxima
                if(amp[i]!! > ampMax){
                    ampMax = amp[i]!!
                }
            }
        }

        //Inicializar las ultimas casillas de systolic y diastolic en 0.0
        systolic[data.size - 1] = 0.0
        diastolic[data.size - 1] = 0.0

        //Recorrido a la inversa para el cálculo de Systolic y Diastolic
        for(i in data.size-2 downTo 0){
            if(amp[i] != null && amp[i]!! >= 0.5*ampMax){
                systolic[i] = peaks[i]
            } else{
                systolic[i] = systolic[i+1]
            }

            if(amp[i] != null && amp[i]!! >= 0.8*ampMax && diastolic[i+1]==0.0){
                diastolic[i] = peaks[i]
            } else{
                diastolic[i] = diastolic[i+1]
            }
        }

        systolicRes = systolic[0]!!
        diastolicRes = diastolic[0]!!

        val results = systolic[0]!!.toInt().toString() + " / " + diastolic[0]!!.toInt().toString()

        tv_device_results.text = results
    }

    private fun verifyNurseMode () {
        var examplePrefs = getSharedPreferences("Shared", 0)
        var shared = examplePrefs.getBoolean("Shared", false)
        if (shared) {
            tv_device_results_title.visibility = View.GONE
            tv_device_results.visibility = View.GONE
            tv_device_diastolic.visibility = View.GONE
            tv_device_systolic.visibility = View.GONE
            divider2.visibility = View.GONE
//            time_graph.visibility = View.INVISIBLE
        }
    }

    fun toString(date: Date?): String? {
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    fun toByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
        return outputStream.toByteArray()
    }

    override fun fetchComplete() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
