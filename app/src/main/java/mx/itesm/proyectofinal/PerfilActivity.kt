
package mx.itesm.proyectofinal

import Database.MedicionDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_perfil.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.google.zxing.WriterException
import android.util.Log
import android.content.Context
import android.graphics.Point
import android.text.method.KeyListener
import android.view.Menu
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_patient_list.*
import org.json.JSONObject
import android.widget.RadioGroup
import com.android.volley.RequestQueue


// Configuration activity declaration and view inflation
class PerfilActivity : AppCompatActivity() {
    lateinit var instanceDatabase: MedicionDatabase
    lateinit var profile: signInActivity.Companion.Profile
    lateinit var clinic: String
    lateinit var queue: RequestQueue

    // Creates the activity, inflates the view and makes a request to the api to load user data
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        floatingActionButtonSave.show()


        this.queue = Volley.newRequestQueue(this)

        val extras = intent.extras?: return
        this.title = "Perfil"
        this.profile = extras.getParcelable(PatientList.ACCOUNT)!!

        perfil_nombre.setText(profile.name, TextView.BufferType.EDITABLE)

        val url = "https://heart-app-tec.herokuapp.com/patients/" + profile.mail
        val jRequest =  StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    var json = JSONObject(response)
                    val genero = json.get("sex").toString()
                    when(genero){
                        "M"->{
                            radiobuttons.check(R.id.but_H)
                        }
                        "F"->{
                            radiobuttons.check(R.id.but_M)
                        }
                    }
                    perfil_edad.setText(json.get("age").toString(),TextView.BufferType.EDITABLE)
                },
                Response.ErrorListener { error->
                    Toast.makeText(applicationContext,"No existes en la base de datos.", Toast.LENGTH_SHORT).show()
                })
        jRequest.tag = "Load"
        this.queue.add(jRequest)

        //Si viene de la actividad clinica se deshabilitan
        if(PatientList.ACTIV == "clinic"){
            perfil_nombre.isEnabled = false
            perfil_edad.isEnabled = false
            but_H.isClickable = false
            but_M.isClickable = false
            floatingActionButtonSave.hide()
        }
        else{
            but_H.setOnClickListener {
                val check = findViewById<RadioButton>(R.id.but_M)
                check.isChecked = false
            }
            but_M.setOnClickListener {
                val check = findViewById<RadioButton>(R.id.but_H)
                check.isChecked = false
            }
        }

        createQRCode(this.profile.mail)
        floatingActionButtonSave.setOnClickListener { sendRequest() }
    }


    /**
     * Creates QR code that contains the email
     **/
    fun createQRCode(email:String){
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.getDefaultDisplay()
        val point = Point()
        display.getSize(point)
        val width = point.x
        val height = point.y
        var smallerDimension = if (width < height) width else height
        val qrgEncoder = QRGEncoder(email, null, QRGContents.Type.TEXT, smallerDimension)
        try {
            // Getting QR-Code as Bitmap
            val bitmap = qrgEncoder.encodeAsBitmap()
            // Setting Bitmap to ImageView
            perfil_qr.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.v("GenerateQRCode", e.toString())
        }
    }

    /**
     * Sends request to server to modify the data
     **/
    fun sendRequest(){
        val url = "https://heart-app-tec.herokuapp.com/patients/" + profile.mail
        var se:String
        if(but_H.isChecked){
            se = "M"
        }
        else{
            se = "F"
        }
        val map: HashMap<String, Any?> = hashMapOf("name" to perfil_nombre.text.toString(),
                "age" to perfil_edad.text.toString().toInt(), "sex" to se, "clinic" to null)
        println(JSONObject(map).toString())
        val jRequest =  JsonObjectRequest(Request.Method.POST, url, JSONObject(map),
                Response.Listener<JSONObject> { response ->
                    // Display the first 500 characters of the response string.
                    var json = response
                    Toast.makeText(applicationContext,"Datos guardados exitosamente.", Toast.LENGTH_SHORT).show()
                },
                Response.ErrorListener { error->
                    Toast.makeText(applicationContext,"No se pudo guardar los datos.", Toast.LENGTH_SHORT).show()
                })

        this.queue.add(jRequest)
    }

    // Handles clicking the back button and edit profile button
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                this.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Handles clicking the back button
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
