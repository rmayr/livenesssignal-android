package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.login.CryptographyManager
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils
import at.jku.ins.liveness.signals.data.ProverData

// TODO: this is suboptimal, but a quick hack to allow the protocol to update state data
class SendProtocolRun(private val writableData: ProtocolRunDataRepository) : ProtocolRun {
    private val cryptographyManager = CryptographyManager()

    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String> {
        val livenessTarget = createClient(data.serverUrl)

        val lastSignalNumber: Int
        if (data.lastSignalNumber == null) {
            lastSignalNumber = 0
            viewModel.addLine("Last signal number not set so far, initializing with $lastSignalNumber")
        }
        else {
            lastSignalNumber = data.lastSignalNumber
        }

        // TODO: this tries to create an authenticated key, which is wrong for that case
        val iv = cryptographyManager.getStaticIv()
        val proverData = ProverData(
            data.signalPassword,
            data.appPassword,
            Constants.signalCount,
            iv,
            lastSignalNumber)
        val prover = Prover(ConfigConstants.ALGORITHM, proverData)

        // update the initial signal data in our repository - the SendFragment is an observer on this variable and will update the QRcode
        writableData.updateInitialSignalData(prover.initialSignalData, false)

        viewModel.addLine("Initialized prover with serverUrl=${data.serverUrl}, signalPassword=${data.signalPassword}, appPassword=${data.appPassword}, lastSignalNumber=$lastSignalNumber")
        viewModel.addLine("Resulting initial signal data: ${SignalUtils.byteArrayToHexString(prover.initialSignalData)}")

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            val signal = prover.nextSignal
            val signalNumber = prover.data.nextSignalNumber
            writableData.updateLastSignalNumber(signalNumber)

            val resultData = submitMessage(livenessTarget, TYPE.STORE, signal, solution, cookies)
            val retrievedSignal: String = resultData.retrieveDataString()

            // Get the data we submitted for storage
            /*println("Using key: " + signal.retrieveKeyString())
            println("Submitted: " + SignalUtils.byteArrayToHexString(signal.signalData))*/

            return Result.Success("The signal number $signalNumber is: $retrievedSignal")
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}