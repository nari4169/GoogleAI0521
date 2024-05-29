package com.billcorea.googleai0521

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    val bitmapSrc = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    val bitmaps = arrayOf (bitmapSrc, bitmapSrc, bitmapSrc)
    val beforeTy = arrayOf (false, false, false)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.AUTH_INFO
    )

    fun sendPrompt(
        bitmaps: Array<Bitmap>,
        prompt: String,
        answer: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmaps[0])
                        image(bitmaps[1])
                        image(bitmaps[2])
                        text("$prompt Please answer in Korean.")
                        text("My answer is:$answer")
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun doUpdateImageArray(i: Int, bitmap: Bitmap, b: Boolean) {
        bitmaps[i] =  bitmap
        beforeTy[i] = b
    }
}