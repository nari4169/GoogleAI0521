package com.billcorea.googleai0521.compose

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.billcorea.googleai0521.R
import com.billcorea.googleai0521.ui.theme.typography
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

@Composable
fun InAppUpdate(context: Context) {
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->

        Log.e("", "launcher ...${result.resultCode}")

        if (result.resultCode != Activity.RESULT_OK) {
            Log.e("", "Update flow failed! Result code: ${result.resultCode}")
        }
    }

    Log.e("", "InAppUpdate ...")

    // Checks that the platform will allow the specified type of update.
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        val UPDATE_DATE_LIMIT = 1

        Log.e("", "addOnSuccessListener ...${appUpdateInfo.updateAvailability()}")

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            // 이 예에서는 즉시 업데이트를 적용합니다. IMMEDIATE
            // 유연한 업데이트를 적용하려면 대신 AppUpdateType.FLEXIBLE을 전달합니다.
            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            //&& (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= UPDATE_DATE_LIMIT
        ) {
            Log.e("", "Update available !!!")
            // Request the update.
            appUpdateManager.startUpdateFlowForResult(
                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                appUpdateInfo,
                // an activity result launcher registered via registerForActivityResult
                launcher,
                // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                // flexible updates.
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
        }
    }

}