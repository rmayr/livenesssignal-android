package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.proofOfWork.ProofOfWorkFactory
import at.jku.ins.liveness.protocol.ChallengeMessage
import at.jku.ins.liveness.protocol.RequestMessage
import at.jku.ins.liveness.protocol.ResponseMessage
import at.jku.ins.liveness.signals.Signal
import jakarta.ws.rs.client.Entity
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.client.ClientBuilder
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.ClientConfig

/** This class implements the general network protocol to interact with a singal server. */
interface ProtocolRun {
    abstract suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<SuccessOutput>

    fun createClient(serverUrl: String): WebTarget {
        val config = ClientConfig()
        if (serverUrl.contains(".onion")) {
            // TODO: check if we have Orbot installed so that we can use it
            config.property(ClientProperties.PROXY_URI, "localhost:9050")
        }
        val client = ClientBuilder.newClient(config)
        return client.target(serverUrl)
    }

    /** Before being able to store or retrieve signals, need to solve a fresh challenge by the server */
    fun computeProofOfWork(livenessTarget: WebTarget): Pair<Long, Map<String, NewCookie>> {
        // Step 1: Retrieve server challenge
        val challengeTarget = livenessTarget.path(Constants.serverUrlChallenge)
        val challengeBuilder = challengeTarget.request(MediaType.APPLICATION_JSON)
        val res = challengeBuilder.get(Response::class.java)
        val challenge: ChallengeMessage = res.readEntity(ChallengeMessage::class.java)
        if (res.status != 200)
            throw Exception("Could not get challenge: server status was " + res.status)
        val pow = ProofOfWorkFactory.getProofOfWork(ConfigConstants.ALGORITHM)
        //println("Received PoW challenge: " + challenge.challenge)

        // Step 2: Compute PoW and to submit solution in the next message
        val solution = pow.proofWork(challenge.challenge, challenge.leadingZeros, 5)
        return Pair(solution, res.cookies)
    }

    /** With the solution to the fresh challenge, store or retrieve signal */
    fun submitMessage(livenessTarget: WebTarget, messageType: RequestMessage.TYPE, signal: Signal, solution: Long, cookies: Map<String, NewCookie>): ResponseMessage {
        val request = RequestMessage(messageType, signal, solution)
        val signalTarget = livenessTarget.path(Constants.serverUrlSignal)
        // Need to create new builder to remove old cookie
        val signalBuilder = signalTarget.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        // Have to manually add the session cookie
        val response = signalBuilder.cookie(cookies["JSESSIONID"]).post(Entity.json(request))
        if (response.status != 200)
            throw Exception("Could not submit/retrieve signal message: server status was " + response.status)

        return response.readEntity<ResponseMessage>(ResponseMessage::class.java)
    }
}