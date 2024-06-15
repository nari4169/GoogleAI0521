package com.billcorea.googleai0521.imageComparison

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.baking.BakingViewModel
import com.billcorea.googleai0521.baking.imageDescriptions
import com.billcorea.googleai0521.ui.theme.softBlue
import com.billcorea.googleai0521.ui.theme.typography
import com.ramcosta.composedestinations.annotation.Destination
import kotlin.random.Random

val imagesL = arrayOf(
    R.drawable.d_1_left,
    R.drawable.d_2_left,
    R.drawable.d_3_left,
    R.drawable.d_4_left,
    R.drawable.d_5_left,
    R.drawable.d_6_left,
    R.drawable.d_7_left,
    R.drawable.d_8_left,
)
val imagesR = arrayOf(
    R.drawable.d_1_right,
    R.drawable.d_2_right,
    R.drawable.d_3_right,
    R.drawable.d_4_right,
    R.drawable.d_5_right,
    R.drawable.d_6_right,
    R.drawable.d_7_right,
    R.drawable.d_8_right,
)

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun ImageComparisonScreen (
    bakingViewModel: BakingViewModel,
    doGetDrawImage:(idx: Int, leftRight: String) -> Unit
) {

    val placeholderPrompt = stringResource(R.string.prompt_comparison)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var answer by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val valWindowToken = LocalView.current.windowToken
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    var bitmapL = BitmapFactory.decodeResource(
        context.resources,
        imagesL[bakingViewModel.ix]
    )
    var bitmapR = BitmapFactory.decodeResource(
        context.resources,
        imagesR[bakingViewModel.ix]
    )

    var leftImage by remember { mutableIntStateOf(imagesL[bakingViewModel.ix]) }
    var rightImage by remember { mutableIntStateOf(imagesL[bakingViewModel.ix]) }

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
                    text = stringResource(R.string.comparsion_title),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.comparsion_description),
                    style = typography.bodyMedium,
                    modifier = Modifier.padding(3.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.padding(all = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageModifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .width(screenWidth.dp /  2 * .8f)
                    .height(screenWidth.dp / 2 * .8f)

                val leftModifier = imageModifier.combinedClickable(
                    onClick = {

                    },
                    onDoubleClick = {
                        doGetDrawImage(bakingViewModel.ix, "Left")
                    },
                    onLongClick = {
                        doGetDrawImage(bakingViewModel.ix, "Left")
                    }
                )

                val rightModifier = imageModifier.combinedClickable(
                    onClick = {

                    },
                    onDoubleClick = {
                        doGetDrawImage(bakingViewModel.ix, "Right")
                    },
                    onLongClick = {
                        doGetDrawImage(bakingViewModel.ix, "Right")
                    }
                )

                if (bakingViewModel.before2Ty[0]) {
                    Image(
                        bitmap = bakingViewModel.bitmap2[0].asImageBitmap(),
                        contentDescription = "Comparison Image",
                        modifier = leftModifier
                    )
                } else {
                    Image(
                        painter = painterResource(leftImage),
                        contentDescription = "Comparison Image",
                        modifier = leftModifier
                    )
                }

                if (bakingViewModel.before2Ty[1]) {
                    Image(
                        bitmap = bakingViewModel.bitmap2[1].asImageBitmap(),
                        contentDescription = "Comparison Image",
                        modifier = rightModifier
                    )
                } else {
                    Image(
                        painter = painterResource(rightImage),
                        contentDescription = "Comparison Image",
                        modifier = rightModifier
                    )
                }

                IconButton(
                    onClick = {
                        bakingViewModel.ix = Random.nextInt(8)
                        Log.e("","ix=${bakingViewModel.ix}")
                        leftImage = imagesL[bakingViewModel.ix]
                        rightImage = imagesR[bakingViewModel.ix]
                        bakingViewModel.before2Ty[0] = false
                        bakingViewModel.before2Ty[1] = false
                    },
                ) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Image Refresh", tint = softBlue)
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
                        if (answer.isEmpty()) answer = "Empty"
                        if (bakingViewModel.before2Ty[0]) {
                            bitmapL = bakingViewModel.bitmap2[0]
                        }
                        if (bakingViewModel.before2Ty[1]) {
                            bitmapR = bakingViewModel.bitmap2[1]
                        }
                        bakingViewModel.sendPrompt2(bitmapL, bitmapR, prompt, answer)
                    },
                    enabled = prompt.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(R.string.action_go), style = typography.bodyMedium.copy(color= Color.White))
                }
            }
        }

//        item {
//            OutlinedTextField(
//                value = answer,
//                onValueChange = {
//                    answer = it
//                },
//                label = { Text(stringResource(R.string.label_answer)) },
//                placeholder = { Text(stringResource(R.string.prompt_answer)) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(3.dp)
//            )
//        }

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