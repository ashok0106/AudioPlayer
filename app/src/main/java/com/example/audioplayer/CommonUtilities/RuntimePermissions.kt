package com.example.audioplayer.CommonUtilities

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.audioplayer.CommonUtilities.LogPrint.PrintThisLog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RuntimePermission(
    permission: String,
    firstTimeShown:Boolean,
    updateFirstTime:()->Unit,
    permissionPermanentlyDenied: () -> Unit,
    permissionGranted:()->Unit
) {
    val scope = rememberCoroutineScope()
    val effects = remember { Channel<PermissionEffects>(Channel.UNLIMITED) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsState = rememberPermissionState(
        permission = permission
    )

    /**
     *  To store names for permissions that are permanently denied by the user.
     */

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    effects.send(PermissionEffects.AskPermission)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }

    })

    //check whether asking permission already access or not
    LaunchedEffect(key1 = effects.receiveAsFlow(), block = {
        effects.receiveAsFlow().onEach {
            when (it) {
                is PermissionEffects.AskPermission -> {
                    if(permissionsState.status.isGranted)
                        permissionGranted()
                    else if (!firstTimeShown && !permissionsState.status.shouldShowRationale) {
                        updateFirstTime()
                        PrintThisLog("inside firstTimeShown ")
                        permissionsState.launchPermissionRequest()
                    } else if(permissionsState.status.shouldShowRationale){
                        permissionsState.launchPermissionRequest()
                        PrintThisLog("inside sshouldShowRationale ")
                    } else {
                        PrintThisLog("inside permissionPermanentlyDenied ")
                        permissionPermanentlyDenied()
                    }
                }

                else -> {}
            }
        }.collect()
    })
}