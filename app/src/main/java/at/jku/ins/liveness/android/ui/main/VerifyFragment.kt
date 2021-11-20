package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.databinding.FragmentVerifyBinding
import at.jku.ins.liveness.signals.SignalUtils
import com.google.zxing.integration.android.IntentIntegrator

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
        super.pageViewModel.initialSignalData.observe(viewLifecycleOwner, Observer {
            if (it != null)
                verifyBtn.isEnabled = true
        })
        verifyBtn.setOnClickListener {
            startAction()
        }

        val importBtn = binding.buttonImport
        importBtn.setOnClickListener {
            IntentIntegrator(activity).initiateScan()
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(Constants.LOG_TAG, "Trying to decode QRcode text after scan")
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanningResult != null) {
            val scanContent = scanningResult.contents
            Log.d(Constants.LOG_TAG, "Decoded initial signal data: $scanContent")
            pageViewModel.setInitialSignalData(SignalUtils.hexStringToByteArray(scanContent))
            // also cache in preferences
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(Constants.initialSignalDataPreference, scanContent)
            editor.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateInitialSignalData(initialSignalData: ByteArray) {
        pageViewModel.setInitialSignalData(initialSignalData)
    }
}
