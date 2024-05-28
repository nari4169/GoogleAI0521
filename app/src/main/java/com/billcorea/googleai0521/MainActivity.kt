package com.billcorea.googleai0521

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billcorea.googleai0521.compose.DrawCanvasScreen
import com.billcorea.googleai0521.NavGraphs
import com.billcorea.googleai0521.destinations.BakingScreenDestination
import com.billcorea.googleai0521.destinations.DrawCanvasScreenDestination
import com.billcorea.googleai0521.ui.theme.GoogleAI0521Theme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable

class MainActivity : ComponentActivity() {
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

                    DestinationsNavHost(
                        engine = engine,
                        navController = navController,
                        navGraph = NavGraphs.root,
                        modifier = Modifier.padding(5.dp),
                        startRoute = startRoute
                    ) {
                        composable(BakingScreenDestination) {
                            BakingScreen(
                                bakingViewModel,
                                doGetPicture = { index ->
                                    destinationsNavigator.navigate(DrawCanvasScreenDestination)
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
                    }

                }
            }
        }
    }
}