package com.billcorea.googleai0521.retrofit

import com.billcorea.googleai0521.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

interface OpenAIApiService {
    @Headers("Content-Type: application/json", "Authorization: Bearer ${BuildConfig.OPENAI_KEY}")
    @POST("v1/images/generations")
    fun generateImage(
        @Body request: ImageGenerationRequest
    ): Call<ImageGenerationResponse>

    companion object {

        val baseUrl = "https://api.openai.com/"

        private val client = OkHttpClient.Builder()
            .connectTimeout(1000 * 10, TimeUnit.MILLISECONDS)
            .readTimeout(1000 * 60 * 5, TimeUnit.MILLISECONDS)
            .build()

        val gson : Gson =   GsonBuilder().setLenient().create();
        /** 비어있는(length=0)인 Response를 받았을 경우 처리 */
        val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this
            override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object :
                Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
                override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                    try{
                        nextResponseBodyConverter.convert(value)
                    }catch (e:Exception){
                        e.printStackTrace()
                        null
                    }
                } else{
                    null
                }
            }
        }

        fun create(): OpenAIApiService {
            return Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(nullOnEmptyConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
                .create(OpenAIApiService::class.java)
        }
    }
}