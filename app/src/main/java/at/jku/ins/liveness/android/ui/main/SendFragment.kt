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
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.databinding.FragmentSendBinding

/**
 * A placeholder fragment containing a simple view.
 */
class SendFragment(private val protocol: ProtocolRun) : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentSendBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java) /*.apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendBinding.inflate(inflater, container, false)
        val root = binding.root
        val textView: TextView = binding.sendLogView
        pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        /*val sendBtn = binding.buttonSend as Button
        sendBtn.setOnClickListener {
            startSend(root)
        }*/

        Log.d(Constants.LOG_TAG, "SendFragment initialized")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*fun startSend(view: View) {
        pageViewModel.setText("Starting request ...")
        pageViewModel.runNetworkRequest(SendProtocolRun())
    }*/

    fun startAction(data: ProtocolRunData) {
        pageViewModel.setText("Starting request ...")
        pageViewModel.runNetworkRequest(protocol, data)
    }


    companion object {
        private var singleton: SendFragment? = null

        @JvmStatic
        fun getInstance(protocol: ProtocolRun): SendFragment {
            if (singleton == null)
                singleton = SendFragment(protocol)

            return singleton as SendFragment
        }
    }
}