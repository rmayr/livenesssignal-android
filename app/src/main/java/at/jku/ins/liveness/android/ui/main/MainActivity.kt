package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
//import at.jku.ins.liveness.android.R
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var appPassword: String? = null
    private var signalPassword: String? = null
    private var serverUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        appPassword = intent.getStringExtra(Constants.intentParamAppPassword)
        signalPassword = intent.getStringExtra(Constants.intentParamSignalPassword)
        serverUrl = sharedPreferences.getString(Constants.serverPreference, "")

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

    fun startSettings(view: View) {
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

        // TODO: make sure we update the server URL, recreating the ProtocolRunData object in both views when anything updates, e.g. when returning from settings
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
