package at.jku.ins.liveness.android.data

import kotlinx.coroutines.withContext

//import at.jku.ins.liveness.protocol.ChallengeMessage

import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response

class VerifyProtocolRun : ProtocolRun {
    private val serverUrl = "https://192.168.64.22:8080/liveness"

    override suspend fun makeRequest(): Result<String> {
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(serverUrl);
        val challengeTarget = livenessTarget.path("challenge")
        val challengeBuilder = challengeTarget.request(MediaType.APPLICATION_JSON)

        try {
            val res = challengeBuilder.get(Response::class.java)
            val cookies = res.cookies
            //val challenge: ChallengeMessage = res.readEntity(ChallengeMessage::class.java)
            return Result.Success("yeah" + res.toString())
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}