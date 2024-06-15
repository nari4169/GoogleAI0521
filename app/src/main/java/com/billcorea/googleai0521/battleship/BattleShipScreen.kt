package com.billcorea.googleai0521.battleship

import android.content.Context
import android.graphics.BitmapFactory
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.UiState
import com.billcorea.googleai0521.baking.BakingViewModel
import com.billcorea.googleai0521.baking.images
import com.billcorea.googleai0521.ui.theme.light_background
import com.billcorea.googleai0521.ui.theme.light_inverseOnSurface
import com.billcorea.googleai0521.ui.theme.light_shadow
import com.billcorea.googleai0521.ui.theme.softBlue
import com.billcorea.googleai0521.ui.theme.typography
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun BattleShipScreen(bakingViewModel: BakingViewModel, function: () -> Unit) {

    var selectedCoordinate: Pair<Int, Int>? by remember { mutableStateOf(null) }
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp

    val placeholderPrompt = stringResource(R.string.battleship_description)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    val prompt2 = stringResource(R.string.answerPrompt)
    var answer by rememberSaveable { mutableStateOf("I am ready") }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val valWindowToken = LocalView.current.windowToken

    LazyColumn ( modifier = Modifier.fillMaxSize()) {
        item {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.battleship_title),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.battleship_description),
                    style = typography.bodyMedium,
                    modifier = Modifier.padding(3.dp)
                )

            }
        }

        items((0 until 5).toList()) { i ->
            Row (modifier = Modifier.fillMaxWidth()){
                for (j in 0 until 5) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (selectedCoordinate == Pair(i, j)) softBlue else light_background
                            )
                            .border(1.dp, Color.Black)
                            .clickable {
                                selectedCoordinate = Pair(i, j)
                                bakingViewModel.sendPromptBattleShip(
                                    prompt,
                                    "I am ready !!!",
                                    selectedCoordinate
                                )
                            },
                    ) {
                        Text(
                            text = "(${i}, ${j})",
                            modifier = Modifier.align(Alignment.Center),
                            color = if (selectedCoordinate == Pair(i, j)) light_background else softBlue
                        )
                    }
                }
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
                    label = { Text(stringResource(R.string.label_answer)) },
                    placeholder = { Text(stringResource(R.string.prompt_answer)) },
                    modifier = Modifier
                        .width((screenWidth * .7f).dp)
                        .padding(3.dp)
                )

                Button(
                    onClick = {

                        imm.hideSoftInputFromWindow(valWindowToken, 0)

                        bakingViewModel.sendPromptBattleShip(prompt2, answer, selectedCoordinate)
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