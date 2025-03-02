package com.billcorea.googleai0521

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import com.billcorea.googleai0521.battleship.BattleShipScreen
import com.billcorea.googleai0521.colorring.ColoringScreen
import com.billcorea.googleai0521.compose.DrawCanvasScreen
import com.billcorea.googleai0521.compose.DrawCanvasScreen2
import com.billcorea.googleai0521.compose.TopScreen
import com.billcorea.googleai0521.destinations.BakingScreenDestination
import com.billcorea.googleai0521.destinations.BattleShipScreenDestination
import com.billcorea.googleai0521.destinations.ColoringScreenDestination
import com.billcorea.googleai0521.destinations.DrawCanvasScreen2Destination
import com.billcorea.googleai0521.destinations.DrawCanvasScreenDestination
import com.billcorea.googleai0521.destinations.ImageComparisonScreenDestination
import com.billcorea.googleai0521.imageComparison.ImageComparisonScreen
import com.billcorea.googleai0521.ui.theme.GoogleAI0521Theme
import com.billcorea.googleai0521.utils.AesCryptor
import com.billcorea.googleai0521.viewModels.BakingViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
                                        bakingViewModel.selectIdx.intValue = index
                                        destinationsNavigator.navigate(DrawCanvasScreenDestination)
                                    },
                                )
                            }
                            composable(BattleShipScreenDestination) {
                                BattleShipScreen(bakingViewModel) {

                                }
                            }
                            composable(ColoringScreenDestination) {
                                ColoringScreen(bakingViewModel, doPrint = {
                                    bitmap, context ->
                                    printBitmap(bitmap, context)
                                })
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

    private fun printBitmap(bitmap: Bitmap, context: Context) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = getString(R.string.app_name) + " Document"
        printManager.print(jobName, MyPrintDocumentAdapter(context, bitmap), null)
    }

    inner class MyPrintDocumentAdapter(private val context: Context, private val bitmap: Bitmap) :
        PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes,
            newAttributes: PrintAttributes,
            cancellationSignal: android.os.CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }

            val builder = PrintDocumentInfo.Builder("bitmap.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            val info = builder.build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: android.os.CancellationSignal,
            callback: WriteResultCallback
        ) {
            var output: FileOutputStream? = null
            val document = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = document.startPage(pageInfo)
                val pageCanvas = page.canvas
                pageCanvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)

                output = FileOutputStream(destination.fileDescriptor)
                document.writeTo(output)
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                Log.e("", "Error writing PDF", e)
                callback.onWriteFailed(e.localizedMessage)
            } finally {
                document.close()
                try {
                    output?.close()
                } catch (e: IOException) {
                    Log.e("", "Error closing output stream", e)
                }
            }
        }
    }
}