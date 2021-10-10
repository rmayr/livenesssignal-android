package at.jku.ins.liveness.android.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {
    private val _textBuilder = StringBuilder()
    val text = MutableLiveData<String>()

    fun setText(newText: String) {
        _textBuilder.clear()
        addLine(newText)
    }

    fun addLine(addText: String) {
        _textBuilder.append(addText).append('\n')
        text.value = _textBuilder.toString()
    }
}