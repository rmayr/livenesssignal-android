package at.jku.ins.liveness.android.data

import android.content.SharedPreferences
import at.jku.ins.liveness.android.ui.main.PageViewModel

sealed interface ProtocolRun {
    suspend fun makeRequest(viewModel: PageViewModel, data: ProtocolRunData): Result<String>
}