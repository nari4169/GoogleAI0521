package com.billcorea.googleai0521.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.billcorea.googleai0521.BuildConfig
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.ui.theme.light_background
import com.billcorea.googleai0521.ui.theme.light_surfaceTint
import com.billcorea.googleai0521.ui.theme.softBlue
import com.billcorea.googleai0521.ui.theme.transparency
import com.billcorea.googleai0521.ui.theme.typography

@Composable
fun TopScreen(
    doInformation:() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = String.format("Ver_%s", BuildConfig.VERSION_NAME), style = typography.bodyMedium)
//        IconButton(onClick = { doAdmob() }) {
//            Icon(imageVector = Icons.Outlined.MonetizationOn, contentDescription = "Money", tint = softBlue)
//        }
        IconButton(onClick = { doInformation() }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Information",
                tint = softBlue
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_banner),
            contentDescription = "App Icon",
            tint = softBlue,
            modifier = Modifier.width(48.dp).height(48.dp)
        )
    }
}