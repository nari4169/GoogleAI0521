package com.billcorea.googleai0521.colorring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.ui.theme.typography
import com.billcorea.googleai0521.viewModels.BakingViewModel
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun ColoringScreen (
    bakingViewModel: BakingViewModel
) {
    val config = LocalConfiguration.current
    val context = LocalContext.current
    val screenWidth = config.screenWidthDp
    var answer by rememberSaveable { mutableStateOf(context.getString(R.string.default_coloring_prompt)) }

    val placeholderPrompt = stringResource(R.string.coloring_description)
    val placeholderResult = stringResource(R.string.results_placeholder)
    val prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    val prompt2 = stringResource(R.string.answerPrompt)

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
                        bakingViewModel.doGetOpenAI2Image(answer)
                    },
                    enabled = answer.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = stringResource(R.string.action_go), style = typography.bodyMedium.copy(color= Color.White))
                }
            }
        }
    }
}