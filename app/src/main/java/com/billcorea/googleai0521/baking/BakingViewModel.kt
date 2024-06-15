package com.billcorea.googleai0521.baking

import android.graphics.Bitmap
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcorea.googleai0521.BuildConfig
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.utils.AesCryptor
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class BakingViewModel : ViewModel() {
    var leftRight = mutableStateOf("")
    var idx = mutableIntStateOf(0)
    var selectIdx = mutableIntStateOf(0)
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    val bitmapSrc = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    val bitmaps = arrayOf (bitmapSrc, bitmapSrc, bitmapSrc)
    val beforeTy = arrayOf (false, false, false)
    val bitmap2 = arrayOf (bitmapSrc, bitmapSrc)
    val before2Ty = arrayOf (false, false)

    val config = generationConfig {
        maxOutputTokens = 4096
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = AesCryptor.decrypt(BuildConfig.apiKey),
        //generationConfig = config
    )

    var ix = Random.nextInt(8)

    private val generativeModel1 = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = AesCryptor.decrypt(BuildConfig.apiKey)
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

    fun sendPromptBattleShip(prompt: String, answer: String, selectedCoordinate: Pair<Int, Int>?) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel1.generateContent(
                    content {
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

    fun sendPrompt2(bitmapOne: Bitmap, bitmapTwo: Bitmap, prompt: String, answer: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmapOne)
                        image(bitmapTwo)
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

    fun doUpdateImage2(index: Int, leftRight: String, bitmap: Bitmap, ty: Boolean) {
        when (leftRight) {
            "Left" ->  {
                bitmap2[0] = bitmap
                before2Ty[0] = ty
            }
            "Right" -> {
                bitmap2[1] = bitmap
                before2Ty[1] = ty
            }
        }
    }

}