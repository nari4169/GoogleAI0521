package com.example.googleai0521.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import java.io.File
import android.Manifest
import android.content.Intent
import android.content.Intent.createChooser
import android.media.MediaScannerConnection
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.googleai0521.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun BitmapFromComposableSnippet() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val picture = remember { Picture() }

    val writeStorageAccessState = rememberMultiplePermissionsState(
        // Android 10 이상에서는 공유 저장소에 파일을 추가하는 데 권한이 필요하지 않습니다.
        emptyList()
    )

    // 이 논리는 ViewModel에 있어야 합니다. URI 공유를 호출하는 부작용을 트리거합니다.
    // 부여된 권한을 확인한 다음 이미 콘텐츠를 캡처하고 있는 사진의 비트맵을 저장합니다.
    // 기본 공유 시트와 공유합니다.
    fun shareBitmapFromComposable() {
        if (writeStorageAccessState.allPermissionsGranted) {
            coroutineScope.launch(Dispatchers.IO) {
                val bitmap = createBitmapFromPicture(picture)
                val uri = bitmap.saveToDisk(context)
                shareBitmap(context, uri)
            }
        } else if (writeStorageAccessState.shouldShowRationale) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                shareBitmapFromComposable()
            }) {
                Icon(Icons.Default.Share, "share")
            }
        }
    ) { padding ->
        // [START android_compose_draw_into_bitmap]
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .drawWithCache {
                    // Example that shows how to redirect rendering to an Android Picture and then
                    // draw the picture into the original destination
                    val width = this.size.width.toInt()
                    val height = this.size.height.toInt()
                    onDrawWithContent {
                        val pictureCanvas =
                            androidx.compose.ui.graphics.Canvas(
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
        ) {
            ScreenContentToCapture()
        }
        // [END android_compose_draw_into_bitmap]
    }
}

@Composable
private fun ScreenContentToCapture() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFF5D5C0),
                        Color(0xFFF8E8E3)
                    )
                )
            )
    ) {
        Image(
            painterResource(id = R.drawable.ic_banner),
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .padding(32.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            "Into the Ocean depths",
            fontSize = 18.sp
        )
    }
}

fun createBitmapFromPicture(picture: Picture): Bitmap {

    val pictureWidth = picture.width
    val pictureHeight = picture.height
    val bitmap = Bitmap.createBitmap(
        picture.width,
        picture.height,
        Bitmap.Config.ARGB_8888
    )

    if (pictureWidth <= 0 || pictureHeight <= 0) {
        Log.e("TAG", "Picture width and height must be greater than 0")

    } else {

        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPicture(picture)

    }

    return bitmap
}

suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.jpg"
    )

    file.writeBitmap(this, Bitmap.CompressFormat.JPEG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}


private suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

fun shareBitmap(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(context, createChooser(intent, "Share your image"), null)
}