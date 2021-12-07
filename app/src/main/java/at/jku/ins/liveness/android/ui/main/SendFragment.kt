package at.jku.ins.liveness.android.ui.main

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
import at.jku.ins.liveness.android.data.*
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

        val syncBtn = binding.buttonSyncData
        val imageView: ImageView = binding.initialSignalCodeExport

        // watch out for successful execution
        pageViewModel.success.observe(viewLifecycleOwner, Observer {
            if (it is ProverOutput) {
                Log.d(Constants.LOG_TAG, "SendProtocolRun finished successfully with ProverOutput data")

                // first store this generated signal number
                data.updateProverLastSignalNumber(it.nextSignalNumber)

                // then create 150x150 bitmap to export initial signal data
                val writer = QRCodeWriter()
                try {
                    // TODO: also encode URL
                    val bitMatrix = writer.encode(SignalUtils.byteArrayToHexString(it.initialSignalData), BarcodeFormat.QR_CODE, 150, 150)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    imageView.setImageBitmap(bmp)

                    syncBtn.isEnabled = true
                } catch (e: WriterException) {
                    pageViewModel.addLine("Unable to create QRCode: $e")
                }
            }
        })

        // allow to explicitly sync to verifier fragment
        syncBtn.setOnClickListener {
            if (pageViewModel.success.value != null && pageViewModel.success.value is ProverOutput) {
                Log.d(Constants.LOG_TAG, "Syncing prover signal data to verifier")
                (activity as MainActivity).updateVerifierInitialSignalData(
                    ((pageViewModel.success.value!!) as ProverOutput).initialSignalData)
            }
            else
                Toast.makeText(context, "Initial signal data from prover run is null, can't sync to verifier", Toast.LENGTH_LONG).show()
        }

        val sendBtn = binding.buttonSend
        sendBtn.setOnClickListener {
            Log.d(Constants.LOG_TAG, "Starting send action")
            startAction(context?.let { it1 -> data.getProverProtocolRunData(it1) })
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}