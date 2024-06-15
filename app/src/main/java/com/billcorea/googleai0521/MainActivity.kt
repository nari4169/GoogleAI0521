package com.billcorea.googleai0521

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billcorea.googleai0521.baking.BakingScreen
import com.billcorea.googleai0521.baking.BakingViewModel
import com.billcorea.googleai0521.battleship.BattleShipScreen
import com.billcorea.googleai0521.compose.DrawCanvasScreen
import com.billcorea.googleai0521.compose.DrawCanvasScreen2
import com.billcorea.googleai0521.compose.TopScreen
import com.billcorea.googleai0521.destinations.BakingScreenDestination
import com.billcorea.googleai0521.destinations.BattleShipScreenDestination
import com.billcorea.googleai0521.destinations.DrawCanvasScreen2Destination
import com.billcorea.googleai0521.destinations.DrawCanvasScreenDestination
import com.billcorea.googleai0521.destinations.ImageComparisonScreenDestination
import com.billcorea.googleai0521.imageComparison.ImageComparisonScreen
import com.billcorea.googleai0521.ui.theme.GoogleAI0521Theme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val bakingViewModel: BakingViewModel = viewModel()
            val engine = rememberAnimatedNavHostEngine()
            val navController = engine.rememberNavController()
            val startRoute = NavGraphs.root.startRoute

            GoogleAI0521Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        topBar = {
                            TopScreen(
                                navController = navController,
                                doInformation ={
                                    val url = "https://billcorea.tistory.com/501"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    startActivity(intent)
                                },
                            )
                        }
                    ) { paddingValue ->
                        DestinationsNavHost(
                            engine = engine,
                            navController = navController,
                            navGraph = NavGraphs.root,
                            modifier = Modifier.padding(paddingValue),
                            startRoute = startRoute
                        ) {
                            composable(BakingScreenDestination) {
                                BakingScreen(
                                    bakingViewModel,
                                    doGetPicture = { index ->
                                        bakingViewModel.selectIdx.value = index
                                        destinationsNavigator.navigate(DrawCanvasScreenDestination)
                                    },
                                )
                            }
                            composable(BattleShipScreenDestination) {
                                BattleShipScreen(bakingViewModel) {

                                }
                            }
                            composable(ImageComparisonScreenDestination) {
                                ImageComparisonScreen(
                                    bakingViewModel,
                                    doGetDrawImage = { idx, leftRight ->
                                        bakingViewModel.idx.intValue = idx
                                        bakingViewModel.leftRight.value = leftRight
                                        destinationsNavigator.navigate(DrawCanvasScreen2Destination)
                                    }
                                )
                            }
                            composable(DrawCanvasScreenDestination) {
                                DrawCanvasScreen(
                                    destinationsNavigator,
                                    bakingViewModel,
                                    doUpdateImageArray = { index, bitmap, ty ->
                                        bakingViewModel.doUpdateImageArray(index, bitmap, ty)
                                    }
                                )
                            }
                            composable(DrawCanvasScreen2Destination) {
                                DrawCanvasScreen2(
                                    destinationsNavigator,
                                    bakingViewModel,
                                    doUpdateImageArray = { index, leftRight, bitmap, ty ->
                                        bakingViewModel.doUpdateImage2(index, leftRight, bitmap, ty)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}