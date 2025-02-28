package com.billcorea.googleai0521.retrofit

import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppTranslator(apiKey: String) {

    private val translate: Translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().service

    suspend fun translateText(text: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            val translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLanguage)
            )
            translation.translatedText
        }
    }
}