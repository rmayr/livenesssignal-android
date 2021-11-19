package at.jku.ins.liveness.android.ui.login

import at.jku.ins.liveness.android.data.ProtocolRunData

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: ProtocolRunData? = null,
    val error: Int? = null
)