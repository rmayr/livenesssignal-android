package at.jku.ins.liveness.android.data

/**
 * Represent the necessary input data for a single protocol run - not all fields are necessarily
 * required for the prover and/or verifier.
 */
data class ProtocolRunData(
    val signalPassword: String,
    val appPassword: String,
    val serverUrl: String,
    val initialSignalData: ByteArray,
    val lastSignalNumber: Int
)

