package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Signal
import at.jku.ins.liveness.signals.Verifier

class VerifyProtocolRun : ProtocolRun {
    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val livenessTarget = createClient(data.serverUrl)

        if (data.initialSignalData == null)
            return Result.Error(Exception("No initial signal data set, please import from prover"))

        val verifier = Verifier(
            ConfigConstants.ALGORITHM,
            data.appPassword,
            data.signalPassword,
            data.initialSignalData
        )

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            // TODO: this needs a loop with the same skip value for getNextKey and verifyWithSkip

            // TODO: set data to random to make traffic analysis harder?
            val data = ByteArray(ConfigConstants.SIGNAL_LENGTH)
            val signal = Signal(verifier.getNextKey(0), data)
            val resultData = submitMessage(livenessTarget, TYPE.RETRIEVE, signal, solution, cookies)
            val retrievedSignal: String = resultData.retrieveDataString()

            // TODO: make maxSkipped a configurable preference instead of a magic value here
            val skippedSignals = verifier.verifyWithSkip(resultData.data, 10)
            if (skippedSignals >= 0)
                return Result.Success("Correctly verified signal (skipped $skippedSignals that were not found)")
            else {
                return Result.Error(Exception("Couldn't verify signal: $retrievedSignal"))
            }
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}