package com.lumiposture.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiposture.model.PostureData
import com.lumiposture.model.PostureQuality

/// 坐姿监测屏幕 — 8x8 压力热力图 + 实时传感器数据
@Composable
fun SitMonitorScreen(posture: PostureData? = null) {
    val data = posture ?: remember { PostureData.mock() }

    Column(Modifier.padding(16.dp)) {
        Text("实时坐姿监测", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // 坐姿质量标签
        val quality = postureQuality(data)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    quality.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = postureQualityColor(quality)
                )
                Text(
                    "对称度 ${"%.0f".format(data.symmetryIndex * 100)}%",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 压力热力图 (8x8)
        Text("压力分布", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth().aspectRatio(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                for (row in 0 until 8) {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        for (col in 0 until 8) {
                            val point = data.pressureMap.find { it.row == row && it.col == col }
                            val intensity = point?.value ?: 0.0
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(1.dp)
                                    .background(
                                        pressureHeatColor(intensity),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
        Text(
            "深色 = 压力越大",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 传感器数据行
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorChip("倾斜", "${"%.1f".format(data.tiltAngle)}°", Modifier.weight(1f))
            SensorChip("旋转", "${"%.1f".format(data.rollAngle)}°", Modifier.weight(1f))
            SensorChip("温度", "${"%.1f".format(data.temperature)}°C", Modifier.weight(1f))
            SensorChip("时长", "${data.sittingDurationSeconds / 60}分", Modifier.weight(1f))
        }
    }
}

fun postureQuality(data: PostureData): PostureQuality = when {
    data.symmetryIndex > 0.85 -> PostureQuality.EXCELLENT
    data.symmetryIndex > 0.70 -> PostureQuality.GOOD
    data.symmetryIndex > 0.50 -> PostureQuality.FAIR
    data.symmetryIndex > 0.30 -> PostureQuality.POOR
    else -> PostureQuality.CRITICAL
}

fun postureQualityColor(q: PostureQuality): Color = when (q) {
    PostureQuality.EXCELLENT -> Color(0xFF4CAF50)
    PostureQuality.GOOD -> Color(0xFF8BC34A)
    PostureQuality.FAIR -> Color(0xFFFF9800)
    PostureQuality.POOR -> Color(0xFFFF5722)
    PostureQuality.CRITICAL -> Color(0xFFE53935)
}

fun pressureHeatColor(intensity: Double): Color {
    val c = (intensity * 255).toInt().coerceIn(0, 255)
    return Color(red = c, green = (255 - c) / 2, blue = 255 - c)
}

@Composable
fun SensorChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(10.dp)) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
