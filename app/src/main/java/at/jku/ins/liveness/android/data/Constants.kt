package at.jku.ins.liveness.android.data

class Constants {
    companion object {
        public val serverPreference = "server"
        public val initialSignalDataPreference = "initial_signal_data"
        public val signalCount = 100000

        public val serverUrlChallenge = "challenge"
        public val serverUrlSignal = "signal"

        public val intentParamAppPassword = "appPassword"
        public val intentParamSignalPassword = "signalPassword"

        public val LOG_TAG = "LivenessSignal"
    }
}

const val SHARED_PREFS_FILENAME = "biometric_prefs"
const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
