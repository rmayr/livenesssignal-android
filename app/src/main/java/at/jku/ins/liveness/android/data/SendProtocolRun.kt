package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.proofOfWork.ProofOfWorkFactory
import at.jku.ins.liveness.protocol.ChallengeMessage
import at.jku.ins.liveness.protocol.RequestMessage
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.protocol.ResponseMessage
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils

import jakarta.ws.rs.client.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

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
            // Step 1: Retrieve server challenge
            val challengeTarget = livenessTarget.path(Constants.serverUrlChallenge)
            val challengeBuilder = challengeTarget.request(MediaType.APPLICATION_JSON)
            var res = challengeBuilder.get(Response::class.java)
            val cookies = res.cookies
            var challenge: ChallengeMessage = res.readEntity(ChallengeMessage::class.java)
            if (res.status != 200)
                return Result.Error(Exception("Could not get challenge: server status was " + res.status))
            val pow = ProofOfWorkFactory.getProofOfWork(ConfigConstants.ALGORITHM)
            viewModel.addLine("Received PoW challenge: " + challenge.challenge)

            // Step 2: Compute PoW and submit solution to retrieve signal
            val solution = pow.proofWork(challenge.challenge, challenge.leadingZeros, 5)
            val signal = prover.nextSignal
            val request = RequestMessage(TYPE.STORE, signal, solution)
            val signalTarget = livenessTarget.path(Constants.serverUrlSignal)
            val signalBuilder = signalTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            // Have to manually add the session cookie
            val response = signalBuilder.cookie(cookies["JSESSIONID"]).post(Entity.json(request))
            val result = response.readEntity<ResponseMessage>(ResponseMessage::class.java)
            if (response.status != 200)
                return Result.Error(Exception("Could not store signal: server status was " + res.status))
            // Get the data we submitted for storage
            val receivedString = SignalUtils.byteArrayToHexString(request.signal.signalData)
            println("Using key: " + signal.retrieveKeyString())
            println("Submitted: " + SignalUtils.byteArrayToHexString(signal.signalData))
            println("Received: + " + receivedString)

            return Result.Success("yeah, the signal is: " + receivedString)
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}