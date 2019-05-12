
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
import android.view.WindowManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


// Configuration activity declaration and view inflation
class PerfilActivity : AppCompatActivity() {
    lateinit var instanceDatabase: MedicionDatabase
    lateinit var profile: signInActivity.Companion.Profile


    // Creates the activity and inflates the view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        val queue = Volley.newRequestQueue(this)
        val extras = intent.extras?: return
        this.title = "Perfil"
        profile = extras.getParcelable(PatientList.ACCOUNT)!!
        perfil_nombre.text = profile.name
        val url = "https://heart-app-tec.herokuapp.com/patients/" + profile.mail
        val jRequest =  StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    var json = JSONObject(response)
                    perfil_genero.text = json.get("sex").toString()
                    perfil_edad.text = json.get("age").toString()
                },
                Response.ErrorListener { error->
                    Toast.makeText(applicationContext,"No existes en la base de datos.", Toast.LENGTH_SHORT).show()
                })
        jRequest.tag = "Load"
        queue.add(jRequest)
        createQRCode(profile.mail)
    }

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

    // Handles clicking the back button
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
