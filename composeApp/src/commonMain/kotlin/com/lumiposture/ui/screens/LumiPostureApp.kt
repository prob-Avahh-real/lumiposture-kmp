package com.lumiposture.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lumiposture.LumiConfig
import com.lumiposture.ui.viewmodel.LumiViewModel

enum class LumiTab { HOME, SIT, DEVICE, HISTORY }

@Composable
fun LumiPostureApp(viewModel: LumiViewModel? = null) {
    var selectedTab by remember { mutableStateOf(LumiTab.HOME) }
    val fatigue by (viewModel?.fatigueData?.collectAsState() ?: remember { mutableStateOf(null) })
    val posture by (viewModel?.postureData?.collectAsState() ?: remember { mutableStateOf(null) })

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(LumiConfig.APP_NAME_CN) },
                    actions = {
                        // 连接状态指示
                        if (viewModel != null) {
                            val isConnected by viewModel.connectedDevice.collectAsState()
                            ConnectionIndicator(isConnected = isConnected != null)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == LumiTab.HOME,
                        onClick = { selectedTab = LumiTab.HOME },
                        icon = { Icon(Icons.Filled.Home, "首页") },
                        label = { Text("首页") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == LumiTab.SIT,
                        onClick = { selectedTab = LumiTab.SIT },
                        icon = { Icon(Icons.Outlined.AirlineSeatFlat, "坐姿") },
                        label = { Text("坐姿") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == LumiTab.DEVICE,
                        onClick = { selectedTab = LumiTab.DEVICE },
                        icon = { Icon(Icons.Outlined.Bluetooth, "设备") },
                        label = { Text("设备") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == LumiTab.HISTORY,
                        onClick = { selectedTab = LumiTab.HISTORY },
                        icon = { Icon(Icons.Outlined.Timeline, "数据") },
                        label = { Text("数据") }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (selectedTab) {
                    LumiTab.HOME -> HomeScreen(fatigue)
                    LumiTab.SIT -> SitMonitorScreen(posture)
                    LumiTab.DEVICE -> PlaceholderScreen("设备", "扫描和连接 LUMI 腰托")
                    LumiTab.HISTORY -> PlaceholderScreen("历史数据", "坐姿历史记录")
                }
            }
        }
    }
}

@Composable
private fun ConnectionIndicator(isConnected: Boolean) {
    val color = if (isConnected) androidx.compose.ui.graphics.Color(0xFF4CAF50)
    else androidx.compose.ui.graphics.Color.Gray
    androidx.compose.foundation.Canvas(Modifier.size(12.dp).padding(end = 8.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2)
    }
}
