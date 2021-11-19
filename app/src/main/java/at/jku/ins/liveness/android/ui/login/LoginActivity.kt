package at.jku.ins.liveness.android.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.ui.main.MainActivity
import at.jku.ins.liveness.android.databinding.ActivityLoginBinding
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security


class LoginActivity : AppCompatActivity() {

    //private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    companion object {
        init {
            Security.removeProvider("BC")
            // Confirm that positioning this provider at the end works for your needs!
            Security.addProvider(BouncyCastleProvider())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val server = binding.server
        val appPassword = binding.appPassword
        val signalPassword = binding.signalPassword
        val start = binding.login
        start.isEnabled = false
        //val loading = binding.loading

        /*loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)*/

        // initialize with defaults from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.getString("server", "").also { server.setText(it.toString()) }

        /*loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login buttonVerify unless both server / appPassword is valid
            start.isEnabled = loginState.isDataValid

            /*if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }*/
        })*/

        /*loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            //loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })*/

        server.afterTextChanged {
            /*loginViewModel.loginDataChanged(
                server.text.toString(),
                appPassword.text.toString()
            )*/
        }

        signalPassword.apply {
            afterTextChanged {
                /*loginViewModel.loginDataChanged(
                    server.text.toString(),
                    appPassword.text.toString()
                )*/
            }

            /*setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            server.text.toString(),
                            appPassword.text.toString()
                        )
                }
                false
            }*/

            start.isEnabled = true
            start.setOnClickListener {
                //loading.visibility = View.VISIBLE
                //loginViewModel.login(server.text.toString(), appPassword.text.toString())

                startMain()
            }
        }
    }

    private fun startMain(/*model: ProtocolRunData*/) {
        //val displayName = model.displayName

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.intentParamAppPassword, binding.appPassword.text.toString())
        intent.putExtra(Constants.intentParamSignalPassword, binding.signalPassword.text.toString())
        startActivity(intent)
    }

    /*private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }*/
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}