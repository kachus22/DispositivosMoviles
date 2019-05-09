
package mx.itesm.proyectofinal

import Database.MedicionDatabase
import Database.ioThread
import NetworkUtility.OkHttpRequest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_perfil.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.google.zxing.WriterException
import android.R.attr.bitmap
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.R.attr.y
import android.R.attr.x
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.view.Display
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_clinic_list.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


// Configuration activity declaration and view inflation
class PerfilActivity : AppCompatActivity() {
    lateinit var instanceDatabase: MedicionDatabase
    lateinit var profile: signInActivity.Companion.Profile


    // Creates the activity and inflates the view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        val extras = intent.extras?: return
        this.title = "Perfil"
        profile = extras.getParcelable(PatientList.ACCOUNT)!!
        perfil_nombre.text = profile.name
        instanceDatabase = MedicionDatabase.getInstance(this)
        ioThread {
            var client = OkHttpClient()
            var request= OkHttpRequest(client)
            val url = "https://heart-app-tec.herokuapp.com/patients/" + profile.mail
            request.GET(url, object: Callback {
                override fun onResponse(call: Call?, response: Response) {
                    println(response.toString())
                    val responseData = response.body()?.string()
                    runOnUiThread {
                        try {
                            var json = JSONObject(responseData)
                            perfil_genero.text = json.get("sex").toString()
                            perfil_edad.text = json.get("age").toString()

                        } catch (e: JSONException) {
                            Toast.makeText(applicationContext,"No existes en la base de datos.", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }

                }

                override fun onFailure(call: Call?, e: IOException?) {
                    Log.d("FAILURE", "REQUEST FAILURE")
                }
            })
        }
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
