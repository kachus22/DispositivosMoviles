package mx.itesm.proyectofinal


import android.content.Context
import android.content.SharedPreferences


class SharedPreference(val context: Context) {
    private val PREFS_NAME = "Preferences"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Series of functions that are used to save values in the app data, so they stay until changed
     * or erased, even if the app is restarted.
     */

    /**
     * Function to save objects of type profile, defined on signInActivity
     */
    fun save(KEY_NAME: String, value: signInActivity.Companion.Profile) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME+"NAME", value.name)
        editor.putString(KEY_NAME+"MAIL", value.mail)
        editor.putString(KEY_NAME+"IMG", value.img)

        editor.commit()
    }

    /**
     * Function to save strings in the data
     */
    fun save(KEY_NAME: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, value)

        editor.commit()
    }

    /**
     * Function to retrieve an object of type profilef rom the shared preferences
     */
    fun getValueProfile(KEY_NAME: String): signInActivity.Companion.Profile? {

        return signInActivity.Companion.Profile(sharedPref.getString(KEY_NAME+"MAIL", null),sharedPref.getString(KEY_NAME+"NAME", null),sharedPref.getString(KEY_NAME+"IMG", null))
    }

    /**
     * Function to retrieve an string from the shared preferences
     */
    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, null)
    }


    /**
     * Function to clear all the saved data
     */
    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        editor.clear()
        editor.commit()
    }

}

