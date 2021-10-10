package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import at.jku.ins.liveness.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val TAG = "LivenessSignal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        /*val fab: FloatingActionButton = binding.fab
        fab.setOnClickListener { view -> startSignalAction(view) }*/
    }

    fun startSettings(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /*fun startSignalAction(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))

        //val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        //val selectedTabPosition = tabLayout.selectedTabPosition
        /*Snackbar.make(view, "Acting on tab " + selectedTabPosition, Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()*/

        //Log.v(TAG, tabLayout.toString())
    }*/
}
