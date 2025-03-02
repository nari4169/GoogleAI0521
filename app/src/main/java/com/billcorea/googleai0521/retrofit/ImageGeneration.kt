package com.billcorea.googleai0521.retrofit

data class ImageGenerationRequest(
    val model: String,
    val prompt: String,
    val n: Int,
    val size: String
)

data class ImageGenerationResponse(
    val data: List<ImageData>
)

data class ImageData(
    val imageURL: String
)