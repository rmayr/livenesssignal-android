package at.jku.ins.liveness.android.data

import androidx.preference.PreferenceManager
import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.login.CryptographyManager
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils
import at.jku.ins.liveness.signals.data.ProverData

class SendProtocolRun() : ProtocolRun {
    private val cryptographyManager = CryptographyManager()

    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val livenessTarget = createClient(data.serverUrl)

        // initialize with defaults from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences()

        // TODO: retrieve the last nextSignalNumber
        val iv = cryptographyManager.getStaticIv()
        val lastSignalNumber

        val proverData = ProverData(data.signalPassword, data.appPassword, Constants.signalCount, iv, lastSignalNumber)
        val prover = Prover(ConfigConstants.ALGORITHM, proverData)

        viewModel.setInitialSignalData(prover.initialSignalData)

        viewModel.addLine("Initialized prover with serverUrl=${data.serverUrl}, signalPassword=${data.signalPassword}, appPassword=${data.appPassword}")
        viewModel.addLine("Initial signal data: ${SignalUtils.byteArrayToHexString(prover.initialSignalData)}")

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            val signal = prover.nextSignal
            // TODO: remember current signal number
            prover.data.nextSignalNumber

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