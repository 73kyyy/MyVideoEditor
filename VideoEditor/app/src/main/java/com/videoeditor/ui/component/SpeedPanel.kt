package com.videoeditor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val speedPresets = listOf(
    0.25f to "0.25x",
    0.5f to "0.5x",
    0.75f to "0.75x",
    1.0f to "1x",
    1.25f to "1.25x",
    1.5f to "1.5x",
    2.0f to "2x",
    3.0f to "3x",
    4.0f to "4x"
)

@Composable
fun SpeedPanel(
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSpeed by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "变速",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = currentSpeed,
            onValueChange = {
                currentSpeed = it
                onSpeedChange(it)
            },
            valueRange = 0.25f..4f,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            String.format("%.2fx", currentSpeed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(speedPresets) { (speed, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (currentSpeed == speed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            currentSpeed = speed
                            onSpeedChange(speed)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentSpeed == speed) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
