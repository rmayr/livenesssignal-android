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
import at.jku.ins.liveness.signals.Verifier
import jakarta.ws.rs.client.*
import kotlinx.coroutines.withContext

//import at.jku.ins.liveness.protocol.ChallengeMessage

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import java.security.SecureRandom

class VerifyProtocolRun : ProtocolRun {
    private val serverUrl = "http://192.168.64.22:8080/liveness"

    override suspend fun makeRequest(viewModel: PageViewModel): Result<String> {
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(serverUrl);

        if (SendProtocolRun.initialSignalData == null)
            return Result.Error(Exception("poof"))

        val SIGNAL_COUNT = 100000
        val SHARED_PWD = "PwdShared"
        // TODO: need to implement QRcode transfer of initial signal data
        val verifier = Verifier(
            ConfigConstants.ALGORITHM,
            "PwdVerifier",
            SHARED_PWD,
            SendProtocolRun.initialSignalData
        )

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

            val data = ByteArray(ConfigConstants.SIGNAL_LENGTH)
            val request = RequestMessage(TYPE.RETRIEVE, Signal(verifier.getNextKey(0), data), solution)
            val signalTarget = livenessTarget.path("signal")
            // Need to create new builder to remove old cookie
            val signalBuilder = signalTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
            // Have to manually add the session cookie
            val response = signalBuilder.cookie(cookies["JSESSIONID"]).post(Entity.json(request))
            if (response.status != 200)
                return Result.Error(Exception("Could not retrieve signal: server status was " + res.status))
            val result = response.readEntity<ResponseMessage>(ResponseMessage::class.java)
            val retrievedSignal: String = result.retrieveDataString()

            if (verifier.verify(result.data))
                return Result.Success("yeah, correctly verified signal is: " + retrievedSignal)
            else
                return Result.Error(Exception("booh, couldn't verify signal: " + retrievedSignal))
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}