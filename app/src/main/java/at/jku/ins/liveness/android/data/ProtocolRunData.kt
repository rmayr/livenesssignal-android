package at.jku.ins.liveness.android.data

/**
 * User details post authentication that is exposed to the UI
 */
data class ProtocolRunData(
    val signalPassword: String,
    val appPassword: String,
    val serverUrl: String
)