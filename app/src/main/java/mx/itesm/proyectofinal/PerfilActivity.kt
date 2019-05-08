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

import Database.MedicionDatabase
import Database.ioThread
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_configuration.*
import kotlinx.android.synthetic.main.activity_perfil.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
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
import android.view.WindowManager






// Configuration activity declaration and view inflation
class PerfilActivity : AppCompatActivity() {
    lateinit var instanceDatabase: MedicionDatabase


    // Creates the activity and inflates the view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        val extras = intent.extras?: return
        this.title = "Perfil"
        val nombre = extras.getString(PatientList.ACCOUNT_NAME)
        val email = extras.getString(PatientList.ACCOUNT_MAIL)
        perfil_nombre.text = nombre
        instanceDatabase = MedicionDatabase.getInstance(this)
        ioThread {
            val paciente = instanceDatabase.pacienteDao().cargarPacientePorEmail(email)
            runOnUiThread {
                if(paciente != null){
                    perfil_genero.text = paciente.sexP
                    perfil_edad.text = paciente.ageP.toString()
                }
                else
                    Toast.makeText(applicationContext,"No existes en la base de datos.", Toast.LENGTH_SHORT).show()
            }
        }
        createQRCode(email)

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
