package at.jku.ins.liveness.android.data

/**
 * Represent the necessary input data for a single protocol run. These fields are used both by
 * prover and verifier.
 */
abstract class ProtocolRunData(
    open val signalPassword: String,
    open val appPassword: String,
    open val serverUrl: String
)

/** Represents the necessary input data for a prover protocol run. If lastSignalNumber is null,
 * it is initialized with 0.
 */
data class ProverProtocolRunData(
    override val signalPassword: String,
    override val appPassword: String,
    override val serverUrl: String,
    val iv: ByteArray,
    val lastSignalNumber: Int?
) : ProtocolRunData(signalPassword, appPassword, serverUrl)

/** Represents the necessary input data for the first verifier protocol run. Resulting encrypted key
 * and verification data should be saved and passed on the next run as [VerifierNextProtocolRunData].
 */
data class VerifierInitialProtocolRunData(
    override val signalPassword: String,
    override val appPassword: String,
    override val serverUrl: String,
    val maxSkipSignals: Int,
    val initialSignalData: ByteArray
) : ProtocolRunData(signalPassword, appPassword, serverUrl)

/** Represents the necessary input data for all but the first verifier protocol runs. Resulting encrypted key
 * and verification data should be saved and passed on the next run as [VerifierNextProtocolRunData].
 */
data class VerifierNextProtocolRunData(
    override val signalPassword: String,
    override val appPassword: String,
    override val serverUrl: String,
    val maxSkipSignals: Int,
    val keyData: ByteArray,
    val verificationData: ByteArray
) : ProtocolRunData(signalPassword, appPassword, serverUrl)
