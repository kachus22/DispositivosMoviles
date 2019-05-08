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

package Database

import NetworkUtility.NetworkConnection
import android.arch.persistence.room.*
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.sql.Date

/*
 * Declares the measurement data that is stored in the database
 */
@Parcelize
@Entity(tableName = "Medicion")
data class Medicion(@ColumnInfo(name = "appSistolica") var appSistolica: String?,
                    @ColumnInfo(name = "appDiastolica") var appDiastolica: String?,
                    @ColumnInfo(name = "manSistolica") var manSistolica: String?,
                    @ColumnInfo(name = "manDiastolica") var manDiastolica: String?,
                    @ColumnInfo(name = "fecha") var fecha: String?,
                    @ColumnInfo(name = "verificado") var verificado: Boolean?,
                    @ColumnInfo(name ="brazo") var brazo: String?,
                    @ColumnInfo(name = "grafica") var grafica: ByteArray?,
                    @ColumnInfo(name = "iniciales") var iniciales: String?): Parcelable {

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    var _id:Int = 0

    companion object {
        fun populateMeds(context: Context, patientP : String) : MutableList<Medicion> = cargaMeds(patientP)
        fun populatePatients(context: Context, clinicM : String): MutableList<Patient> = cargaPatients(clinicM)
    }
}

/*
@Parcelize
@Entity(tableName = "Clinics")
data class Clinica(@ColumnInfo(name = "email") var mailC: String?,
                    @ColumnInfo(name = "name") var nameC: String?): Parcelable {

    @ColumnInfo(name = "_idC")
    @PrimaryKey(autoGenerate = true)
    var _idC:Int = 0
}
*/

@Parcelize
@Entity(tableName = "Patient")
data class Patient(@ColumnInfo(name = "email") var mailC: String?,
                   @ColumnInfo(name = "Fname") var FNameP: String?,
                   @ColumnInfo(name = "LName") var LNameP: String?,
                   @ColumnInfo(name = "age") var ageP: Int?,
                   @ColumnInfo(name = "sex") var sexP: String?,
                   @ColumnInfo(name = "clinic") var clinicP: String?): Parcelable {

    @ColumnInfo(name = "_idP")
    @PrimaryKey(autoGenerate = true)
    var _idP:Int = 0
}
/*
    {
    "id": 1,
    "date": "2019-04-27",
    "verified": true,
    "systolic": 93,
    "diastolic": 82,
    "manual_systolic": 93,
    "manual_diastolic": 82,
    "arm": "D",
    "patient": {
        "id": 1,
        "email": "A01251806@itesm.mx",
        "first_name": "Alejandra",
        "last_name": "Gamez",
        "age": 78,
        "sex": "F"
    }
}
*/

fun cargaMeds(patientMail: String) : MutableList<Medicion> {
    val url = NetworkConnection.buildUrlPressures(patientMail)
    var dataJson : String?
    var meds : MutableList<Medicion> = mutableListOf()
    try{
        dataJson = NetworkConnection.getResponseFromHttpUrl(url)
    } catch (e : Exception){
        return meds
    }

    return parseJsonMeds(dataJson!!)
}

/*
        "id": 1,
        "email": "rodrigocampos@example.net",
        "first_name": "Elvia",
        "last_name": "Modesto",
        "age": 13,
        "sex": "F"
 */
fun cargaPatients(clinicaMail : String) : MutableList<Patient>{
    val urlP = NetworkConnection.buildUrlPatients(clinicaMail)
    var dataJsonP : String?
    var patients : MutableList<Patient> = mutableListOf()
    try{
        dataJsonP = NetworkConnection.getResponseFromHttpUrl(urlP)
    }catch (e : Exception){
        return patients
    }

    return parseJsonPats(dataJsonP!!, clinicaMail)
}

fun parseJsonMeds(jsonString: String): MutableList<Medicion>{
    var pressures : MutableList<Medicion> = mutableListOf()
    var press : Medicion
    //Primero es array
    try {
        val dataJsonList : JSONArray = JSONArray(jsonString)
        for(i in 0 until dataJsonList.length()){
            val jsonMed : JSONObject = dataJsonList.getJSONObject(i)
            val dateMed = jsonMed.getString("date")
            val verMed = jsonMed.getBoolean("verified")
            val sysMed = jsonMed.getInt("systolic").toString()
            val disMed = jsonMed.getInt("diastolic").toString()
            val m_sysMed = jsonMed.getInt("manual_systolic").toString()
            val m_disMed = jsonMed.getInt("manual_diastolic").toString()
            val armMedMed = jsonMed.getString("arm")
            //patient object
            val patientMed = jsonMed.getJSONObject("patient")
            val patientMedMail = patientMed.getString("email").toString().toLowerCase()

            press = Medicion(sysMed,disMed,m_sysMed,m_disMed,dateMed,verMed,armMedMed,null,patientMedMail)
            pressures.add(press)
        }
    }catch (e: JSONException) {
        e.printStackTrace()
        throw IOException("JSONException")
    }
    return pressures
}

fun parseJsonPats(jsonString: String, clinicPat : String): MutableList<Patient>{
    var patients : MutableList<Patient> = mutableListOf()
    var pat : Patient
    //Primero es array
    try {
        val dataJsonList : JSONArray = JSONArray(jsonString)
        for(i in 0 until dataJsonList.length()){
            val jsonPat : JSONObject = dataJsonList.getJSONObject(i)
            val patMail = jsonPat.getString("email")
            val patFname = jsonPat.getString("first_name")
            val patLname = jsonPat.getString("last_name")
            val patAge = jsonPat.getInt("age")
            val patSex = jsonPat.getString("sex")

            pat = Patient(patMail,patFname,patLname,patAge,patSex,clinicPat)
            patients.add(pat)
        }
    }catch (e: JSONException) {
        e.printStackTrace()
        throw IOException("JSONException")
    }
    return patients
}