package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Prover
import at.jku.ins.liveness.signals.SignalUtils
import at.jku.ins.liveness.signals.data.ProverData

class SendProtocolRun : ProtocolRun {
    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<ProverOutput> {
        val livenessTarget = createClient(data.serverUrl)

        val lastSignalNumber: Int
        val iv: ByteArray
        if (data is ProverProtocolRunData) {
            if (data.lastSignalNumber == null) {
                lastSignalNumber = 1
                viewModel.addLine("Last signal number not set so far, initializing with $lastSignalNumber")
            }
            else {
                lastSignalNumber = data.lastSignalNumber
            }
            iv = data.iv
        }
        else {
            lastSignalNumber = 1
            viewModel.addLine("WARNING: SendProtocolRun called without ProverProtocolRunData. Setting lastSignalNumber=$lastSignalNumber and continuing, but this should not happen.")
            // this is a bad hack and really shouldn't happen - in this case the IV is set to 0 as a NOP
            iv = ByteArray(32)
        }

        // the IV is created on first call and then stored in keystore (unauthenticated)
        val proverData = ProverData(
            ConfigConstants.ALGORITHM,
            data.signalPassword,
            data.appPassword,
            Constants.signalCount,
            iv,
            lastSignalNumber)
        val prover = Prover(proverData)

        viewModel.addLine("Initialized prover with serverUrl=${data.serverUrl}, " +
                "signalPassword=0x${SignalUtils.byteArrayToHexString(proverData.sharedPassword)}, " +
                "appPassword=0x${SignalUtils.byteArrayToHexString(proverData.prooferPassword)}, " +
                "iv=0x${SignalUtils.byteArrayToHexString(proverData.iv)}, " +
                "nextSignalNumber=${proverData.nextSignalNumber}")
        viewModel.addLine("Resulting initial signal data: ${SignalUtils.byteArrayToHexString(prover.initialSignalData)}")

        try {
            val (solution, cookies) = computeProofOfWork(livenessTarget)

            val signal = prover.nextSignal
            val signalNumber = prover.data.nextSignalNumber

            val resultData = submitMessage(livenessTarget, TYPE.STORE, signal, solution, cookies)
            val retrievedSignal: String = resultData.retrieveDataString()

            return Result.Success(
                ProverOutput(
                "The signal number $signalNumber is: 0x$retrievedSignal at key 0x${SignalUtils.byteArrayToHexString(signal.key)}",
                nextSignalNumber = signalNumber,
                initialSignalData = prover.initialSignalData
            ))
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}