package com.lumiposture.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumiposture.model.FatigueData
import com.lumiposture.model.FatigueLevel

/// 首页 — 疲劳仪表盘
///
/// 展示: 疲劳指数圆环 + 等级 + 建议 + 累计疲劳
@Composable
fun HomeScreen(fatigue: FatigueData? = null) {
    Column(
        Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LUMI 智能腰托", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val level = fatigue?.level ?: FatigueLevel.COMFORTABLE
        val index = fatigue?.fatigueIndex ?: 0.0

        // 疲劳仪表盘
        Card(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = levelColor(level).copy(alpha = 0.1f))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 表情 + 等级
                Text(level.emoji, fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(level.label, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = levelColor(level))
                Spacer(Modifier.height(8.dp))

                // 疲劳指数进度条
                Text("疲劳指数 ${"%.1f".format(index)} / 100", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (index / 100.0).toFloat() },
                    modifier = Modifier.fillMaxWidth(0.7f).height(8.dp),
                    color = levelColor(level),
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )

                Spacer(Modifier.height(16.dp))
                Text(level.suggestion, fontSize = 14.sp, color = Color.Gray)
            }
        }

        // 累计疲劳
        if (fatigue != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("累积疲劳", "${"%.1f".format(fatigue.cumulativeStrain)}", Modifier.weight(1f))
                StatCard("坐姿分钟", "${"%.0f".format(fatigue.continuousSittingMinutes)}", Modifier.weight(1f))
                StatCard("不对称度", "${"%.0f".format(fatigue.pressureAsymmetry * 100)}%", Modifier.weight(1f))
            }
        }
    }
}

fun levelColor(level: FatigueLevel): Color = when (level) {
    FatigueLevel.COMFORTABLE -> Color(0xFF4CAF50)
    FatigueLevel.MILD -> Color(0xFFFF9800)
    FatigueLevel.MODERATE -> Color(0xFFFF5722)
    FatigueLevel.SEVERE -> Color(0xFFE53935)
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
