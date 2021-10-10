package at.jku.ins.liveness.android.data

import at.jku.ins.liveness.android.ui.main.PageViewModel

sealed interface ProtocolRun {
    suspend fun makeRequest(viewModel: PageViewModel): Result<String>
}