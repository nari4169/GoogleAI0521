package com.example.googleai0521

import android.graphics.BitmapFactory
import android.graphics.Picture
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.googleai0521.compose.createBitmapFromPicture
import com.example.googleai0521.compose.saveToDisk
import com.example.googleai0521.compose.shareBitmap
import com.example.googleai0521.ui.theme.typography
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


val images = arrayOf(
    // Image generated using Gemini from the prompt "cupcake image"
    R.drawable.baked_goods_1,
    // Image generated using Gemini from the prompt "cookies images"
    R.drawable.baked_goods_2,
    // Image generated using Gemini from the prompt "cake images"
    R.drawable.baked_goods_3,
)
val imageDescriptions = arrayOf(
    R.string.image1_description,
    R.string.image2_description,
    R.string.image3_description,
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val color = remember { Color.Red }
    var points by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val picture = remember { Picture() }

    val writeStorageAccessState = rememberMultiplePermissionsState(
        // Android 10 이상에서는 공유 저장소에 파일을 추가하는 데 권한이 필요하지 않습니다.
        emptyList()
    )

    // 이 논리는 ViewModel에 있어야 합니다. URI 공유를 호출하는 부작용을 트리거합니다.
    // 부여된 권한을 확인한 다음 이미 콘텐츠를 캡처하고 있는 사진의 비트맵을 저장합니다.
    // 기본 공유 시트와 공유합니다.
    fun shareBitmapFromComposable(i: Int) {
        if (writeStorageAccessState.allPermissionsGranted) {
            coroutineScope.launch(Dispatchers.IO) {
                val bitmap = createBitmapFromPicture(picture)
                val uri = bitmap.saveToDisk(context)
                shareBitmap(context, uri)
            }
        } else if (writeStorageAccessState.shouldShowRationale) {
            coroutineScope.launch {
                val result = snackBarHostState.showSnackbar(
                    message = "The storage permission is needed to save the image",
                    actionLabel = "Grant Access"
                )

                if (result == SnackbarResult.ActionPerformed) {
                    writeStorageAccessState.launchMultiplePermissionRequest()
                }
            }
        } else {
            writeStorageAccessState.launchMultiplePermissionRequest()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.baking_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(images) { index, image ->
                    var imageModifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .requiredSize(200.dp)
                        .clickable {
                            selectedImage.intValue = index
                        }
                    if (index == selectedImage.intValue) {
                        imageModifier =
                            imageModifier.border(BorderStroke(4.dp, MaterialTheme.colorScheme.primary))
                    }
                    Image(
                        painter = painterResource(image),
                        contentDescription = stringResource(imageDescriptions[index]),
                        modifier = imageModifier
                    )

                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                TextField(
                    value = prompt,
                    label = { Text(stringResource(R.string.label_prompt)) },
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Button(
                    onClick = {
                        val bitmap = BitmapFactory.decodeResource(
                            context.resources,
                            images[selectedImage.intValue]
                        )
                        bakingViewModel.sendPrompt(bitmap, prompt)
                    },
                    enabled = prompt.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(R.string.action_go))
                }
            }
        }

        item  {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                Text(text = stringResource(id = R.string.msgStartDrag), style = typography.bodyMedium)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                        .drawWithCache {
                            // 렌더링을 Android 사진으로 리디렉션하는 방법을 보여주는 예입니다.
                            // 원래 대상에 그림을 그립니다.
                            val width = this.size.width.toInt()
                            val height = this.size.height.toInt()
                            onDrawWithContent {
                                val pictureCanvas =
                                    Canvas(
                                        picture.beginRecording(
                                            width,
                                            height
                                        )
                                    )
                                draw(this, this.layoutDirection, pictureCanvas, this.size) {
                                    this@onDrawWithContent.drawContent()
                                }
                                picture.endRecording()

                                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                            }
                        }
                        .pointerInput(key1 = Unit) {
                            detectDragGestures(
                                onDragStart = { touch ->
                                    points = listOf(touch)
                                },
                                onDrag = { change, _ ->
                                    val pointsFromHistory = change.historical
                                        .map { it.position }
                                        .toTypedArray()
                                    val newPoints = listOf(*pointsFromHistory, change.position)
                                    points = points + newPoints
                                },
                            )
                        }
                ) {
                    if (points.size > 1) {
                        val path = Path().apply {
                            val firstPoint = points.first()
                            val rest = points.subList(1, points.size - 1)

                            moveTo(firstPoint.x, firstPoint.y)
                            rest.forEach {
                                lineTo(it.x, it.y)
                            }
                        }

                        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))

                    }
                }
            }
        }

        item{
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.End
            ){
                IconButton(onClick = {
                    shareBitmapFromComposable(0)
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_one), "one")
                }
                IconButton(onClick = {
                    shareBitmapFromComposable(1)
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_two), "two")
                }
                IconButton(onClick = {
                    shareBitmapFromComposable(2)
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_three), "three")
                }

            }
        }

        item {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.wrapContentHeight())
            } else {
                var textColor = MaterialTheme.colorScheme.onSurface
                if (uiState is UiState.Error) {
                    textColor = MaterialTheme.colorScheme.error
                    result = (uiState as UiState.Error).errorMessage
                } else if (uiState is UiState.Success) {
                    textColor = MaterialTheme.colorScheme.onSurface
                    result = (uiState as UiState.Success).outputText
                }
//                val scrollState = rememberScrollState()
                Text(
                    text = result,
                    textAlign = TextAlign.Start,
                    color = textColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(16.dp)
                        .fillMaxSize()
                )
            }
        }
    }
}

fun drawToBitmap(): ImageBitmap {
    val drawScope = CanvasDrawScope()
    val size = Size(400f, 400f) // simple example of 400px by 400px image
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)

    drawScope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        // Draw whatever you want here; for instance, a white background and a red line.
        drawRect(color = Color.White, topLeft = Offset.Zero, size = size)
        drawLine(
            color = Color.Red,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
            strokeWidth = 5f
        )
    }
    return bitmap
}


