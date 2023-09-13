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

        // finally start initializing the UI elements
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        // if the URL is an .onion, make sure OrBot is available
        val orbot = OrbotHelper.get(this.applicationContext)
        orbot.init()
        // check if we have Orbot installed so that we can use it
        data.server.value?.let { if (it.contains(".onion")) {
            Log.d(Constants.LOG_TAG, "Server URL is a Tor Onion service, checking that Orbot is installed")
            if (! orbot.isInstalled) {
                Log.w(Constants.LOG_TAG,"Orbot is not installed, will try to install it for Onion service communication")
                // TODO: UI interaction: tell users what needs to be done, add callback and wait for installation
                orbot.installOrbot(this)
            }
        } }

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

    fun startSettings(menuItem: MenuItem?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun startLegal(menuItem: MenuItem?) {
        val intent = Intent(this, LegalActivity::class.java)
        startActivity(intent)
    }

    fun updateVerifierInitialSignalData(initialSignalData: ByteArray) {
        (sectionsPagerAdapter.getItem(0) as VerifyFragment).updateInitialSignalData(initialSignalData)
    }
}
