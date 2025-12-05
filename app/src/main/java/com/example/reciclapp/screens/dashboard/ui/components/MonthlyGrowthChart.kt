package com.example.reciclapp.screens.dashboard.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.reciclapp.screens.dashboard.data.models.CrecimientoUsuario
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*

@Composable
fun MonthlyGrowthChart(data: List<CrecimientoUsuario>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    axisRight.isEnabled = false

                    // Configurar eje X
                    xAxis.apply {
                        granularity = 1f
                        position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                            (1..12).map { it.toString() } // nombres de los meses del 1 al 12
                        )
                    }

                    axisLeft.apply {
                        axisMinimum = 0f
                        granularity = 1f
                    }
                }
            },
            update = { chart ->
                // 1. Crear un mapa con todos los meses inicializados en 0
                val monthlyMap = (1..12).associateWith { 0 }.toMutableMap()

                // 2. Rellenar los datos existentes
                data.forEach { crecimiento ->
                    val month = crecimiento.mes.toIntOrNull() ?: return@forEach
                    monthlyMap[month] = crecimiento.cantidad
                }

                // 3. Convertir en acumulativo
                var acumulado = 0
                val entries = monthlyMap.toSortedMap().map { (mes, cantidad) ->
                    acumulado += cantidad
                    Entry(mes.toFloat() - 1, acumulado.toFloat()) // -1 porque MPAndroidChart indexa desde 0
                }

                // 4. Crear DataSet
                val dataSet = LineDataSet(entries, "Usuarios acumulados").apply {
                    color = android.graphics.Color.BLUE
                    lineWidth = 3f
                    setDrawCircles(true)
                    circleRadius = 5f
                    valueTextSize = 12f
                    setDrawFilled(true)
                    fillColor = android.graphics.Color.CYAN
                }

                chart.data = LineData(dataSet)
                chart.invalidate() // refresca el gráfico
            }
        )
    }
}
