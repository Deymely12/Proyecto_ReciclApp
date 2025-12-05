package com.example.reciclapp.screens.dashboard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.reciclapp.screens.dashboard.data.models.ResiduosData
import com.github.mikephil.charting.charts.BarChart
import java.util.Calendar
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun ResiduosBarChart(
    residuosData: List<ResiduosData>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    // Filtrado según el tiempo
    val today = Calendar.getInstance()

    val filteredData = residuosData.filter { residuo ->
        val cal = Calendar.getInstance()
        cal.time = residuo.fecha.toDate()
        when (selectedFilter) {
            "Día" -> cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            "Semana" -> cal.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
                    && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            "Mes" -> cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            "Año" -> cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            else -> true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp), // más grande para que se vea bien
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                BarChart(context).apply {
                    description.isEnabled = false
                    setDrawValueAboveBar(true)
                    setPinchZoom(false)
                    setDrawGridBackground(false)
                }
            },
            update = { chart ->
                val entriesMap = filteredData.groupBy { it.tipo }
                    .mapValues { it.value.sumOf { r -> r.cantidad } }

                val entries = entriesMap.entries.mapIndexed { index, entry ->
                    BarEntry(index.toFloat(), entry.value.toFloat())
                }

                val dataSet = BarDataSet(entries, "Residuos")
                dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                dataSet.setDrawValues(true)
                dataSet.highLightAlpha = 150 // efecto al tocar

                val barData = BarData(dataSet)
                chart.data = barData

                // Etiquetas de tipo en X
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(entriesMap.keys.toList())
                chart.xAxis.granularity = 1f
                chart.xAxis.setDrawLabels(true)

                chart.invalidate()
            }
        )
    }
}





