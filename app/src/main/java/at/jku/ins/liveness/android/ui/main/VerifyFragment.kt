package at.jku.ins.liveness.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.databinding.FragmentVerifyBinding

/**
 * Verify fragment
 */
class VerifyFragment(protocol: ProtocolRun) : ViewFragment(protocol) {
    private var _binding: FragmentVerifyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        val root = binding.root
        val textView: TextView = binding.verifyLogView
        super.pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val verifyBtn = binding.buttonVerify
        verifyBtn.setOnClickListener {
            startAction()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateInitialSignalData(initialSignalData: ByteArray) {
        pageViewModel.setInitialSignalData(initialSignalData)
    }
}
