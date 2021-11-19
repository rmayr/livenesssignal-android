package at.jku.ins.liveness.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.SendProtocolRun
import at.jku.ins.liveness.android.databinding.FragmentSendBinding

/**
 * Send fragment
 */
class SendFragment(protocol: ProtocolRun) : ViewFragment(protocol) {
    private var _binding: FragmentSendBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendBinding.inflate(inflater, container, false)
        val root = binding.root
        val textView: TextView = binding.sendLogView
        super.pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val sendBtn = binding.buttonSend as Button
        sendBtn.setOnClickListener {
            startAction()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}