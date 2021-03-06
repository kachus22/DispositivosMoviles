package mx.itesm.proyectofinal

import NetworkUtility.NetworkConnection
import NetworkUtility.NetworkConnection.Companion.buildStringAccount
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.activity_sign_in.*
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.parcel.Parcelize
import me.rohanjahagirdar.outofeden.Utils.FetchCompleteListener
import mx.itesm.proyectofinal.PatientList.Companion.ACCOUNT
import mx.itesm.proyectofinal.PatientList.Companion.ACCOUNT_TYPE
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject


class signInActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, FetchCompleteListener {

    lateinit var profile: Profile
    lateinit var tipo: String
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient : GoogleSignInClient? = null
    lateinit var sharedPreference:SharedPreference
    lateinit var queue: RequestQueue



    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d("CONNECTION_FAILED", "onConnectionFailed: $p0")
    }

    // Creates the activity, and makes the request to get your account
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference=SharedPreference(this)
        setContentView(R.layout.activity_sign_in)
        queue = Volley.newRequestQueue(this)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val extras = intent.extras?: return
        tipo = extras.getString(ElegirTipo.TYPE)!!
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        btnLogin.setOnClickListener{ signin() }
    }

    /*
     * When the activity starts it will check if you want to sign out or you are already signed in
     */
    override fun onStart() {
        super.onStart()
        PatientList.ACTIV = "sign"
        if(PatientList.STATUS == "si") {
            signOut()
            PatientList.STATUS == "no"
        }else {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            updateUILogged(account)
        }
    }

    // Signs you out of your account
    private fun signOut() {
        mGoogleSignInClient?.signOut()
                ?.addOnCompleteListener(this) {
                    // ...
                }
    }

    // Handles the result of signin() and calls updateui with your account
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            Log.w("SIGNIN_EXCEPTION", "failed code: " + e.statusCode)
            updateUI(null)
        }
    }

    //Calls google api and signs you in
    fun signin(){
        if (NetworkConnection.isNetworkConnected(this)) {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            // alerta usando la librería de ANKO
            alert(message = resources.getString(R.string.internet_no_desc), title = resources.getString(R.string.internet_no_title)) {
                okButton {  }
            }.show()
        }
    }

    //Result for activity google signin client
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    //Saves your account in a profile object
    fun updateUI(account: GoogleSignInAccount?){
        if(account!=null){
            setContentView(R.layout.activity_loading_acc)
            val mail = account.email
            val name = account.displayName
            val imgUrl = account.photoUrl.toString()
            profile = Profile(mail!!, name!!, imgUrl)
            checkUser()
        }
    }

    //Saves your account in a profile object when you are already logged in
    fun updateUILogged(account: GoogleSignInAccount?){
        if(account!=null){
            val mail = account.email
            val name = account.displayName
            val imgUrl = account.photoUrl.toString()
            profile = Profile(mail!!, name!!, imgUrl)
            fetchComplete()
        }
    }

    /**
     * Function to check if the user exists on the database.
     * In case it doesn't it adds it.
     */
    fun checkUser(){
        val url = buildStringAccount()
        val map: HashMap<String, String> = hashMapOf("name" to profile.name, "email" to profile.mail)
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

    //Starts desired activity
    override fun fetchComplete() {
        lateinit var startAppIntent:Intent
        val TIPO_USUARIO = "TIPO_USUARIO"
        PatientList.STATUS = "no"
        when(tipo){
            "clinica"->{
                sharedPreference.save(TIPO_USUARIO,"clinica")
                sharedPreference.save("ACCOUNT",profile)
                startAppIntent = Intent(this,Clinic_list::class.java)
                startAppIntent.putExtra(ACCOUNT_TYPE, 0)
            }
            "paciente"->{
                sharedPreference.save(TIPO_USUARIO,"paciente")
                sharedPreference.save("ACCOUNT",profile)
                startAppIntent = Intent(this,PatientList::class.java)
                startAppIntent.putExtra(ACCOUNT_TYPE, 1)
            }

        }
        startAppIntent.putExtra(ACCOUNT, profile)
        startActivity(startAppIntent)
    }
    companion object {
        // Data class. An ArrayList of this type is sent to ResultsActivity
        @Parcelize
        data class Profile(var mail: String, var name: String, var img: String) : Parcelable
    }
}