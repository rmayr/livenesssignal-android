package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils

import jakarta.ws.rs.client.*

class SendProtocolRun() : ProtocolRun {
    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val livenessTarget = createClient(data.serverUrl)

        val prover = Prover(
            ConfigConstants.ALGORITHM,
            data.appPassword,
            data.signalPassword,
            Constants.signalCount
        )
        viewModel.setInitialSignalData(prover.initialSignalData)

        viewModel.addLine("Initialized prover with serverUrl=${data.serverUrl}, signalPassword=${data.signalPassword}, appPassword=${data.appPassword}")
        viewModel.addLine("Initial signal data: ${SignalUtils.byteArrayToHexString(prover.initialSignalData)}")

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