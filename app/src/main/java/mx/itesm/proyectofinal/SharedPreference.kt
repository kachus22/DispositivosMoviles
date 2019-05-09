package mx.itesm.proyectofinal


import android.content.Context
import android.content.SharedPreferences


class SharedPreference(val context: Context) {
    private val PREFS_NAME = "Preferences"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun save(KEY_NAME: String, value: signInActivity.Companion.Profile) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME+"NAME", value.name)
        editor.putString(KEY_NAME+"MAIL", value.mail)
        editor.putString(KEY_NAME+"IMG", value.img)

        editor.commit()
    }


    fun save(KEY_NAME: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, value)

        editor.commit()
    }

    fun getValueProfile(KEY_NAME: String): signInActivity.Companion.Profile? {

        return signInActivity.Companion.Profile(sharedPref.getString(KEY_NAME+"MAIL", null),sharedPref.getString(KEY_NAME+"NAME", null),sharedPref.getString(KEY_NAME+"IMG", null))
    }

    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, null)
    }

    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        editor.clear()
        editor.commit()
    }

}

