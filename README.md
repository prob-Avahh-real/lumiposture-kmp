# LumiPosture KMP

智能人体工学腰托移动应用 — Kotlin Multiplatform + Compose Multiplatform

## 功能

- BLE 连接 LUMI 智能腰托 (TI CC254x, 0xFFE0-0xFFE3 UUID)
- 8x8 压力矩阵实时热力图
- 坐姿对称度分析 + 倾斜角 + 腰部温度监测
- 疲劳指数计算 (加权线性公式: 倾斜 ×0.25 + 不对称 ×0.30 + 久坐 ×0.30)
- 按摩模式控制 (6 种: 关闭/轻柔/标准/强力/波浪/脉冲)
- 节能模式 (4 级) + 加热控制 (35-45°C)
- 坐姿提醒 (6 种类型: 纠正/休息/疲劳/按摩/喝水/护眼)

## 架构

```
lumiposture-kmp/
├── shared/
│   ├── commonMain/
│   │   ├── platform/BleAdapter.kt        ← expect BLE 适配器接口
│   │   ├── LumiConfig.kt                 ← 全局配置
│   │   ├── model/Entities.kt             ← 数据模型 (6 枚举 + 8 数据类)
│   │   ├── engine/
│   │   │   ├── IFatigueEngine.kt         ← 疲劳引擎接口 (策略模式)
│   │   │   └── FatigueCalculator.kt      ← 时域加权线性公式
│   │   └── data/
│   │       ├── LumiBleProtocol.kt        ← BLE 协议常量 + 命令编码
│   │       ├── PostureFrameParser.kt     ← 68 字节帧解析器
│   │       ├── IPostureRepository.kt     ← 仓库接口
│   │       ├── PostureRepository.kt      ← BLE → 坐姿流 + 命令路由
│   │       ├── AppState.kt               ← 状态容器 + 会话存储
│   │       ├── DailyStats.kt             ← 每日统计
│   │       └── HttpClient.kt             ← Ktor HTTP 客户端
│   ├── androidMain/  — BLE via Kable (Android Bluetooth)
│   └── iosMain/      — BLE via Kable (CoreBluetooth)
├── composeApp/
│   ├── ui/viewmodel/LumiViewModel.kt     ← ViewModel 编排层
│   └── ui/screens/
│       ├── LumiPostureApp.kt             ← 主导航 (4 tab)
│       ├── HomeScreen.kt                 ← 疲劳仪表盘
│       ├── SitMonitorScreen.kt           ← 8x8 压力热力图 + 传感器
│       └── PlaceholderScreen.kt          ← 占位页面
└── .github/workflows/ci.yml
```

## 抽象层

| 接口 | 位置 | 当前实现 | 可替换策略 |
|------|------|----------|-----------|
| `IFatigueEngine` | engine/ | `FatigueCalculator` | ML 模型, 传感器融合 |
| `IPostureRepository` | data/ | `PostureRepository` | 模拟仓库, 云端同步 |
| `ISessionStore` | data/ | `InMemorySessionStore` | SQLite, Key-Value |

## 构建

```bash
./gradlew :composeApp:assembleRelease    # Android APK
./gradlew :shared:allTests               # 测试 (8 测试类, 54 测试用例)
```

## 测试覆盖

| 模块 | 测试类 | 覆盖点 |
|------|--------|--------|
| FatigueCalculator | 2 类 | 等级边界, 累积疲劳, 确定性, 接口契约 |
| PostureFrameParser | 1 类 | 正常帧, 不完整帧, 极限值, 坐标验证 |
| InMemorySessionStore | 1 类 | 增删, 淘汰, 今日筛选, 清空 |
| DailyStats | 1 类 | 空列表, 单点, 良好率, 全对称 |
| PostureData | 1 类 | 计算属性, symmetryIndex, mock |
| LumiBleProtocol | 1 类 | 命令编码, 温度钳制, UUID 格式 |
| Entities | 1 类 | 枚举完整性, 默认值, 单调性 |
