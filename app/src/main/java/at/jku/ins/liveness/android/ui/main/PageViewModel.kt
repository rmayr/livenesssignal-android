package at.jku.ins.liveness.android.ui.main

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import at.jku.ins.liveness.android.data.ProtocolRun
import at.jku.ins.liveness.android.data.ProtocolRunData
import at.jku.ins.liveness.android.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PageViewModel : ViewModel() {
    private val _textBuilder = StringBuilder()
    val text = MutableLiveData<String>()
    val bitmap = MutableLiveData<Bitmap>()
    val success = MutableLiveData<Boolean>()

    fun setText(newText: String) {
        _textBuilder.clear()
        addLine(newText)
    }

    fun addLine(addText: String) {
        _textBuilder.append(addText).append('\n')
        text.postValue(_textBuilder.toString())
    }

    fun setBitmap(newBitmap: Bitmap) {
        bitmap.postValue(newBitmap)
    }

    fun runNetworkRequest(protocol: ProtocolRun, data: ProtocolRunData, viewModel: PageViewModel = this) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = protocol.makeRequest(viewModel, data)) {
                is Result.Success<String> -> {
                    addLine("Yeah")
                    success.postValue(true)
                }
                else -> addLine("Error" + result)
            }
        }
    }
}