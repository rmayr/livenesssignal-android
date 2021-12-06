package at.jku.ins.liveness.android.ui.main

import androidx.lifecycle.*
import at.jku.ins.liveness.android.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PageViewModel() : ViewModel() {
    private val _textBuilder = StringBuilder()
    val text = MutableLiveData<String>()
    val success = MutableLiveData<SuccessOutput>()
    // this is only used in the verifier, so having it in here is slightly ugly
    val initialSignalData = MutableLiveData<ByteArray>()

    fun setText(newText: String) {
        _textBuilder.clear()
        addLine(newText)
    }

    fun addLine(addText: String) {
        _textBuilder.append(addText).append('\n')
        text.postValue(_textBuilder.toString())
    }

    fun runNetworkRequest(protocol: ProtocolRun, data: ProtocolRunData, viewModel: PageViewModel = this) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = protocol.makeRequest(viewModel, data)) {
                is Result.Success<SuccessOutput> -> {
                    addLine("Success: " + result.data.text)
                    success.postValue(result.data)
                }
                else -> addLine("Error: $result")
            }
        }
    }
}