package com.example.audioplayer.HomeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    moveToAudioPlayerScreen:()->Unit,
    moveToAudioRecorderScreen:()->Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { moveToAudioPlayerScreen() }) {
            Text("Go to Audio Player")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { moveToAudioRecorderScreen() }) {
            Text("Go to Audio Recorder")
        }
    }

}