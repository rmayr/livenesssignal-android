package at.jku.ins.liveness.android.ui.main

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.Constants
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository
import at.jku.ins.liveness.android.databinding.FragmentSendBinding
import at.jku.ins.liveness.signals.SignalUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Send fragment
 */
class SendFragment(protocol: ProtocolRun) : ViewFragment(protocol) {
    private var _binding: FragmentSendBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

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

        val imageView: ImageView = binding.initialSignalCodeExport
        data.initialSignalData.observe(viewLifecycleOwner, Observer {
            // create 150x150 bitmap to export initial signal data
            val writer = QRCodeWriter()
            try {
                // TODO: also encode URL
                val bitMatrix = writer.encode(SignalUtils.byteArrayToHexString(it), BarcodeFormat.QR_CODE, 150, 150)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                imageView.setImageBitmap(bmp)
            } catch (e: WriterException) {
                pageViewModel.addLine("Unable to create QRCode: $e")
            }

        })

        val syncBtn = binding.buttonSyncData
        super.pageViewModel.success.observe(viewLifecycleOwner, Observer {
            if (it as Boolean) {
                syncBtn.isEnabled = true
            }
        })
        // TODO: I think we don't need this anymore, as we have a singleton initial signal data
        /*syncBtn.setOnClickListener {
            if (data.initialSignalData.value != null) {
                Log.d(Constants.LOG_TAG, "Syncing prover signal data to verifier")
                else {
                    // this is a hack mostly for testing, but ugly from a separation point of view
                    Log.d(Constants.LOG_TAG, "Signal data preferences already set, only syncing temporarily to verifier fragment")
                    (activity as MainActivity).updateVerifierInitialSignalData(pageViewModel.initialSignalData.value!!)
                }
            }
            else
                Toast.makeText(context, "Initial signal data is null, can't sync to verifier", Toast.LENGTH_LONG).show()
        }*/

        val sendBtn = binding.buttonSend
        sendBtn.setOnClickListener {
            Log.d(Constants.LOG_TAG, "Starting send action")
            startAction()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}