package com.example.audioplayer.AudioRecorder


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioplayer.AudioPlayer.HandlePermissionDeniedPermanently
import com.example.audioplayer.CommonUtilities.RuntimePermission


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AudioRecorderScreen(
    viewModel: AudioRecorderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasAudioRecordPermission by remember {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
//        } else mutableStateOf(true)
    }
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }
    var firstTimeShown by remember { mutableStateOf(false) }
    if (!hasAudioRecordPermission && !permissionPermanentlyDenied && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RuntimePermission(
            permission = Manifest.permission.RECORD_AUDIO,
            firstTimeShown = firstTimeShown,
            updateFirstTime = { firstTimeShown = true },
            permissionGranted = { hasAudioRecordPermission = true },
            permissionPermanentlyDenied = {
                permissionPermanentlyDenied = true
            }
        )
    }
    HandlePermissionDeniedPermanently(
        permissionPermanentlyDenied = permissionPermanentlyDenied,
        hasAudioTrackPermission = hasAudioRecordPermission,
        context = context
    )
    if (hasAudioRecordPermission) {
        AudioRecorderApp(audioRecorderViewModel = viewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AudioRecorderApp(audioRecorderViewModel: AudioRecorderViewModel ) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Record Button
        Button(
            onClick = {
                if (!audioRecorderViewModel.isRecording) {
                    audioRecorderViewModel.startRecording(context)
                } else {
                    audioRecorderViewModel.stopRecording()
                    Toast.makeText(context, "Recording saved!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = if (audioRecorderViewModel.isRecording) "Stop Recording" else "Start Recording")
        }

        // Play Button
        Button(
            onClick = {
                if (!audioRecorderViewModel.isPlaying) {
                    audioRecorderViewModel.playRecording()
                } else {
                    audioRecorderViewModel.stopPlaying()
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = if (audioRecorderViewModel.isPlaying) "Stop Playing" else "Play Recording")
        }

        // Save to Recordings Folder
        Button(
            onClick = {
                audioRecorderViewModel.saveRecordingToFile(context)
                Toast.makeText(context, "Recording saved to Recordings folder!", Toast.LENGTH_SHORT)
                    .show()
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Save Recording")
        }
    }
}


//@Composable
//fun AudioRecorderWithVisualizer(viewModel: AudioRecorderViewModel ) {
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        AudioVisualizer(audioData = viewModel.audioData.value)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                if (viewModel.isRecording) {
//                    viewModel.stopRecording()
//                } else {
//                    viewModel.startRecording()
//                }
//            }
//        ) {
//            Text(if (viewModel.isRecording) "Stop Recording" else "Start Recording")
//        }
//    }
//}
//@Composable
//fun AudioRecorderScreen(viewModel: AudioRecorderViewModel) {
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Button(onClick = {
//            if (!viewModel.isRecording) {
//                viewModel.startRecording()
//            } else {
//                viewModel.stopRecording()
//                viewModel.saveRecordingToFile(context)
//            }
//        }) {
//            Text(if (viewModel.isRecording) "Stop Recording" else "Start Recording")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (viewModel.isRecording) {
//            AudioVisualizer(viewModel.amplitudeData)
//        }
//    }
//}

@Composable
fun AudioVisualizer(amplitudeData: IntArray) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 16.dp)
    ) {
        val width = size.width
        val barWidth = width / amplitudeData.size.toFloat()
        val maxBarHeight = size.height * 0.7f

        amplitudeData.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude / Short.MAX_VALUE.toFloat()) * maxBarHeight
            drawRect(
                color = Color.Blue,
                topLeft = Offset(index * barWidth, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
