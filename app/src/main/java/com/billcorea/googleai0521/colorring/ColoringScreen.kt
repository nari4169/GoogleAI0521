package com.billcorea.googleai0521.colorring

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.ui.theme.colorScheme
import com.billcorea.googleai0521.ui.theme.typography
import com.billcorea.googleai0521.viewModels.BakingViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Rotate
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import java.util.LinkedList
import java.util.Queue

@SuppressLint("StateFlowValueCalledInComposition")
@Destination
@Composable
fun ColoringScreen (
    bakingViewModel: BakingViewModel
) {
    val config = LocalConfiguration.current
    val context = LocalContext.current
    val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    var answer by rememberSaveable { mutableStateOf(sp.getString("prompt", context.getString(R.string.default_coloring_prompt)).toString() ) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val openAIUrl by bakingViewModel.openAIUrl.collectAsState()
    val placeholderPrompt = stringResource(R.string.coloring_description)
    val placeholderResult = stringResource(R.string.results_placeholder)
    val prompt by rememberSaveable { mutableStateOf(sp.getString("prompt", placeholderPrompt).toString()) }
    val prompt2 = stringResource(R.string.answerPrompt)

    val screenHeight = config.screenHeightDp
    val screenWidth = config.screenWidthDp

    if (openAIUrl.isEmpty()) {
        bakingViewModel._openAIUrl.value = sp.getString("beforeUrl", "") ?: ""
    }

    fun getRotation(context: Context, imagePath: String): Float {
        return try {
            val exif = ExifInterface(imagePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0f
        }
    }

    val bitmapFlow = remember(openAIUrl) {
        callbackFlow {
            val target = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    trySend(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            }
            val rotation = getRotation(context, openAIUrl)
            val requestOptions = RequestOptions().transform(Rotate(rotation.toInt()))
            Glide.with(context)
                .asBitmap()
                .load(openAIUrl)
                .override((screenWidth * .8).toInt(), (screenHeight * .6).toInt())
                .apply(requestOptions)
                .into(target)
            awaitClose { Glide.with(context).clear(target) }
        }
    }
    val bitmap by bitmapFlow.collectAsState(initial = null)

    lateinit var bitmapState : MutableState<Bitmap?>
    lateinit var imageBitmap: ImageBitmap
    if (bitmap != null) {
        bitmapState = remember { mutableStateOf(bitmap?.copy(Bitmap.Config.ARGB_8888, true)) }
        imageBitmap = bitmapState.value!!.asImageBitmap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.coloring_title),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.coloring_description),
                    style = typography.bodyMedium,
                    modifier = Modifier.padding(3.dp)
                )

            }
        }

        item {
            Row(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                OutlinedTextField(
                    value = answer,
                    onValueChange = {
                        answer = it
                    },
                    label = { Text(stringResource(R.string.coloring_title)) },
                    placeholder = { Text(stringResource(R.string.prompt_color)) },
                    modifier = Modifier
                        .width((screenWidth * .7f).dp)
                        .padding(3.dp)
                )

                Button(
                    onClick = {
                        bakingViewModel.doGetOpenAI2Image(context, answer)
                    },
                    enabled = answer.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(R.string.action_go), style = typography.bodyMedium.copy(color= Color.White))
                }
            }
        }

        item {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                bitmap?.let {
                    bakingViewModel._uiState.value = UiState.Success("Image loaded")
                    Canvas(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .fillMaxWidth()
                            .padding(5.dp)
                            .border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp))
                            .height((screenHeight * .6).dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { offset ->
                                        Log.e("", "onLongPress ... ${offset.x}, ${offset.y}")

                                        val canvasWidth = size.width
                                        val canvasHeight = size.height

                                        val imageWidth = imageBitmap.width.toFloat()
                                        val imageHeight = imageBitmap.height.toFloat()

                                        val canvasRatio = canvasWidth / canvasHeight
                                        val imageRatio = imageWidth / imageHeight

                                        // 90% 크기 계산
                                        val scaledCanvasWidth = canvasWidth * 0.9f
                                        val scaledCanvasHeight = canvasHeight * 0.9f

                                        val scaledImageWidth: Float
                                        val scaledImageHeight: Float
                                        val offsetX: Float
                                        val offsetY: Float

                                        if (imageRatio > canvasRatio) {
                                            // 이미지가 더 넓은 경우
                                            scaledImageHeight = scaledCanvasHeight
                                            scaledImageWidth = scaledImageHeight * imageRatio
                                            offsetX = (canvasWidth - scaledImageWidth) / 2 + ((canvasWidth - scaledCanvasWidth) / 2)
                                            offsetY = (canvasHeight - scaledImageHeight) / 2
                                        } else if (imageRatio < canvasRatio) {
                                            // 이미지가 더 높은 경우
                                            scaledImageWidth = scaledCanvasWidth
                                            scaledImageHeight = scaledImageWidth / imageRatio
                                            offsetX = (canvasWidth - scaledImageWidth) / 2
                                            offsetY = (canvasHeight - scaledImageHeight) / 2
                                        } else {
                                            // 이미지와 Canvas의 비율이 같은 경우
                                            scaledImageWidth = scaledCanvasWidth
                                            scaledImageHeight = scaledCanvasHeight
                                            offsetX = (canvasWidth - scaledCanvasWidth) / 2
                                            offsetY = (canvasHeight - scaledCanvasHeight) / 2
                                        }

                                        // 클릭 위치를 이미지 좌표계로 변환
                                        val imageX = ((offset.x - offsetX) / (scaledImageWidth / imageWidth)).toInt()
                                        val imageY = ((offset.y - offsetY) / (scaledImageHeight / imageHeight)).toInt()

                                        if (imageX in 0 until bitmapState.value!!.width && imageY in 0 until bitmapState.value!!.height) {
                                            val newBitmap = bitmapState.value!!.copy(Bitmap.Config.ARGB_8888, true)
                                            floodFill(newBitmap, imageX, imageY, R.color.purple_500)
                                            bitmapState.value = newBitmap
                                        }
                                    }
                                )
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val imageWidth = imageBitmap.width.toFloat()
                        val imageHeight = imageBitmap.height.toFloat()

                        val canvasRatio = canvasWidth / canvasHeight
                        val imageRatio = imageWidth / imageHeight

                        // 90% 크기 계산
                        val scaledCanvasWidth = canvasWidth * 0.9f
                        val scaledCanvasHeight = canvasHeight * 0.9f

                        val srcSize: IntSize
                        val dstSize: IntSize
                        val srcOffset: IntOffset
                        val dstOffset: IntOffset

                        if (imageRatio > canvasRatio) {
                            // 이미지가 더 넓은 경우
                            val scaledImageHeight = scaledCanvasHeight
                            val scaledImageWidth = scaledImageHeight * imageRatio
                            srcSize = IntSize(imageWidth.toInt(), imageHeight.toInt())
                            dstSize = IntSize(scaledImageWidth.toInt(), scaledImageHeight.toInt())
                            srcOffset = IntOffset(
                                ((imageWidth - (imageHeight * canvasRatio)) / 2).toInt(),
                                0
                            )
                            dstOffset = IntOffset(
                                ((canvasWidth - scaledImageWidth) / 2).toInt() + ((canvasWidth - scaledCanvasWidth) / 2).toInt(),
                                ((canvasHeight - scaledImageHeight) / 2).toInt()
                            )
                        } else if (imageRatio < canvasRatio) {
                            // 이미지가 더 높은 경우
                            val scaledImageWidth = scaledCanvasWidth
                            val scaledImageHeight = scaledImageWidth / imageRatio
                            srcSize = IntSize(imageWidth.toInt(), imageHeight.toInt())
                            dstSize = IntSize(scaledImageWidth.toInt(), scaledImageHeight.toInt())
                            srcOffset = IntOffset(
                                0,
                                ((imageHeight - (imageWidth / canvasRatio)) / 2).toInt()
                            )
                            dstOffset = IntOffset(
                                ((canvasWidth - scaledImageWidth) / 2).toInt(),
                                ((canvasHeight - scaledImageHeight) / 2).toInt()
                            )
                        } else {
                            // 이미지와 Canvas의 비율이 같은 경우
                            srcSize = IntSize(imageWidth.toInt(), imageHeight.toInt())
                            dstSize = IntSize(scaledCanvasWidth.toInt(), scaledCanvasHeight.toInt())
                            srcOffset = IntOffset(0, 0)
                            dstOffset = IntOffset(((canvasWidth - scaledCanvasWidth) / 2).toInt(), ((canvasHeight - scaledCanvasHeight) / 2).toInt())
                        }

                        drawImage(
                            image = imageBitmap,
                            srcOffset = srcOffset,
                            srcSize = srcSize,
                            dstOffset = dstOffset,
                            dstSize = dstSize
                        )
                    }

                } ?: run {
                    bakingViewModel._uiState.value = UiState.Error("No image found")
                    Text(text = stringResource(R.string.results_placeholder), style = typography.bodyMedium)
                }
            }
        }
    }
}

fun floodFill(bitmap: Bitmap, x: Int, y: Int, fillColor: Int) {
    val targetColor = bitmap.getPixel(x, y)
    if (android.graphics.Color.alpha(targetColor) != 0) return // 투명하지 않으면 종료

    val width = bitmap.width
    val height = bitmap.height
    val queue: Queue<Pair<Int, Int>> = LinkedList()
    queue.offer(Pair(x, y))

    while (queue.isNotEmpty()) {
        val (currentX, currentY) = queue.poll()

        if (currentX < 0 || currentX >= width || currentY < 0 || currentY >= height) continue

        val currentColor = bitmap.getPixel(currentX, currentY)
        if (android.graphics.Color.alpha(currentColor) == 0) {
            bitmap.setPixel(currentX, currentY, fillColor)

            queue.offer(Pair(currentX + 1, currentY))
            queue.offer(Pair(currentX - 1, currentY))
            queue.offer(Pair(currentX, currentY + 1))
            queue.offer(Pair(currentX, currentY - 1))
        }
    }
}