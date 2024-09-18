package com.example.audioplayer.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AudioVisualizer(audioData: ByteArray) {
     val temp = ByteArray(5)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(100.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / (audioData.size.toFloat() * 2)
        val maxBarHeight = height * 0.7f
        val middle = height / 2

        val gradientColors = listOf(Color.Blue, Color.Cyan, Color.Magenta)

        audioData.forEachIndexed { index, byte ->
            val barHeight = ((byte.toFloat() + 128) / 256) * maxBarHeight
            val startX = index * barWidth * 2
            val endX = startX + barWidth

            drawRect(
                brush = Brush.verticalGradient(gradientColors),
                topLeft = Offset(startX, middle - barHeight),
                size = Size(barWidth, barHeight * 2),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
@Composable
fun AudioVisualizer(amplitude: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(16.dp)
    ) {
        val barWidth = size.width / 50
        val barHeight = (amplitude / 32767f) * size.height // Normalize amplitude

        for (i in 0..49) {
            val barTop = size.height - barHeight * (i % 5 + 1) // Dynamic heights for visual effect
            drawRect(
                color = Color.Cyan,
                topLeft = Offset(i * barWidth, barTop),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
