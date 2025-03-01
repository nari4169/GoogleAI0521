package com.billcorea.googleai0521.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billcorea.googleai0521.BuildConfig
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.retrofit.AppTranslator
import com.billcorea.googleai0521.retrofit.ImageGenerationRequest
import com.billcorea.googleai0521.retrofit.ImageGenerationResponse
import com.billcorea.googleai0521.retrofit.RetrofitAPI
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

class BakingViewModel : ViewModel() {
    var leftRight = mutableStateOf("")
    var idx = mutableIntStateOf(0)
    var selectIdx = mutableIntStateOf(0)
    val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    val _openAIUrl : MutableStateFlow<String> = MutableStateFlow("")
    val openAIUrl = _openAIUrl.asStateFlow()

    val bitmapSrc = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    val bitmaps = arrayOf (bitmapSrc, bitmapSrc, bitmapSrc)
    val beforeTy = arrayOf (false, false, false)
    val bitmap2 = arrayOf (bitmapSrc, bitmapSrc)
    val before2Ty = arrayOf (false, false)

    val config = generationConfig {
        maxOutputTokens = 4096
    }

    private lateinit var appTranslator: AppTranslator

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey,
        //generationConfig = config
    )

    var ix = Random.nextInt(8)

    private val generativeModel1 = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
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

    private fun translateText(
        context: Context,
        sourceText: String,
        sourceLang: String,
        targetLang: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator: Translator = Translation.getClient(options)

        // Download the model if needed
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                // Translate the text
                translator.translate(sourceText)
                    .addOnSuccessListener { translatedText ->
                        onSuccess(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun doGetOpenAI2Image(context: Context, prompt: String) {

        val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        _uiState.value = UiState.Loading

        if (sp.getLong("beforeTime", 0) > System.currentTimeMillis() - 1000 * 60 * 30)
            //&& sp.getString("prompt", "") == prompt )
        {
            _openAIUrl.value = sp.getString("beforeUrl", "") ?: ""
            _uiState.value = UiState.Success(_openAIUrl.value)
            return
        }
        appTranslator = AppTranslator(BuildConfig.GOOGLE_CLOUD_KEY)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.e("","doGetOpenAI2Image Request ${sdf.format(sp.getLong("beforeTime", 0))}" )
                val defaultPrompt = "Now we will create the base picture for the coloring activity that the kids will like. The picture should always be in an anime style and since the kids will be coloring, the base picture should be simple leaving only the important parts of the image. I want to make the background transparent and leave only the base picture of the picture."

                val transText = appTranslator.translateText(prompt, "en")
                Log.e("", "Translated Text: $transText")
                val request = ImageGenerationRequest(
                    model = "dall-e-3",
                    prompt = String.format("%s %s", defaultPrompt, transText),
                    n = 1,
                    size = "1024x1024"
                )

                RetrofitAPI.create().generateImage(request).enqueue(object : Callback<ImageGenerationResponse> {
                    override fun onResponse(
                        call: Call<ImageGenerationResponse>,
                        response: Response<ImageGenerationResponse>
                    ) {
                        if (response.isSuccessful) {
                            val imageResponse = response.body()
                            imageResponse?.data?.forEach {
                                Log.e("","Image URL: ${it.url}")
                                if (it.url.isNotEmpty()) {
                                    _openAIUrl.value = it.url
                                    val editor = sp.edit()
                                    editor.putString("prompt", prompt)
                                    editor.putLong("beforeTime", System.currentTimeMillis())
                                    editor.putString("beforeUrl", _openAIUrl.value)
                                    editor.apply()
                                }
                            }
                            _uiState.value = UiState.Success(_openAIUrl.value)
                        } else {
                            Log.e("","API Error: ${response.code()} - ${response.message()}")
                            _uiState.value = UiState.Error(response.message())
                        }
                    }

                    override fun onFailure(call: Call<ImageGenerationResponse>, t: Throwable) {
                        _uiState.value = UiState.Error("Error=${t.localizedMessage}")
                        t.printStackTrace()
                    }
                })

            } catch (e : Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Error=${e.localizedMessage}")
            }
        }
    }
}