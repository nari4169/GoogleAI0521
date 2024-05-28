package com.billcorea.googleai0521.compose

import android.graphics.Bitmap
import android.graphics.Picture
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.BakingViewModel
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.ui.theme.typography
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class)
@Composable
@Destination
fun DrawCanvasScreen(
    navigator: DestinationsNavigator,
    bakingViewModel: BakingViewModel,
    doUpdateImageArray: (i: Int, bitmap: Bitmap, b: Boolean) -> Unit,
) {

    val color = remember { Color.Red }
    var points by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var picture = remember { Picture() }

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
                doUpdateImageArray(i, bitmap, true)
                Log.e("", "$i ${bakingViewModel.bitmaps.size}")
                //val uri = bitmap.saveToDisk(context)
                //shareBitmap(context, uri)
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
                    bakingViewModel.beforeTy[0] = false
                    bakingViewModel.beforeTy[1] = false
                    bakingViewModel.beforeTy[2] = false
                    navigator.popBackStack()
                }) {
                    Icon(imageVector = Icons.Outlined.Clear, "clear", tint=Color.Blue)
                }
                IconButton(onClick = {
                    shareBitmapFromComposable(0)
                    navigator.popBackStack()
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_one), "one", tint=Color.Blue)
                }
                IconButton(onClick = {
                    shareBitmapFromComposable(1)
                    navigator.popBackStack()
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_two), "two", tint=Color.Blue)
                }
                IconButton(onClick = {
                    shareBitmapFromComposable(2)
                    navigator.popBackStack()
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_three), "three", tint=Color.Blue)
                }

            }
        }
    }

}