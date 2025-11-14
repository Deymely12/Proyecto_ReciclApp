package com.example.reciclapp.screens.dashboard.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun LineChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    AndroidView(factory = { context ->
        LineChart(context).apply {
            description.isEnabled = false
            legend.isEnabled = true
            setNoDataText("No hay datos")
        }
    }, modifier = modifier) { chart ->
        val sorted = data.toSortedMap()
        val entries = sorted.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }
        val dataSet = LineDataSet(entries, "Puntos diarios").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            setDrawFilled(true)
            fillColor = Color.CYAN
        }
        chart.xAxis.apply {
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(sorted.keys.toList())
            textColor = Color.BLACK
        }
        chart.data = LineData(dataSet)
        chart.invalidate()
    }
}