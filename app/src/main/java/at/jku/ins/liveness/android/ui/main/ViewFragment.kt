package at.jku.ins.liveness.android.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunData

/**
 * Common send and verify fragment base class
 */
open class ViewFragment(private val protocol: ProtocolRun) : Fragment() {
    internal lateinit var pageViewModel: PageViewModel
    internal var data: ProtocolRunData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java) /*.apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }*/
    }

    fun setData(_data: ProtocolRunData) {
        data = _data
    }

    fun startAction() {
        val _data = data
        if (_data == null) {
            Log.e(Constants.LOG_TAG, "Can't continue: protocol run data not initialized")
            return
        }

        pageViewModel.setText("Starting request ...")
        pageViewModel.runNetworkRequest(protocol, _data)
    }

    /*companion object {
        private var singleton: ViewFragment? = null

        @JvmStatic
        fun getInstance(protocol: ProtocolRun): ViewFragment {
            if (singleton == null)
                singleton = ViewFragment(protocol)

            return singleton as ViewFragment
        }
    }*/
}