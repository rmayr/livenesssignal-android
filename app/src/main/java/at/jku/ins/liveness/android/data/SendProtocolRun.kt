package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.proofOfWork.ProofOfWork
import at.jku.ins.liveness.proofOfWork.ProofOfWorkFactory
import at.jku.ins.liveness.protocol.ChallengeMessage
import at.jku.ins.liveness.protocol.RequestMessage
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.protocol.ResponseMessage
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.Signal
import at.jku.ins.liveness.signals.SignalUtils
import jakarta.ws.rs.client.*
import kotlinx.coroutines.withContext

//import at.jku.ins.liveness.protocol.ChallengeMessage

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import java.security.SecureRandom

class SendProtocolRun() : ProtocolRun {
    private val serverUrl = "http://192.168.64.22:8080/liveness"

    companion object {
        var initialSignalData: ByteArray? = null
    }

    override suspend fun makeRequest(viewModel: PageViewModel): Result<String> {
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(serverUrl);

        val SIGNAL_COUNT = 100000
        val SHARED_PWD = "PwdShared"
        val prover = Prover(
            ConfigConstants.ALGORITHM,
            "PwdProofer",
            SHARED_PWD,
            SIGNAL_COUNT
        )
        initialSignalData = prover.initialSignalData

        try {
            // Step 1: Retrieve server challenge
            val challengeTarget = livenessTarget.path("challenge")
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
            val signalTarget = livenessTarget.path("signal")
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