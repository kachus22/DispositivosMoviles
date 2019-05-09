package mx.itesm.proyectofinal

import Database.MedicionDatabase
import NetworkUtility.NetworkConnection
import NetworkUtility.NetworkConnection.Companion
import NetworkUtility.NetworkConnection.Companion.buildStringAccount
import NetworkUtility.OkHttpRequest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.synthetic.main.activity_sign_in.*
import android.content.Intent
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.parcel.Parcelize
import me.rohanjahagirdar.outofeden.Utils.FetchCompleteListener
import mx.itesm.proyectofinal.PatientList.Companion.ACCOUNT
import mx.itesm.proyectofinal.PatientList.Companion.ACCOUNT_TYPE
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class signInActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, FetchCompleteListener {

    private lateinit var detailsJSON: JSONObject
    lateinit var profile: Profile
    lateinit var tipo: String
    private val RC_SIGN_IN = 9001
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mGoogleSignInClient : GoogleSignInClient? = null
    lateinit var sharedPreference:SharedPreference


    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d("CONNECTION_FAILED", "onConnectionFailed: $p0")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference=SharedPreference(this)
        setContentView(R.layout.activity_sign_in)
        //btnLogin = findViewById(R.id.btnLogin)
        //btnLogout = findViewById(R.id.btnLogout)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val extras = intent.extras?: return
        tipo = extras.getString(ElegirTipo.TYPE)!!
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        /*mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()*/
        btnLogin.setOnClickListener{ signin() }
    }

    override fun onStart() {
        super.onStart()
        PatientList.ACTIV = "sign"
        if(PatientList.STATUS == "si") {
            signOut()
        }else {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            updateUILogged(account)
        }
    }

    private fun signOut() {
        mGoogleSignInClient?.signOut()
                ?.addOnCompleteListener(this) {
                    // ...
                }
    }

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

    fun updateUI(account: GoogleSignInAccount?){
        if(account!=null){
            setContentView(R.layout.activity_loading_acc)
            val mail = account.email
            val name = account.displayName
            val imgUrl = account.photoUrl.toString()
            profile = Profile(mail!!, name!!, imgUrl)
            checkUser(mail, name)
        }
    }

    fun updateUILogged(account: GoogleSignInAccount?){
        if(account!=null){
            val mail = account.email
            val name = account.displayName
            val imgUrl = account.photoUrl.toString()
            profile = Profile(mail!!, name!!, imgUrl)
            fetchComplete()
        }
    }

    fun checkUser(email: String, name: String){
        var client = OkHttpClient()
        var request= OkHttpRequest(client)
        val url = buildStringAccount()
        val map: HashMap<String, Any> = hashMapOf("name" to profile.name, "email" to profile.mail)

        request.POST(url, map, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread{
                    try {
                        var json = JSONObject(responseData)
                        detailsJSON = json
                        fetchComplete()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("FAILURE", "REQUEST FAILURE")
            }
        })
    }

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
        finish()
    }
    companion object {
        // Data class. An ArrayList of this type is sent to ResultsActivity
        @Parcelize
        data class Profile(var mail: String, var name: String, var img: String) : Parcelable
    }
}