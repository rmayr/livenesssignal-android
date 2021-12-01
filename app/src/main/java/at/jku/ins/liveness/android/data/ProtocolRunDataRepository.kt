package at.jku.ins.liveness.android.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.signals.SignalUtils

// heavily inspired by https://github.com/googlecodelabs/android-datastore/blob/master/app/src/main/java/com/codelab/android/datastore/data/UserPreferencesRepository.kt
/** This class with a singleton instance created by @getInstance is used for sharing data items
 * between activities and protocol run implementations. Some of the data is persisted through
 * SharedPreferences, some optionally through the local keystore, and other data is only kept
 * in-memory while the app is loaded.
 */
class ProtocolRunDataRepository private constructor(context: Context) {
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        // listen for changes in shared preferences and update our protocol run data when necessary
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            Log.d(Constants.LOG_TAG, "Preferences changed: '$key'")

            if (key.equals(Constants.serverPreference))
                _server.value = sharedPreferences.getString(Constants.serverPreference, "").orEmpty()

            /*if (key.equals(Constants.initialSignalDataPreference)) {
                val signalDataPrefs = prefs.getString(Constants.initialSignalDataPreference, "")
                if (!signalDataPrefs.isNullOrEmpty()) {
                    try {
                        _initialSignalData.setValue(SignalUtils.hexStringToByteArray(signalDataPrefs))
                    } catch (e: Exception) {
                        Log.e(Constants.LOG_TAG,"Unable to parse initial signal data from preferences: $e")
                    }
                }
            }*/
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /** Internal storage for server URL. */
    private val _server = MutableLiveData<String>()
    /** This String represents the server URL to interact with. On reading, it will check
     * SharedPreferences and initialize from there. */
    val server: LiveData<String>
        get() {
            if (_server.value.isNullOrEmpty()) {
                Log.d(Constants.LOG_TAG, "serverUrl is empty while trying to get from ProtocolRunDataRepository, trying to read from preferences")
                sharedPreferences.getString(Constants.serverPreference, "").also {
                    Log.d(Constants.LOG_TAG, "Found stored $Constants.serverPreference preference: $it")
                    _server.value = it
                }
            }
            Log.d(Constants.LOG_TAG, "Returning: $_server with value ${_server.value}")
            return _server
        }
    /** Update server URL. This will both update the in-memory @_server variable in this object
     * as well as the preference value. */
    fun updateServer(s: String) {
        Log.d(Constants.LOG_TAG, "Updating serverUrl to: $s")
        // update preferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.serverPreference, s)
        editor.apply()
        // and post updates to any observers of the getter
        _server.value = s
    }

    /** Internal storage for local app password. */
    private val _appPassword = MutableLiveData<String>()
    /** This String represents the local app password used to protect local storage. */
    val appPassword: LiveData<String>
        get() {
            return _appPassword
        }
    /** Update local app password. This will update the in-memory @_appPassword variable in this
     * object and, if preferences are set accordingly, the password stored in the local
     * keystore, potentially locked with biometric authentication. */
    fun updateAppPassword(s: String) {
        // TODO: if preferences are set to store the local app password, then do so
        // and post updates to any observers of the getter
        _appPassword.value = s
    }

    /** Internal storage for shared signal password. */
    private val _signalPassword = MutableLiveData<String>()
    /** This String represents the shared signal password for deriving signal data. */
    val signalPassword: LiveData<String>
        get() {
            return _signalPassword
        }
    /** Update shared signal password. This only updates the in-memory @_signalPassword variable. */
    fun updateSignalPassword(s: String) {
        // post updates to any observers of the getter
        _signalPassword.value = s
    }

