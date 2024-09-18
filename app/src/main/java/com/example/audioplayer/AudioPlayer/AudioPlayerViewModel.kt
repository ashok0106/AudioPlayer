package com.example.audioplayer.AudioPlayer

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.CommonUtilities.LogPrint.PrintThisLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel() : ViewModel() {
    var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    var isPlaying by mutableStateOf(false)
    var audioData by mutableStateOf(ByteArray(0))
    var audioFiles by  mutableStateOf<List<AudioFile>>(emptyList())
    var selectedAudioFile by  mutableStateOf<AudioFile?>(null)
    var currentPosition by mutableStateOf(0) // Current playback position
    var totalDuration by mutableStateOf(0)   // Total audio duration

    init {
        PrintThisLog("AudioPlayerViewModel init")
        PrintThisLog("isPlaying ${isPlaying}")
    }
    fun playAudio(context: Context, uri: Uri) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            start()
            this@AudioPlayerViewModel.isPlaying = true
            totalDuration = duration // Set total duration
        }
        updateCurrentPosition()
        setupVisualizer()
    }


    fun togglePlayPause() {
        viewModelScope.launch {
            if (isPlaying) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
            updateCurrentPosition()
            isPlaying = !isPlaying
        }
    }
    fun updateCurrentPosition() {
        viewModelScope.launch {
            try {
                while (isPlaying && isActive) {
                    this@AudioPlayerViewModel.currentPosition = this@AudioPlayerViewModel.mediaPlayer?.currentPosition ?: 0
                    if(this@AudioPlayerViewModel.currentPosition >= this@AudioPlayerViewModel.totalDuration){
                        mediaPlayer?.pause()
                        isPlaying = false
                    }
                    delay(100) // Update every 100ms
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun setupVisualizer() {
        viewModelScope.launch {
            visualizer?.release()
            visualizer = mediaPlayer?.audioSessionId?.let { Visualizer(it) }

            visualizer = mediaPlayer?.let {
                Visualizer(it.audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(
                        object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                visualizer: Visualizer,
                                waveform: ByteArray,
                                samplingRate: Int
                            ) {
                                audioData = waveform
                            }

                            override fun onFftDataCapture(
                                visualizer: Visualizer,
                                fft: ByteArray,
                                samplingRate: Int
                            ) {
//                                audioData = fft
                            }
                        },
                        Visualizer.getMaxCaptureRate() / 2,
                        true,
                        false
                    )
                    enabled = true
                }
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        visualizer?.release()
        mediaPlayer = null
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        currentPosition = position

    }
}
class AudioPlayer {
    var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    var isPlaying by mutableStateOf(false)
    var audioData by mutableStateOf(ByteArray(0))

    fun playAudio(context: Context, uri: Uri) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            start()
            this@AudioPlayer.isPlaying = true
        }
        setupVisualizer()
    }

    fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        isPlaying = !isPlaying
    }

    fun setupVisualizer() {
        visualizer?.release()
        PrintThisLog("${mediaPlayer?.audioSessionId}")
        visualizer = mediaPlayer?.audioSessionId?.let { Visualizer(it) }

        visualizer = mediaPlayer?.let {
            Visualizer(it.audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer,
                            waveform: ByteArray,
                            samplingRate: Int
                        ) {
                            audioData = waveform
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer,
                            fft: ByteArray,
                            samplingRate: Int
                        ) {

                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                enabled = true
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        visualizer?.release()
    }
}