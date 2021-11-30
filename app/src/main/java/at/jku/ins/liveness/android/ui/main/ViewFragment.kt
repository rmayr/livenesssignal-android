package at.jku.ins.liveness.android.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository

/**
 * Common send and verify fragment base class
 */
open class ViewFragment(private val protocol: ProtocolRun) : Fragment() {
    internal lateinit var pageViewModel: PageViewModel
    internal val data: ProtocolRunDataRepository = ProtocolRunDataRepository.getInstance(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java) /*.apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }*/
    }

    fun startAction() {
        // at this point get a snapshot of protocol run data
        val _data = data.getProtocolRunData()
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