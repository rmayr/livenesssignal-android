package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.ConfigConstants
import at.jku.ins.liveness.android.ui.main.PageViewModel
import at.jku.ins.liveness.protocol.RequestMessage.TYPE
import at.jku.ins.liveness.signals.Signal
import at.jku.ins.liveness.signals.SignalUtils
import at.jku.ins.liveness.signals.Verifier
import at.jku.ins.liveness.signals.data.VerifierData
import kotlin.random.Random

class VerifyProtocolRun : ProtocolRun {
    override suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<VerifierOutput> {
        val livenessTarget = createClient(data.serverUrl)

        // need to distinguish if this is the first or a subsequent protocol run
        val verifierData: VerifierData
        val maxSkipSignals: Int
        when(data) {
            is VerifierInitialProtocolRunData -> {
                viewModel.addLine("Initializing new Verifier with blank state from initial signal data ${SignalUtils.byteArrayToHexString(data.initialSignalData)}")
                verifierData = VerifierData(
                    ConfigConstants.ALGORITHM,
                    data.signalPassword,
                    data.appPassword,
                    data.initialSignalData)
                maxSkipSignals = data.maxSkipSignals
            }
            is VerifierNextProtocolRunData -> {
                verifierData = VerifierData(
                    ConfigConstants.ALGORITHM,
                    data.signalPassword,
                    data.appPassword,
                    data.verificationData,
                    data.keyData
                )
                maxSkipSignals = data.maxSkipSignals
            }
            else -> {
                return Result.Error(Exception("No initial signal data set, please import from prover"))
            }
        }
        val verifier = Verifier(verifierData)

        viewModel.addLine("Trying to verify signal with shared password 0x${SignalUtils.byteArrayToHexString(verifierData.sharedPassword)}, " +
                "verifier app password 0x${SignalUtils.byteArrayToHexString(verifierData.verifierPassword)}, " +
                "key chain data 0x${SignalUtils.byteArrayToHexString(verifierData.keyData)}, " +
                "verification signal chain data 0x${SignalUtils.byteArrayToHexString(verifierData.verificationData)}," +
                " skipping at max $maxSkipSignals signals")
        viewModel.addLine("First key 0x${SignalUtils.byteArrayToHexString(verifier.getNextKey(0))}")

        try {
            // now we loop up to the maximum number of signals we are willing to skip
            for (skip in 0..maxSkipSignals) {
                val (solution, cookies) = computeProofOfWork(livenessTarget)

                // for each skip, try to fetch this signal with the respective next key
                val signalData = ByteArray(ConfigConstants.SIGNAL_LENGTH)
                // fill the data part of the signal with random bytes, as we are querying, not submitting
                Random.Default.nextBytes(signalData)
                val signal = Signal(verifier.getNextKey(skip), signalData)
                //viewModel.addLine("Trying with skip $skip at key 0x${SignalUtils.byteArrayToHexString(signal.key)}")
                val resultData = submitMessage(livenessTarget, TYPE.RETRIEVE, signal, solution, cookies)
                val retrievedSignal: String = resultData.retrieveDataString()

                // and try to verify
                val skippedSignals = verifier.verifyWithSkip(resultData.data, skip)
                //viewModel.addLine("Retrieved signal 0x$retrievedSignal, verification result=$skippedSignals")

                // if success, done, if not, try again until maxSkipSignal
                if (skippedSignals == skip)
                    return Result.Success(VerifierOutput(
                        "Correctly verified signal (skipped $skippedSignals that were not found)",
                        nextSignalKeyData = verifier.data.keyDataEnc,
                        nextSignalVerificationData = verifier.data.verificationData
                    ))
            }
            // if we get to here, we were not able to find a signal that verified --> fail
            return Result.Error(Exception("Couldn't verify signals, having skipped up to $maxSkipSignals"))
        }
        catch (e: Exception) {
            return Result.Error(e)
        }
    }
}