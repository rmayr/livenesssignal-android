package at.jku.ins.liveness.android.ui.main

import androidx.lifecycle.*
import at.jku.ins.liveness.android.data.ProtocolRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PageViewModel : ViewModel() {
    private val _textBuilder = StringBuilder()
    val text = MutableLiveData<String>()

    fun setText(newText: String) {
        _textBuilder.clear()
        addLine(newText)
    }

    fun addLine(addText: String) {
        _textBuilder.append(addText).append('\n')
        text.postValue(_textBuilder.toString())
    }

    fun runNetworkRequest(protocol: ProtocolRun, viewModel: PageViewModel = this) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = protocol.makeRequest(viewModel)
            when (result) {
                //is Result.Success<String> -> addLine("Yeah")
                else -> addLine("Error" + result)
            }
        }
    }
}