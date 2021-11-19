package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.protocol.ResponseMessage
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils

import jakarta.ws.rs.client.*
import jakarta.ws.rs.core.MediaType

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.graphics.Color

class SendProtocolRun() : ProtocolRun {
    companion object {
        var initialSignalData: ByteArray? = null
    }

    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val client = ClientBuilder.newClient()
        val livenessTarget = client.target(data.serverUrl)

        val prover = Prover(
            ConfigConstants.ALGORITHM,
            data.appPassword,
            data.signalPassword,
            Constants.signalCount
        )
        initialSignalData = prover.initialSignalData

        viewModel.addLine("Initialized prover with serverUrl=${data.serverUrl}, signalPassword=${data.signalPassword}, appPassword=${data.appPassword}")
        viewModel.addLine("Initial signal data: ${SignalUtils.byteArrayToHexString(initialSignalData)}")

        // create 150x150 bitmap to export initial signal data
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(SignalUtils.byteArrayToHexString(initialSignalData), BarcodeFormat.QR_CODE, 150, 150)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            viewModel.setBitmap(bmp)
        } catch (e: WriterException) {
            return Result.Error(e)
        }

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            val signal = prover.nextSignal
            val resultData = submitMessage(livenessTarget, TYPE.STORE, signal, solution, cookies)
            val retrievedSignal: String = resultData.retrieveDataString()

            // Get the data we submitted for storage
            /*println("Using key: " + signal.retrieveKeyString())
            println("Submitted: " + SignalUtils.byteArrayToHexString(signal.signalData))*/

            return Result.Success("The signal is: $retrievedSignal")
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}