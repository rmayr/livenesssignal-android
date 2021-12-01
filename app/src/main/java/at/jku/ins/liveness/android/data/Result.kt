package at.jku.ins.liveness.android.data

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

/** On Success, additional data is returned from protocols runs. */
abstract class SuccessOutput(
    open val text: String
)

data class ProverOutput(
    override val text: String,
    val nextSignalNumber: Int,
    val initialSignalData: ByteArray
): SuccessOutput(text)

data class VerifierOutput(
    override val text: String,
    val nextSignalKeyData: ByteArray,
    val nextSignalVerificationData: ByteArray
): SuccessOutput(text)

