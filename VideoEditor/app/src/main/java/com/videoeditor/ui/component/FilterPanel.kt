package com.videoeditor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.videoeditor.data.model.Filter
import com.videoeditor.data.model.FilterType
import com.videoeditor.ui.theme.*

@Composable
fun FilterPanel(
    onFilterSelected: (Filter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            "滤镜",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(FilterType.entries) { filterType ->
                FilterItem(
                    filterType = filterType,
                    onClick = {
                        onFilterSelected(
                            Filter(
                                name = filterType.displayName,
                                type = filterType,
                                intensity = 1.0f
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterItem(
    filterType: FilterType,
    onClick: () -> Unit
) {
    val backgroundColor = when (filterType) {
        FilterType.NONE -> Color.DarkGray
        FilterType.WARM -> FilterWarm
        FilterType.COOL -> FilterCool
        FilterType.VINTAGE -> FilterVintage
        FilterType.BLACK_WHITE -> FilterBW
        FilterType.SEPIA -> Color(0xFF8D6E63)
        else -> Color.DarkGray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (filterType == FilterType.NONE) {
                Text("原片", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            filterType.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
