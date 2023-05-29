package at.jku.ins.liveness.android.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import at.jku.ins.liveness.android.R
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository
import at.jku.ins.liveness.android.ui.main.MainActivity
import at.jku.ins.liveness.android.databinding.ActivityLoginBinding
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var biometricPrompt: BiometricPrompt

    private lateinit var data: ProtocolRunDataRepository

    private val cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            Constants.BIOMETRIC_PREFERENCES,
            Context.MODE_PRIVATE,
            Constants.CIPHERTEXT_WRAPPER
        )

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

        // initialize our runtime data singleton
        data = ProtocolRunDataRepository.getInstance(this.applicationContext)

        val server = binding.server
        val appPassword = binding.appPassword
        val biometricButton = binding.biometricButton
        val signalPassword = binding.signalPassword
        val start = binding.login
        start.isEnabled = false

        // populate the server field from stored preferences (if stored before)
        data.server.value.also {
            if (! it.isNullOrEmpty()) {
                Log.d(Constants.LOG_TAG, "Found previously set serverUrl: $it")
                server.setText(it)
            }
        }
        // and store changed data as input by user
        server.afterTextChanged {
            data.updateServer(server.text.toString())
        }

        // TODO: load appPassword if it has been stored locally or unlock with biometrics
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.biometricButton.visibility = View.VISIBLE
            binding.biometricButton.setOnClickListener {
                if (ciphertextWrapper != null) {
                    showBiometricPromptForDecryption()
                } /*else {
                    startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
                }*/
            }
        } else {
            binding.biometricButton.visibility = View.INVISIBLE
        }
        /*if (ciphertextWrapper == null) {
            setupForLoginWithPassword()
        }*/

        appPassword.afterTextChanged {
            data.updateAppPassword(appPassword.text.toString())
        }

        // signal password needs to be entered on every app start and is never cached
        signalPassword.apply {
            afterTextChanged {
                data.updateSignalPassword(signalPassword.text.toString())
                start.isEnabled = true
            }

            /*setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                }
                false
            }*/
        }

        start.setOnClickListener {
            startMain()
        }
    }

    /**
     * The logic is kept inside onResume instead of onCreate so that authorizing biometrics takes
     * immediate effect.
     */
    override fun onResume() {
        super.onResume()

        if (ciphertextWrapper != null) {
            if (binding.appPassword.text.isNullOrEmpty()) {
                showBiometricPromptForDecryption()
            } else {
                // app password still provided manually, so ignore the prompt
            }
        }
    }

    // code from https://developer.android.com/codelabs/biometric-login#2
    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )
            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this,
                    ::decryptServerTokenFromStorage
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                binding.appPassword.setText(plaintext)
            }
        }
    }

    private fun startMain(/*model: ProtocolRunData*/) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
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