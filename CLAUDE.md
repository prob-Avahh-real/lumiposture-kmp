# CLAUDE.md — LumiPosture KMP

智能人体工学腰托 KMP 应用。BLE 连接 LUMI 设备 → 8x8 压力矩阵 → 姿态分析 + 疲劳计算。

## 命令

```bash
./gradlew :composeApp:assembleRelease    # Android APK
./gradlew :shared:allTests               # 所有测试
```

## 架构

```
shared/commonMain/
├── platform/BleAdapter.kt           ← expect BLE 适配器
├── data/
│   ├── LumiBleProtocol.kt           ← BLE 协议常量 + 命令编码
│   ├── PostureFrameParser.kt        ← 68 字节帧解析器 (Result 语义)
│   ├── IPostureRepository.kt        ← 仓库接口
│   ├── PostureRepository.kt         ← BLE → 坐姿流 + 命令路由
│   ├── AppState.kt                  ← 状态容器 + ISessionStore + InMemorySessionStore
│   ├── DailyStats.kt                ← 每日统计工具
│   └── HttpClient.kt               ← Ktor HTTP 客户端
├── engine/
│   ├── IFatigueEngine.kt            ← 疲劳引擎接口 (策略模式)
│   └── FatigueCalculator.kt         ← 时域加权线性公式实现
├── model/Entities.kt                ← 所有数据模型 + 枚举
└── LumiConfig.kt                    ← 全局配置常量

composeApp/commonMain/
├── ui/viewmodel/LumiViewModel.kt    ← ViewModel 编排 BLE + 引擎 + UI
└── ui/screens/
    ├── LumiPostureApp.kt            ← 主导航 (4 tab) + MaterialTheme
    ├── HomeScreen.kt                ← 疲劳仪表盘
    ├── SitMonitorScreen.kt          ← 8x8 压力热力图 + 传感器
    └── PlaceholderScreen.kt         ← 占位页面

platform/
├── androidMain/.../BleAdapter.android.kt  ← Kable (Context)
└── iosMain/.../BleAdapter.ios.kt           ← Kable (CoreBluetooth)
```

## 接口抽象

- `IFatigueEngine` — 疲劳计算策略 (公式 / ML / 传感器融合)
- `IPostureRepository` — BLE 数据仓库
- `ISessionStore` — 会话存储 (内存 / SQLite / 云端)

## BLE 协议

- 服务: FFE2 (数据), FFE0 (控制)
- 特征: FFE3 (notify: 68 字节帧), FFE1 (write: 命令)
- 命令: [0xA0, mode] 按摩, [0xA1, mode] 节能, [0xA2, on, temp] 加热

## 疲劳公式

```
fatigueIndex = (tiltDev × 0.25 + pressureAsym × 0.30 + sittingFactor × 0.30) × 100
阈值: <25 舒适, 25-50 轻度, 50-75 中度, >75 重度
```

## 测试覆盖

8 个测试类, 覆盖:
- FatigueCalculator (7 测试: 等级边界, 边界条件, 累积疲劳)
- PostureFrameParser (5 测试: 正常帧, 不完整帧, 极限值)
- InMemorySessionStore (5 测试: CRUD, 淘汰, 今日筛选)
- DailyStats (4 测试: 空列表, 单点, 良好率)
- PostureData (7 测试: 计算属性, mock)
- LumiBleProtocol (5 测试: 命令编码, UUID)
- Entities (9 测试: 枚举完整性, 默认值)
- FatigueEngineContract (2 测试: 接口实现, 等级覆盖)
