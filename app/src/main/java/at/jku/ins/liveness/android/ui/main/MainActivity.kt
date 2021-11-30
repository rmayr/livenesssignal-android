package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository
import at.jku.ins.liveness.android.databinding.ActivityMainBinding
import info.guardianproject.netcipher.proxy.OrbotHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter

    private val data: ProtocolRunDataRepository = ProtocolRunDataRepository.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: remove this block
        // are we recreated (e.g. after pausing to go into settings?)
        /*if (savedInstanceState != null) {
            Log.d(Constants.LOG_TAG, "MainActivity re-created with saveInstanceState")
            if (! savedInstanceState.getString(Constants.intentParamAppPassword).isNullOrEmpty() )
                appPassword = savedInstanceState.getString(Constants.intentParamAppPassword)
            if (! savedInstanceState.getString(Constants.intentParamSignalPassword).isNullOrEmpty() )
                signalPassword = savedInstanceState.getString(Constants.intentParamSignalPassword)
        }*/

        // finally start initializing the UI elements
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        // if the URL is an .onion, make sure OrBot is available
        OrbotHelper.get(this).init()

        // and make sure that at the end, we update our data to be ready
        if (!data.appPassword.value.isNullOrEmpty() && !data.signalPassword.value.isNullOrEmpty() && !data.server.value.isNullOrEmpty()) {
            Log.d(Constants.LOG_TAG, "Started main activity with serverUrl=${data.server.value}, signalPassword=${data.signalPassword.value}, appPassword=${data.appPassword.value}")

            /*val fab: FloatingActionButton = binding.fab
            fab.setOnClickListener { view -> startSignalAction(view) }*/
        }
        else {
            //VerifyFragment.getModel().setText("Can't continue: invalid password(s) set")
            Log.e(Constants.LOG_TAG, "Can't continue: invalid password(s) or server URL set")
        }
    }

    // TODO: remove this method
    /** Store the in-memory passwords for an activity reload */
    /*override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(Constants.LOG_TAG, "Saving in-memory password for activity pause")
        outState.putString(Constants.intentParamAppPassword, appPassword)
        outState.putString(Constants.intentParamSignalPassword, signalPassword)
    }*/

    fun startSettings(menuItem: MenuItem?) {
        // need to pass appPassword along to SettingsActivity in case it should be saved on-device
        val intent = Intent(this, SettingsActivity::class.java)
        // TODO: remove this line
        //intent.putExtra(Constants.intentParamAppPassword, appPassword)
        startActivity(intent)
    }

    // TODO: remove this
    /*private fun updateProtocolRunData() {
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
    }*/

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
