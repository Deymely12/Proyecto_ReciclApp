package com.example.reciclapp.screens.dashboard.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.HorizontalBarChart as MPHorizontalBarChart


@Composable
fun HorizontalBarChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    AndroidView(factory = { context ->
        MPHorizontalBarChart(context).apply {
            description.isEnabled = false
            legend.isEnabled = true
            setFitBars(true)
            setNoDataText("No hay datos")
        }
    }, modifier = modifier) { chart ->
        val entries = data.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
        val dataSet = BarDataSet(entries, "Impacto ecológico").apply {
            colors = listOf(Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.RED)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }
        chart.xAxis.apply {
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(data.keys.toList())
            textColor = Color.BLACK
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }
        chart.data = BarData(dataSet)
        chart.invalidate()
    }
}