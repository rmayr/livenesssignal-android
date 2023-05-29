package at.jku.ins.liveness.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import at.jku.ins.liveness.android.data.*
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

    private var protocolData: ProtocolRunData? = null

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

        // enable verify button when we either have initial signal data (synced from prover or scanned from camera)
        val verifyBtn = binding.buttonVerify
        pageViewModel.initialSignalData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                verifyBtn.isEnabled = true
                protocolData = data.getInitialVerifierProtocolRunData(it)
            }
        })
        // or when both encrypted key data and verification chain data is available in preferences
        /* Note: this is a bit trickier because we need to try and read once to see if has been
         * cached in preferences and then we also register for changes.
         */
        if (data.verifierKeyData.value != null && data.verifierChainData.value != null) {
            verifyBtn.isEnabled = true
            protocolData = data.getNextVerifierProtocolRunData()
        }
        data.verifierKeyData.observe(viewLifecycleOwner) {
            if (it != null && data.verifierChainData.value != null) {
                verifyBtn.isEnabled = true
                protocolData = data.getNextVerifierProtocolRunData()
            } else {
                Log.e(
                    Constants.LOG_TAG,
                    "Observed update to verifier key/chain data with null. This should never happen."
                )
                verifyBtn.isEnabled = false
                protocolData = null
            }
        }
        data.verifierChainData.observe(viewLifecycleOwner) {
            if (it != null && data.verifierKeyData.value != null) {
                verifyBtn.isEnabled = true
                protocolData = data.getNextVerifierProtocolRunData()
            } else {
                Log.e(
                    Constants.LOG_TAG,
                    "Observed update to verifier key/chain data with null. This should never happen."
                )
                verifyBtn.isEnabled = false
                protocolData = null
            }
        }

        // and use this initialized protocol data when clicking data button
        verifyBtn.setOnClickListener {
            Log.d(Constants.LOG_TAG, "Starting verify action")
            startAction(protocolData)
        }

        val importBtn = binding.buttonImport
        importBtn.setOnClickListener {
            IntentIntegrator(activity).initiateScan()
        }

        // watch out for successful execution
        pageViewModel.success.observe(viewLifecycleOwner, Observer {
            if (it is VerifierOutput) {
                Log.d(Constants.LOG_TAG,"VerifyProtocolRun finished successfully with VerifierOutput data")

                // store the key and verification data
                data.updateVerifierKeyData(it.nextSignalKeyData, true)
                data.updateVerifierChainData(it.nextSignalVerificationData, true)

                // and prepare for the subsequent run
                protocolData = data.getNextVerifierProtocolRunData()
            }
        })

        return root
    }

    /** Called when returning from QRcode scanner */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Log.d(Constants.LOG_TAG, "Trying to decode QRcode text after scan")
        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (scanningResult != null) {
            val scanContent = scanningResult.contents
            Log.d(Constants.LOG_TAG, "Decoded initial signal data: $scanContent")
            // TODO: parse serverUrl as well
            pageViewModel.initialSignalData.value = SignalUtils.hexStringToByteArray(scanContent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateInitialSignalData(initialSignalData: ByteArray) {
        pageViewModel.initialSignalData.value = initialSignalData
    }
}
