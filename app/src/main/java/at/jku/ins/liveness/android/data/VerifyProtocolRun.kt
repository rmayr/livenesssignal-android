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
    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(data.serverUrl)

        if (SendProtocolRun.initialSignalData == null)
            return Result.Error(Exception("No initial signal data set, please import from prover"))

        // TODO: need to implement QRcode transfer of initial signal data
        val verifier = Verifier(
            ConfigConstants.ALGORITHM,
            data.appPassword,
            data.signalPassword,
            SendProtocolRun.initialSignalData
        )

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            val data = ByteArray(ConfigConstants.SIGNAL_LENGTH)
            val signal = Signal(verifier.getNextKey(0), data)
            val resultData = submitMessage(livenessTarget, TYPE.RETRIEVE, signal, solution, cookies)
            val retrievedSignal: String = resultData.retrieveDataString()

            if (verifier.verify(resultData.data))
                return Result.Success("Correctly verified signal is: $retrievedSignal")
            else
                return Result.Error(Exception("Couldn't verify signal: $retrievedSignal"))
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}