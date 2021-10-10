package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.proofOfWork.ProofOfWork
import at.jku.ins.liveness.proofOfWork.ProofOfWorkFactory
import at.jku.ins.liveness.protocol.ChallengeMessage
import at.jku.ins.liveness.protocol.RequestMessage
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.protocol.ResponseMessage
import at.jku.ins.liveness.signals.Signal
import at.jku.ins.liveness.signals.SignalUtils
import jakarta.ws.rs.client.*
import kotlinx.coroutines.withContext

//import at.jku.ins.liveness.protocol.ChallengeMessage

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import java.security.SecureRandom

class SendProtocolRun(val newSignal: String) : ProtocolRun {
    private val serverUrl = "http://192.168.64.22:8080/liveness"

    override suspend fun makeRequest(viewModel: PageViewModel): Result<String> {
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(serverUrl);

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

            // TODO: don't need random later
            val rnd = SecureRandom()
            // TODO: do proper signal key derivation from the input here!
            val key = ByteArray(ConfigConstants.SIGNAL_LENGTH)
            rnd.nextBytes(key)
            // Make sure we do not have correct data at the moment - TODO: try with cached data first
            val data = ByteArray(ConfigConstants.SIGNAL_LENGTH)
            rnd.nextBytes(data)

            val request = RequestMessage(TYPE.STORE, Signal(key, data), solution)
            val signalTarget = livenessTarget.path("signal")
            val signalBuilder = signalTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            // Have to manually add the session cookie
            val response = signalBuilder.cookie(cookies["JSESSIONID"]).post(Entity.json(request))
            val result = response.readEntity<ResponseMessage>(ResponseMessage::class.java)
            // Get the data we submitted for storage
            val sentSignal = SignalUtils.byteArrayToHexString(request.signal.signalData)

            return Result.Success("yeah, the signal is: " + sentSignal)
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}