package com.example.audioplayer.AudioRecorder

import android.content.ContentValues
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStream

class AudioRecorderViewModel : ViewModel() {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var isRecording by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var recordingFilePath by mutableStateOf("")

    private var recordingJob: Job? = null

    // Function to start recording audio in .3gp format
    fun startRecording(context: Context) {
        val fileName = "recording_${System.currentTimeMillis()}.3gp"
        val recordingFile =
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/$fileName"
        recordingFilePath = recordingFile

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)

            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isRecording) {
                // You can add visualizer updates here if needed
            }
        }
    }

    // Stop recording and save to file
    fun stopRecording() {
        isRecording = false
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        recordingJob?.cancel()
    }

    // Play the recorded audio
    fun playRecording() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(recordingFilePath)
                prepare()
                start()
                this@AudioRecorderViewModel.isPlaying = true

                setOnCompletionListener {
                    stopPlaying()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Stop playing audio
    fun stopPlaying() {
        isPlaying = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Save recording to a visible folder in the user's storage
    fun saveRecordingToFile(context: Context) {
        val contentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "recording_${System.currentTimeMillis()}.3gp")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Recordings")
        }

        val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(uri).use { output ->
                if (output != null) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.copyTo(output)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        stopPlaying()
    }
}


class AudioRecorderViewModel5 : ViewModel() {

    private var audioRecorder: AudioRecord? = null
    private var recordingJob: Job? = null
    var isRecording by mutableStateOf(false)
    var amplitudeData by mutableStateOf(IntArray(0))

    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val buffer = ShortArray(bufferSize)

    // Function to start audio recording
    fun startRecording() {
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        amplitudeData = IntArray(buffer.size)

        audioRecorder?.startRecording()
        isRecording = true

        // Launch a coroutine to keep reading audio data from the recorder
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isRecording) {
                val readCount = audioRecorder?.read(buffer, 0, buffer.size) ?: 0
                if (readCount > 0) {
                    amplitudeData =
                        buffer.map { it.toInt() }.toIntArray() // Updating amplitude data
                }
            }
        }
    }

    // Function to stop the audio recording
    fun stopRecording() {
        isRecording = false
        audioRecorder?.stop()
        audioRecorder?.release()
        recordingJob?.cancel()
        audioRecorder = null
    }

    // Function to save the recording to the user's "Recordings" folder using MediaStore
    @RequiresApi(Build.VERSION_CODES.S)
    fun saveRecordingToFile(context: Context) {
        val contentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "recording_${System.currentTimeMillis()}.wav")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Recordings")
        }

        val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(uri).use { output ->
                if (output != null) {
                    // Write WAV header and data
                    writeWavHeader(output, buffer.size * 2)
                    buffer.forEach { sample ->
                        output.write(shortToByteArray(sample))
                    }
                }
            }
        }
    }

    // Function to convert a short value to a byte array (16-bit PCM data)
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }

    // Function to write the WAV header
    private fun writeWavHeader(outputStream: OutputStream, totalAudioLen: Int) {
        val totalDataLen = totalAudioLen + 36
        val sampleRate = 44100
        val channels = 1
        val byteRate = sampleRate * 2 * channels

        val header = byteArrayOf(
            // RIFF header
            'R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte(),
            (totalDataLen and 0xff).toByte(),
            ((totalDataLen shr 8) and 0xff).toByte(),
            ((totalDataLen shr 16) and 0xff).toByte(),
            ((totalDataLen shr 24) and 0xff).toByte(),
            // WAVE header
            'W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte(),
            // fmt subchunk
            'f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte(),
            16, 0, 0, 0, // Subchunk1Size (16 for PCM)
            1, 0, // AudioFormat (1 for PCM)
            channels.toByte(), 0.toByte(), // NumChannels
            (sampleRate and 0xff).toByte(),
            ((sampleRate shr 8) and 0xff).toByte(),
            ((sampleRate shr 16) and 0xff).toByte(),
            ((sampleRate shr 24) and 0xff).toByte(),
            (byteRate and 0xff).toByte(),
            ((byteRate shr 8) and 0xff).toByte(),
            ((byteRate shr 16) and 0xff).toByte(),
            ((byteRate shr 24) and 0xff).toByte(),
            (2 * channels).toByte(), 0.toByte(), // BlockAlign
            16, 0, // BitsPerSample (16 bits per sample)
            // data subchunk
            'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte(),
            (totalAudioLen and 0xff).toByte(),
            ((totalAudioLen shr 8) and 0xff).toByte(),
            ((totalAudioLen shr 16) and 0xff).toByte(),
            ((totalAudioLen shr 24) and 0xff).toByte()
        )

        outputStream.write(header, 0, 44)
    }

    // Ensuring that recording is stopped when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}


class AudioRecorderViewModel4 : ViewModel() {
    private var audioRecorder: AudioRecord? = null
    private var recordingJob: Job? = null
    var isRecording by mutableStateOf(false)
    var amplitudeData by mutableStateOf(IntArray(0))

    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    fun startRecording() {
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val buffer = ShortArray(bufferSize)
        amplitudeData = IntArray(buffer.size)

        audioRecorder?.startRecording()
        isRecording = true

        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isRecording) {
                val readCount = audioRecorder?.read(buffer, 0, buffer.size) ?: 0
                if (readCount > 0) {
                    amplitudeData = buffer.map { it.toInt() }.toIntArray()
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecorder?.stop()
        audioRecorder?.release()
        recordingJob?.cancel()
        audioRecorder = null
    }

    fun saveRecordingToFile(context: Context) {
        val fileName = "${System.currentTimeMillis()}.wav"
        val recordingsDir =
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings")
        if (!recordingsDir.exists()) recordingsDir.mkdirs()

        val file = File(recordingsDir, fileName)
        file.outputStream().use { output ->
            // Add WAV header if necessary, and write audio data here
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}

class AudioRecorderViewModel3 : ViewModel() {
    private var audioRecord: AudioRecord? = null
    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    val isRecording = mutableStateOf(false)

    val audioData = mutableStateOf(ByteArray(0))

    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording.value = true

        // Start collecting audio data
        viewModelScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize)
            while (isRecording.value) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    audioData.value = convertToByteArray(buffer)
                }
                delay(50L) // Update interval for visualizer
            }
        }
    }

    fun stopRecording() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        isRecording.value = false
    }

    // Converts the ShortArray to ByteArray for the visualizer
    private fun convertToByteArray(shortArray: ShortArray): ByteArray {
        val byteArray = ByteArray(shortArray.size * 2) // 16-bit audio, 2 bytes per sample
        for (i in shortArray.indices) {
            byteArray[i * 2] = (shortArray[i].toInt() and 0xFF).toByte()
            byteArray[i * 2 + 1] = ((shortArray[i].toInt() shr 8) and 0xFF).toByte()
        }
        return byteArray
    }
}

class AudioRecorderViewModel2 : ViewModel() {
    private var mediaRecorder: MediaRecorder? = null
    var isRecording by mutableStateOf(false)
    var amplitudeData by mutableStateOf(0)

    fun startRecording(outputFile: File) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        updateAmplitude()
    }

    private fun updateAmplitude() {
        // Launch a coroutine to capture amplitude data while recording
        viewModelScope.launch {
            while (isRecording) {
                amplitudeData = mediaRecorder?.maxAmplitude ?: 0
                delay(100) // Delay between amplitude updates
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
    }
}

