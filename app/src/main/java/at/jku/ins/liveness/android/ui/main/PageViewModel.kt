package at.jku.ins.liveness.android.ui.main

import androidx.lifecycle.*
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PageViewModel() : ViewModel() {
    private val _textBuilder = StringBuilder()
    val text = MutableLiveData<String>()
    val success = MutableLiveData<Boolean>()

    fun setText(newText: String) {
        _textBuilder.clear()
        addLine(newText)
    }

    fun addLine(addText: String) {
        _textBuilder.append(addText).append('\n')
        text.postValue(_textBuilder.toString())
    }

    fun setInitialSignalData(newInitialSignalData: ByteArray) {
        initialSignalData.postValue(newInitialSignalData)
    }

    fun runNetworkRequest(protocol: ProtocolRun, data: ProtocolRunData, viewModel: PageViewModel = this) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = protocol.makeRequest(viewModel, data)) {
                is Result.Success<String> -> {
                    addLine("Success: " + result.data)
                    success.postValue(true)
                }
                else -> addLine("Error: $result")
            }
        }
    }
}