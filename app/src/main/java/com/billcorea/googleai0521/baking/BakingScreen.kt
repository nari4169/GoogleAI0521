package com.billcorea.googleai0521.baking

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.compose.InAppUpdate
import com.billcorea.googleai0521.ui.theme.softBlue
import com.billcorea.googleai0521.ui.theme.typography
import com.ramcosta.composedestinations.annotation.Destination


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

@OptIn(ExperimentalFoundationApi::class)
@Destination(start = true)
@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel,
    doGetPicture:(index: Int) -> Unit,
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    val answerPrompt = stringResource(R.string.prompt_answer)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var answer by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val state = rememberLazyListState()
    val valWindowToken = LocalView.current.windowToken
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    InAppUpdate(context)

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
                    text = stringResource(R.string.baking_title),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.baking_description),
                    style = typography.bodyMedium,
                    modifier = Modifier.padding(3.dp)
                )

            }
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = state
            ) {
                itemsIndexed(images) { index, image ->
                    var imageModifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .requiredSize(200.dp)
                        .combinedClickable(
                            onClick = {
                                selectedImage.intValue = index
                            },
                            onDoubleClick = {
                                doGetPicture(index)
                            },
                            onLongClick = {
                                doGetPicture(index)
                            }
                        )
                    if (index == selectedImage.intValue) {
                        imageModifier =
                            imageModifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary))
                    }
                    Log.e("", "$index, ${bakingViewModel.beforeTy[index]}")
                    if (bakingViewModel.beforeTy[index]) {
                        Image(
                            bitmap = bakingViewModel.bitmaps[index].asImageBitmap(),
                            contentDescription = stringResource(imageDescriptions[index]),
                            modifier = imageModifier
                        )
                    } else {
                        Image(
                            painter = painterResource(image),
                            contentDescription = stringResource(imageDescriptions[index]),
                            modifier = imageModifier
                        )
                    }
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
                    maxLines = 3,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Button(
                    onClick = {

                        imm.hideSoftInputFromWindow(valWindowToken, 0)

                        for(ix in 0 .. 2) {
                            val bitmap = BitmapFactory.decodeResource(
                                context.resources,
                                images[ix]
                            )
                            if (!bakingViewModel.beforeTy[ix]) {
                                bakingViewModel.bitmaps[ix] = bitmap
                            }
                        }
                        if (answer.isEmpty()) answer = "Empty"
                        bakingViewModel.sendPrompt(bakingViewModel.bitmaps, prompt, answer)
                    },
                    enabled = prompt.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(R.string.action_go), style = typography.bodyMedium.copy(color= Color.White))
                }
            }
        }

        item {
            OutlinedTextField(
                value = answer,
                onValueChange = {
                    answer = it
                },
                label = { Text(stringResource(R.string.label_answer)) },
                placeholder = { Text(stringResource(R.string.prompt_answer)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            )
        }

        item {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
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


