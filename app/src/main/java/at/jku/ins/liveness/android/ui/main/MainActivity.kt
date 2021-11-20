package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.databinding.ActivityMainBinding
import at.jku.ins.liveness.signals.SignalUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var appPassword: String? = null
    private var signalPassword: String? = null
    private var serverUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // are we recreated (e.g. after pausing to go into settings?)
        if (savedInstanceState != null) {
            Log.d(Constants.LOG_TAG, "MainActivity re-created with saveInstanceState")
            if (! savedInstanceState.getString(Constants.intentParamAppPassword).isNullOrEmpty() )
                appPassword = savedInstanceState.getString(Constants.intentParamAppPassword)
            if (! savedInstanceState.getString(Constants.intentParamSignalPassword).isNullOrEmpty() )
                signalPassword = savedInstanceState.getString(Constants.intentParamSignalPassword)
        }

        // if started from LoginActivity, check if we have proper passwords passed here
        if (intent.hasExtra(Constants.intentParamAppPassword))
            appPassword = intent.getStringExtra(Constants.intentParamAppPassword)
        if (intent.hasExtra(Constants.intentParamSignalPassword))
            signalPassword = intent.getStringExtra(Constants.intentParamSignalPassword)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        serverUrl = sharedPreferences.getString(Constants.serverPreference, "")

        // finally start initializing the UI elements
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        // listen for changes in shared preferences and update our protocol run data when necessary
        // Note: this needs to happen after UI initialization, as the callback requires the fragments to be up!
        val listener = OnSharedPreferenceChangeListener { prefs, key ->
            Log.d(Constants.LOG_TAG, "Preferences changed: $prefs = '$key'")
            if (prefs.equals(Constants.serverPreference))
                updateProtocolRunData()
            if (prefs.equals(Constants.initialSignalDataPreference) && !key.isNullOrEmpty())
                updateVerifierInitialSignalData(SignalUtils.hexStringToByteArray(key.toString()))
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // and make sure that at the end, we update our data to be ready
        if (appPassword != null && signalPassword != null && serverUrl != null &&
            appPassword!!.isNotEmpty() && signalPassword!!.isNotEmpty() && serverUrl!!.isNotEmpty()) {
            Log.d(Constants.LOG_TAG, "Started main activity with serverUrl=${serverUrl}, signalPassword=${signalPassword}, appPassword=${appPassword}")

            /*val fab: FloatingActionButton = binding.fab
            fab.setOnClickListener { view -> startSignalAction(view) }*/

            updateProtocolRunData()
        }
        else {
            //VerifyFragment.getModel().setText("Can't continue: invalid password(s) set")
            Log.e(Constants.LOG_TAG, "Can't continue: invalid password(s) or server URL set")
        }
    }

    /** Store the in-memory passwords for an activity reload */
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(Constants.LOG_TAG, "Saving in-memory password for activity pause")
        outState.putString(Constants.intentParamAppPassword, appPassword)
        outState.putString(Constants.intentParamSignalPassword, signalPassword)
    }

    fun startSettings(menuItem: MenuItem?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun updateProtocolRunData() {
        // try to update from shared preferences - we might have returned from that activity
        serverUrl = sharedPreferences.getString(Constants.serverPreference, "")

        if (appPassword != null && signalPassword != null && serverUrl != null &&
            appPassword!!.isNotEmpty() && signalPassword!!.isNotEmpty() && serverUrl!!.isNotEmpty()) {
            val data = ProtocolRunData(signalPassword!!, appPassword!!, serverUrl!!)

            sectionsPagerAdapter.getItem(0).setData(data)
            sectionsPagerAdapter.getItem(1).setData(data)
        }
        else {
            Log.e(Constants.LOG_TAG, "Can't continue: invalid password(s) or server URL set")
            Toast.makeText(applicationContext, "Can't continue: invalid password(s) or server URL set", Toast.LENGTH_LONG).show()
        }
    }

    fun updateVerifierInitialSignalData(initialSignalData: ByteArray) {
        (sectionsPagerAdapter.getItem(0) as VerifyFragment).updateInitialSignalData(initialSignalData)
    }

    /*private fun startSignalAction(view: View) {
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        val selectedTabPosition = tabLayout.selectedTabPosition
        /*Snackbar.make(view, "Acting on tab " + selectedTabPosition, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()*/

        // update server URL - it may have been changed in settings
        val serverUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.serverPreference, "")
        if (serverUrl == null || serverUrl!!.isEmpty()) {
            Log.e(Constants.LOG_TAG, "Can't continue: invalid server URL set in settings")
            return
        }
        _data.serverUrl = serverUrl

        sectionsPagerAdapter.getItem(selectedTabPosition).startAction(
            ProtocolRunData(signalPassword!!, appPassword!!, serverUrl!!)
        )
    }*/
}
