package NetworkUtility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.io.IOException
import java.net.URL

class NetworkConnection {


    companion object {
        const val BASE_URL = "https://heart-app-tec.herokuapp.com"

        /**
         * Functions to build the urls of the web service, each one for a different purpose
         */
        fun buildStringAccount(): String = "$BASE_URL/account/"

        fun buildStringClinicPatients(clinic: String): String = "$BASE_URL/clinics/$clinic/"

        fun buildStringPatients(mail: String): String = "$BASE_URL/patients/$mail"

        fun buildStringPatientsPressures(patient: String): String = "$BASE_URL/patients/$patient/pressures"

        fun buildStringPressures(): String = "$BASE_URL/pressures/"

        fun buildUrlPatients(clinic : String): URL =
                //http://www.mocky.io/v2/5cc4d8a53400002c0076559c
                //$BASE_URL/patients/?format=json
                URL("$BASE_URL/clinics/$clinic?format=json")

        fun buildUrlPressures(patient : String): URL =
                //http://www.mocky.io/v2/5cc3b67a3400003700765452
                //$BASE_URL/pressures/?format=json
            URL("$BASE_URL/patients/$patient/pressures?format=json")


        /**
         * Function that get response of an URL
         */
        fun getResponseFromHttpUrl(url: URL): String =

                try {
                    //readText() does have an internal limit of 2 GB file size.
                    url.readText()
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw IOException("Not connected")
                }

        /**
         * Function that checks if the device has a correct network connection
         */
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager: ConnectivityManager = context.getSystemService(
                    Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            return networkInfo?.isConnectedOrConnecting ?: false
        }
    }
}