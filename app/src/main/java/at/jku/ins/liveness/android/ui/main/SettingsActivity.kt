package at.jku.ins.liveness.android.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.preference.*
import at.jku.ins.liveness.android.R
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository
import at.jku.ins.liveness.android.ui.login.BiometricPromptUtils
import at.jku.ins.liveness.android.ui.login.CryptographyManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var cryptographyManager: CryptographyManager

    private val data: ProtocolRunDataRepository = ProtocolRunDataRepository.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        // TODO: might want to enable again
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // register a preferences listener: if the biometrics preference is turned on, show the prompt to store
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            Log.d(Constants.LOG_TAG, "Preferences changed: $prefs = '$key'")
            if (key.equals(Constants.biometricPreference)) {
                Log.d(Constants.LOG_TAG, "Biometrics prompt now set to: " + prefs.getBoolean(Constants.biometricPreference, false))
                if (prefs.getBoolean(Constants.biometricPreference, false))
                    showBiometricPromptForEncryption()
            }
        }
        PreferenceManager.getDefaultSharedPreferences(applicationContext).registerOnSharedPreferenceChangeListener(listener)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // if we have biometrics available, allow to set the preference item
            context?.let {
                val canAuthenticate = (BiometricManager.from(it).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS)
                findPreference<SwitchPreferenceCompat>(Constants.biometricPreference)?.setEnabled(canAuthenticate)
            }
        }
    }

    private fun showBiometricPromptForEncryption() {
        Log.i(Constants.LOG_TAG, "Trying to enable biometric prompt")
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            val token = data.appPassword.value
            if (! token.isNullOrEmpty()) {
                Log.d(Constants.LOG_TAG, "The appPassword to store is $token")
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    Constants.BIOMETRIC_PREFERENCES,
                    Context.MODE_PRIVATE,
                    Constants.CIPHERTEXT_WRAPPER
                )
            }
        }
        finish()
    }

}