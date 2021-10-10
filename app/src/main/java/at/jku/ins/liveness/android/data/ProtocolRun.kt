package at.jku.ins.liveness.android.data

sealed interface ProtocolRun {
    suspend fun makeRequest(): Result<String>
}