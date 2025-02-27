package com.billcorea.googleai0521.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.billcorea.googleai0521.BuildConfig
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.destinations.BakingScreenDestination
import com.billcorea.googleai0521.destinations.ColoringScreenDestination
import com.billcorea.googleai0521.destinations.DirectionDestination
import com.billcorea.googleai0521.destinations.ImageComparisonScreenDestination
import com.billcorea.googleai0521.ui.theme.softBlue
import com.billcorea.googleai0521.ui.theme.typography

@Composable
fun TopScreen(
    navController: NavHostController,
    doInformation: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(23.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
//        IconButton(onClick = { doAdmob() }) {
//            Icon(imageVector = Icons.Outlined.MonetizationOn, contentDescription = "Money", tint = softBlue)
//        }
        IconButton(
            onClick = {
                //doRunBaking()
                navController.navigate(NaviItem.Home.direction.route)
            },
        ) {
            Icon(painter = painterResource(R.drawable.ic_panorama_24), contentDescription = "Image Analogy", tint = softBlue)
        }
        IconButton(
            onClick = {
                //doRunImageComparison()
                navController.navigate(NaviItem.ImageComparison.direction.route)
            },
        ) {
            Icon(painter = painterResource(R.drawable.ic_compare_24), contentDescription = "Image Comparison", tint = softBlue)
        }
        IconButton(onClick = {
            navController.navigate(NaviItem.Coloring.direction.route)
        }) {
            Icon(painter = painterResource(R.drawable.ic_add_photo_alternate_24), contentDescription = "add_photo_alternate", tint = softBlue)
        }
        IconButton(onClick = { doInformation() }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Information",
                tint = softBlue
            )
        }
        Text(text = String.format("Ver_%s", BuildConfig.VERSION_NAME), style = typography.bodyMedium)
        Icon(
            painter = painterResource(id = R.drawable.ic_banner),
            contentDescription = "App Icon",
            tint = softBlue,
            modifier = Modifier.width(48.dp).height(48.dp)
        )
    }
}

enum class NaviItem(
    val direction: DirectionDestination,
    val icon: Int,
    val label: String
) {
    Home(BakingScreenDestination, R.drawable.ic_panorama_24, "R.string.Order"),
    ImageComparison(ImageComparisonScreenDestination, R.drawable.ic_compare_24, "R.string.productItems"),
    Coloring(ColoringScreenDestination, R.drawable.ic_add_photo_alternate_24, "R.string.productItems"),
}