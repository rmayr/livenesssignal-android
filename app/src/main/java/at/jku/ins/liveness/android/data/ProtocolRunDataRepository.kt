package at.jku.ins.liveness.android.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.signals.SignalUtils

// heavily inspired by https://github.com/googlecodelabs/android-datastore/blob/master/app/src/main/java/com/codelab/android/datastore/data/UserPreferencesRepository.kt
class ProtocolRunDataRepository private constructor(context: Context) {
    private lateinit var sharedPreferences: SharedPreferences

    /** Internal storage for server URL. */
    private val _server = MutableLiveData<String>()
    /** This String represents the server URL to interact with. */
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
    /** This String represents local app password used to protect local storage. */
    val appPassword: LiveData<String>
        get() {
            return _appPassword
        }
    fun updateAppPassword(s: String) {
        // TODO: if preferences are set to store the local app password, then do so
        // and post updates to any observers of the getter
        _appPassword.postValue(s)
    }

    private val _signalPassword = MutableLiveData<String>()

    private val _initialSignalData = MutableLiveData<ByteArray>()

    companion object {
        @Volatile
        private var INSTANCE: ProtocolRunDataRepository? = null

        fun getInstance(context: Context): ProtocolRunDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = ProtocolRunDataRepository(context)
                instance.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                INSTANCE = instance
                instance
            }
        }
    }}