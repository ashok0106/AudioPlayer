package com.example.audioplayer.AudioPlayer

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioplayer.CommonUtilities.LogPrint.PrintThisLog
import com.example.audioplayer.CommonUtilities.RuntimePermission
import com.example.audioplayer.CommonUtilities.redirectToSettings
import com.example.audioplayer.R
import com.example.audioplayer.visualizer.AudioVisualizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


@Composable
fun AudioScreen(
    viewModel: AudioPlayerViewModel = viewModel(),
    moveToHomeScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasAudioTrackPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionPermanentlyDenied by remember {
        mutableStateOf(false)
    }
    var firstTimeShown by remember {
        mutableStateOf(false)
    }
    if (!hasAudioTrackPermission && !permissionPermanentlyDenied && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RuntimePermission(
            permission = Manifest.permission.READ_MEDIA_AUDIO,
            firstTimeShown = firstTimeShown,
            updateFirstTime = { firstTimeShown = true },
            permissionGranted = { hasAudioTrackPermission = true },
            permissionPermanentlyDenied = {
                permissionPermanentlyDenied = true
            }
        )
    }
    HandlePermissionDeniedPermanently(
        permissionPermanentlyDenied = permissionPermanentlyDenied,
        hasAudioTrackPermission = hasAudioTrackPermission,
        context = context
    )
    if (hasAudioTrackPermission) {
        AudioPlayerApp(
            viewModel = viewModel,
            moveToHomeScreen = moveToHomeScreen
        )
    }
}

@Composable
fun HandlePermissionDeniedPermanently(
    permissionPermanentlyDenied: Boolean,
    hasAudioTrackPermission: Boolean,
    context: Context
) {
    if (permissionPermanentlyDenied && !hasAudioTrackPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        redirectToSettings(context)
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Access to audio files is required")
                Text(text = "Move to Settings")
            }
        }
    }


}

data class AudioFile(
    val id: Long, val title: String, val artist: String, val duration: Long
)


@Composable
fun AudioPlayerApp(
    viewModel: AudioPlayerViewModel,
    moveToHomeScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.release()
        moveToHomeScreen()
    }

//    var audioFiles by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
//    var selectedAudioFile by remember { mutableStateOf<AudioFile?>(null) }

    LaunchedEffect(Unit) {
        if (viewModel.audioFiles.isEmpty())
            viewModel.audioFiles = getAudioFiles(context)
    }
    LaunchedEffect(key1 = viewModel.audioData) {
        PrintThisLog("inside audio data")
    }
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue)
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier,
                    text = "Audio Player",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.surface
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(viewModel.audioFiles) { audioFile ->
                        AudioDetailRow(
                            albumArt = painterResource(id = R.drawable.ic_launcher_foreground),
                            title = audioFile.title,
                            artist = audioFile.artist,
                            duration = audioFile.duration.toString()
                        ) {
                            viewModel.selectedAudioFile = audioFile
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioFile.id
                            )
                            viewModel.playAudio(context, uri)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                viewModel.selectedAudioFile?.let {
                    Text(
                        "Now Playing: ${it.title}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AudioVisualizer(viewModel.audioData)
                Spacer(modifier = Modifier.height(16.dp))
                // Seekbar and time
                SeekbarWithTime(currentPosition = viewModel.currentPosition,
                    totalDuration = viewModel.totalDuration,
                    onSeek = { newPosition -> viewModel.seekTo(newPosition) })

                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                        viewModel.mediaPlayer?.let { viewModel.togglePlayPause() }
                    },
                    tint = if (viewModel.mediaPlayer == null) Color.Gray else Color.Black,
                    painter = painterResource(if (!viewModel.isPlaying) R.drawable.ic_play else R.drawable.ic_pause),
                    contentDescription = if (viewModel.isPlaying) "Pause" else "Play"
                )
            }
        }
    }
}

//@Composable
//fun AudioVisualizer(audioData: ByteArray) {
//    Canvas(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(100.dp)
//    ) {
//        val barWidth = size.width / audioData.size
//        audioData.forEachIndexed { index, byte ->
//            val barHeight = (byte.toFloat() + 128) / 256 * size.height
//            drawRect(
//                color = Color.Blue,
//                topLeft = Offset(index * barWidth, size.height - barHeight),
//                size = Size(barWidth, barHeight)
//            )
//        }
//    }
//}

suspend fun getAudioFiles(context: Context): List<AudioFile> = withContext(Dispatchers.IO) {
    val audioFiles = mutableListOf<AudioFile>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        "${MediaStore.Audio.Media.TITLE} ASC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn)
            val artist = cursor.getString(artistColumn)
            val duration = cursor.getLong(durationColumn)

            audioFiles.add(AudioFile(id, title, artist, duration))
        }
    }

    audioFiles
}


@Composable
fun AudioDetailRow(
    albumArt: Painter,  // Image for album art
    title: String,      // Song title
    artist: String,     // Artist name
    duration: String,   // Audio duration
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        Image(
            painter = albumArt,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Song Title, Artist, and Duration
        Column(
            modifier = Modifier
                .weight(1f) // Take available space
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration Text
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

    }
}

@Composable
fun SeekbarWithTime(
    currentPosition: Int,
    totalDuration: Int,
    onSeek: (Int) -> Unit
) {
    val currentTime = formatTime(currentPosition)
    val totalTime = formatTime(totalDuration)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seekbar
        Slider(
            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
            onValueChange = { progress ->
                val newPosition = (progress * totalDuration).toInt()
                onSeek(newPosition)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = currentTime, style = MaterialTheme.typography.bodyMedium)
            Text(text = totalTime, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun formatTime(ms: Int): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Composable
fun AudioFileItem(audioFile: AudioFile, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(audioFile.title, style = MaterialTheme.typography.bodyLarge)
        Text(audioFile.artist, style = MaterialTheme.typography.bodySmall)
    }
}