    /** Internal storage for signal encrypted key data as used by the verifier. */
    private val _verifierKeyData = MutableLiveData<ByteArray>()
    /** This ByteArray represents signal encrypted key data as used by the verifier.
     * On reading, it will check SharedPreferences and initialize from there. */
    val verifierKeyData: LiveData<ByteArray>
        get() {
            // if not yet set, ...
            if (_verifierKeyData.value == null) {
                // ... and we have a cached value set in SharedPreferences ...
                sharedPreferences.getString(Constants.verifierEncKeyDataPreference, "").also {
                    if (! it.isNullOrEmpty()) {
                        Log.d(Constants.LOG_TAG,"Found previously stored verifier key data in preferences ('$it')")
                        // ... then try to parse back into ByteArray representation
                        try {
                            _verifierKeyData.setValue(SignalUtils.hexStringToByteArray(it))
                        } catch (e: Exception) {
                            Log.e(Constants.LOG_TAG,"Unable to parse verifier key data from preferences ('$it'): $e")
                        }
                    }
                }
            }
            return _verifierKeyData
        }
    /** If forceStore is set to true, always cache in SharedPreferences as it has been freshly imported by the used. If false,
     * only cache if not yet stored in SharedPreferences.
     */
    // TODO: remove forceStore parameter and always overwrite?
    fun updateVerifierKeyData(newData: ByteArray, forceStore: Boolean) {
        // also cache in preferences - but only if not yet present (don't overwrite a previously scanned result)
        Log.d(Constants.LOG_TAG, "Currently set verifier key data: '${sharedPreferences.getString(Constants.verifierEncKeyDataPreference, "")}'")
        if (sharedPreferences.getString(Constants.verifierEncKeyDataPreference, "").isNullOrEmpty() || forceStore) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(Constants.verifierEncKeyDataPreference, SignalUtils.byteArrayToHexString(newData))
            editor.apply()
            Log.d(Constants.LOG_TAG, "Synced verifier key data to preferences")
        }
        // and post updates to any observers of the getter
        _verifierKeyData.value = newData
    }

    /** Internal storage for signal chain data as used by the verifier. */
    private val _verifierChainData = MutableLiveData<ByteArray>()
    /** This ByteArray represents signal chain data as used by the verifier.
     * On reading, it will check SharedPreferences and initialize from there. */
    val verifierChainData: LiveData<ByteArray>
        get() {
            // if not yet set, ...
            if (_verifierChainData.value == null) {
                // ... and we have a cached value set in SharedPreferences ...
                sharedPreferences.getString(Constants.verifierChainDataPreference, "").also {
                    if (! it.isNullOrEmpty()) {
                        Log.d(Constants.LOG_TAG,"Found previously stored signal verification data in preferences ('$it')")
                        // ... then try to parse back into ByteArray representation
                        try {
                            _verifierChainData.setValue(SignalUtils.hexStringToByteArray(it))
                        } catch (e: Exception) {
                            Log.e(Constants.LOG_TAG,"Unable to parse signal verification  data from preferences ('$it'): $e")
                        }
                    }
                }
            }
            return _verifierChainData
        }
    /** If forceStore is set to true, always cache in SharedPreferences as it has been freshly imported by the used. If false,
     * only cache if not yet stored in SharedPreferences.
     */
    // TODO: remove forceStore parameter and always overwrite?
    fun updateVerifierChainData(newData: ByteArray, forceStore: Boolean) {
        // also cache in preferences - but only if not yet present (don't overwrite a previously scanned result)
        Log.d(Constants.LOG_TAG, "Currently set signal verification  data: '${sharedPreferences.getString(Constants.verifierChainDataPreference, "")}'")
        if (sharedPreferences.getString(Constants.verifierChainDataPreference, "").isNullOrEmpty() || forceStore) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(Constants.verifierChainDataPreference, SignalUtils.byteArrayToHexString(newData))
            editor.apply()
            Log.d(Constants.LOG_TAG, "Synced signal verification data to preferences")
        }
        // and post updates to any observers of the getter
        _verifierChainData.value = newData
    }

    private val _verifierMaxSkipSignals = MutableLiveData<Int>()
    // TODO: make configurable through Preferences/Settings
    val verifierMaxSkipSignals = 10

    private val _proverlastSignalNumber = MutableLiveData<Int>()
    val proverLastSignalNumber: LiveData<Int>
        get() {
            return _proverlastSignalNumber
        }
    fun updateProverLastSignalNumber(i: Int) {
        // post updates to any observers of the getter
        _proverlastSignalNumber.value = i
    }

    /** Returns a current snapshot of the internally stored data values for starting a prover protocol run. */
    fun getProverProtocolRunData(): ProverProtocolRunData? =
        if (appPassword.value != null && signalPassword.value != null && server.value != null &&
            appPassword.value!!.isNotEmpty() && signalPassword.value!!.isNotEmpty() && server.value!!.isNotEmpty())
            ProverProtocolRunData(signalPassword.value!!, appPassword.value!!, server.value!!,
                proverLastSignalNumber.value)
        else
            null

    /** Returns a current snapshot of the internally stored data values for starting an initial verifier protocol run. */
    fun getInitialVerifierProtocolRunData(initialSignalData: ByteArray): VerifierInitialProtocolRunData? =
        if (appPassword.value != null && signalPassword.value != null && server.value != null &&
            appPassword.value!!.isNotEmpty() && signalPassword.value!!.isNotEmpty() && server.value!!.isNotEmpty())
            VerifierInitialProtocolRunData(signalPassword.value!!, appPassword.value!!, server.value!!,
                verifierMaxSkipSignals, initialSignalData)
        else
            null

    /** Returns a current snapshot of the internally stored data values for starting an initial verifier protocol run. */
    fun getNextVerifierProtocolRunData(): VerifierNextProtocolRunData? =
        if (appPassword.value != null && signalPassword.value != null && server.value != null &&
            verifierKeyData.value != null && verifierChainData.value != null &&
            appPassword.value!!.isNotEmpty() && signalPassword.value!!.isNotEmpty() && server.value!!.isNotEmpty() &&
            verifierKeyData.value!!.isNotEmpty() && verifierChainData.value!!.isNotEmpty())
            VerifierNextProtocolRunData(signalPassword.value!!, appPassword.value!!, server.value!!,
                verifierMaxSkipSignals, keyData = verifierKeyData.value!!, verificationData = verifierChainData.value!!)
        else
            null


    companion object {
        @Volatile
        private var INSTANCE: ProtocolRunDataRepository? = null

        fun getInstance(context: Context?): ProtocolRunDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                if (context != null) {
                    val instance = ProtocolRunDataRepository(context)
                    INSTANCE = instance
                    instance
                }
                else
                    throw Exception("Tried to initialize singleton instance of ProtocolRunDataRepository with null context. This should not happen in normal app lifecycle!")
            }
        }
    }}