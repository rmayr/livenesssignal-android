package at.jku.ins.liveness.android.data

class Constants {
    companion object {
        public val serverPreference = "server"
        public val initialSignalDataPreference = "initial_signal_data"
        public val biometricPreference = "biometricsAppPassword"
        public val signalCount = 100000

        public val serverUrlChallenge = "challenge"
        public val serverUrlSignal = "signal"

        public val intentParamAppPassword = "appPassword"
        public val intentParamSignalPassword = "signalPassword"

        public const val LOG_TAG = "LivenessSignal"

        public val PROTOCOL_DATA_PREFERENCES = "protocol_preferences"

        public const val BIOMETRIC_PREFERENCES = "biometric_preferences"
        public const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"

        public const val IV_KEY_NAME = "static_iv"
    }
}
