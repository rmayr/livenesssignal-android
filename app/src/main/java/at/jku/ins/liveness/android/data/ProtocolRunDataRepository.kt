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
                _server.postValue(sharedPreferences.getString(Constants.serverPreference, "").orEmpty())

            if (key.equals(Constants.initialSignalDataPreference)) {
                val signalDataPrefs = prefs.getString(Constants.initialSignalDataPreference, "")
                if (!signalDataPrefs.isNullOrEmpty()) {
                    try {
                        _initialSignalData.postValue(SignalUtils.hexStringToByteArray(signalDataPrefs))
                    } catch (e: Exception) {
                        Log.e(Constants.LOG_TAG,"Unable to parse initial signal data from preferences: $e")
                    }
                }
            }
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
                sharedPreferences.getString(Constants.serverPreference, "").also {
                    _server.postValue(it)
                }
            }
            return _server
        }
    /** Update server URL. This will both update the in-memory @_server variable in this object
     * as well as the preference value. */
    fun updateServer(s: String) {
        // update preferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constants.serverPreference, s)
        editor.apply()
        // and post updates to any observers of the getter
        _server.postValue(s)
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
        _appPassword.postValue(s)
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
        _signalPassword.postValue(s)
    }

    /** Internal storage for initial signal data shared from prover to verifier. */
    private val _initialSignalData = MutableLiveData<ByteArray>()
    /** This ByteArray represents the initial signal data shared from prover to verifier.
     * On reading, it will check SharedPreferences and initialize from there. */
    val initialSignalData: LiveData<ByteArray>
        get() {
            // if not yet set, ...
            if (_initialSignalData.value == null) {
                // ... and we have a cached value set in SharedPreferences ...
                sharedPreferences.getString(Constants.initialSignalDataPreference, "").also {
                    if (! it.isNullOrEmpty()) {
                        Log.d(Constants.LOG_TAG,"Found previously stored initial signal data in preferences ('$it'), initializing verifier")
                        // ... then try to parse back into ByteArray representation
                        try {
                            _initialSignalData.postValue(SignalUtils.hexStringToByteArray(it))
                        } catch (e: Exception) {
                            Log.e(Constants.LOG_TAG,"Unable to parse initial signal data from preferences ('$it'): $e")
                        }
                    }
                }
            }
            return _initialSignalData
        }

    /** If importFromScan is set to true, always cache in SharedPreferences as it has been freshly imported by the used. If false,
     * only cache if not yet stored in SharedPreferences
     */
    fun updateInitialSignalData(newData: ByteArray, importFromScan: Boolean) {
        // also cache in preferences - but only if not yet present (don't overwrite a previously scanned result)
        Log.d(Constants.LOG_TAG, "Currently set initial signal data: '${sharedPreferences.getString(Constants.initialSignalDataPreference, "")}'")
        if (sharedPreferences.getString(Constants.initialSignalDataPreference, "").isNullOrEmpty() || importFromScan) {
            // writing the preferences will actually cause the local data in VerifyFragment to be set through the settings listener in MainActivity
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(Constants.initialSignalDataPreference, SignalUtils.byteArrayToHexString(newData))
            editor.apply()
            Log.d(Constants.LOG_TAG, "Synced prover signal data to preferences")
        }
        // and post updates to any observers of the getter
        _initialSignalData.postValue(newData)
    }

    private val _lastSignalNumber = MutableLiveData<Int>()
    val lastSignalNumber: LiveData<Int>
        get() {
            return _lastSignalNumber
        }
    fun updateLastSignalNumber(i: Int) {
        // post updates to any observers of the getter
        _lastSignalNumber.postValue(i)
    }

    /** Returns a current snapshot of the internally stored data values for starting a protocol run. */
    fun getProtocolRunData(): ProtocolRunData? =
        if (appPassword.value != null && signalPassword.value != null && server.value != null &&
            appPassword.value!!.isNotEmpty() && signalPassword.value!!.isNotEmpty() && server.value!!.isNotEmpty())
            ProtocolRunData(signalPassword.value!!, appPassword.value!!, server.value!!,
                initialSignalData.value, lastSignalNumber.value)
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