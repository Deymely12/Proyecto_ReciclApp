package com.example.reciclapp.screens.dashboard.ui.components

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.text.get

@Composable
fun DonutChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val chartColors = ColorTemplate.MATERIAL_COLORS.map { ComposeColor(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f), // Mantiene el círculo perfecto
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp) // Espacio alrededor del donut
            ) {
                AndroidView(factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setDrawEntryLabels(false)
                        setUsePercentValues(true)
                        isDrawHoleEnabled = true
                        setHoleColor(Color.TRANSPARENT)
                        setEntryLabelColor(Color.BLACK)
                        legend.isEnabled = false
                    }
                }, update = { chart ->
                    val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 16f
                        valueFormatter = com.github.mikephil.charting.formatter.PercentFormatter(chart)
                    }
                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }, modifier = Modifier.fillMaxSize()) // Ocupa todo el Box
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda manual
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.entries.forEachIndexed { index, entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = chartColors[index % chartColors.size],
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "${entry.key}: ${entry.value} (${String.format("%.1f%%", entry.value * 100f / data.values.sum())})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}