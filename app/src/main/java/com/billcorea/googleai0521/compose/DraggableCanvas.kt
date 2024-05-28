package com.billcorea.googleai0521.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DraggableCanvas() {
    val color = remember { Color.Red }
    val radius = remember { 10f }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                offset = Offset(
                    x = offset.x + dragAmount.x,
                    y = offset.y + dragAmount.y
                )
                Log.e("","${dragAmount.x},${dragAmount.y}")
                // Consume the gesture event, not pass it on
                change.consume()
            }
        }
    ) {
        drawCircle(
            color = color,
            center = offset,
            radius = radius
        )
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun DraggableCanvas1() {
    val path = remember { Path() }
    val density = LocalDensity.current.density
    val color1 = remember { Color.Red }
    val radius = remember { 3f }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // val (x, y) = change.position
                    val x = offset.x + dragAmount.x
                    val y = offset.y + dragAmount.y
                    if (change.pressed) {
                        path.lineTo(x , y)
                    } else {
                        path.moveTo(x , y)
                    }
                    Log.e("", "$x, $y / $density")
                    offset = Offset(
                        x = offset.x + dragAmount.x,
                        y = offset.y + dragAmount.y
                    )
                    //Log.e("","${dragAmount.x},${dragAmount.y}")
                    // Consume the gesture event, not pass it on
                    offset = Offset(
                        x = offset.x + dragAmount.x,
                        y = offset.y + dragAmount.y
                    )
                    change.consume()
                }
            }
    ) {
        drawPath(path = path, color = color1)
    }
}


