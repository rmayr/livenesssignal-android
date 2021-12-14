package at.jku.ins.liveness.android.data

class Constants {
    companion object {
        const val serverPreference = "server"
        const val proverNextSignalPreference = "prover_next_signal"
        const val verifierEncKeyDataPreference = "verifier_enc_key_data"
        const val verifierChainDataPreference = "verifier_chain_data"
        const val verifierMaxSignalsSkipPreference = "verifier_max_signals_skip"
        const val biometricPreference = "biometricsAppPassword"
//        public const val signalCount = 100 // For faster testing
        const val signalCount = 100000

        const val serverUrlChallenge = "challenge"
        const val serverUrlSignal = "signal"

        const val LOG_TAG = "LivenessSignal"

        const val BIOMETRIC_PREFERENCES = "biometric_preferences"
        const val PROTOCOL_PREFERENCES = "protocol_preferences"
        const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"

        const val IV_KEY_NAME = "static_iv"
    }
}
