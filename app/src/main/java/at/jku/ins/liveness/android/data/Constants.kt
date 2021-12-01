package at.jku.ins.liveness.android.data

class Constants {
    companion object {
        public const val serverPreference = "server"
        public const val verifierEncKeyDataPreference = "verifier_enc_key_data"
        public const val verifierChainDataPreference = "verifier_chain_data"
        public const val verifierMaxSignalsSkipPreference = "verifier_max_signals_skip"
        public const val biometricPreference = "biometricsAppPassword"
        public const val signalCount = 100000

        public const val serverUrlChallenge = "challenge"
        public const val serverUrlSignal = "signal"

        public const val LOG_TAG = "LivenessSignal"

        public const val BIOMETRIC_PREFERENCES = "biometric_preferences"
        public const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"

        public const val IV_KEY_NAME = "static_iv"
    }
}
