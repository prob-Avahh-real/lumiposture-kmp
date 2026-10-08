# lumiposture-kmp

LumiPosture KMP 智能人体工学腰托移动应用 — Kotlin Multiplatform + Compose Multiplatform 功能 - BLE 连接 LUMI 智能腰托 (TI CC254x, 0xFFE0-0xFFE3 UUID) - 8x8 压力矩阵实时热力图 - 坐姿对称度分析 + 倾斜角 + 腰部温度监测 - 疲劳指数计算 (加权线性公式: 倾斜 ×0.25 + 不对称 ×0.30 + 久坐 ×0.30)

## 用途与 Target 判定

Kotlin Multiplatform / Compose 工程（已归档）

- 移动端：需要
- 智能硬件：需要

## 目标平台

| 平台 | 方式 | 产出 |
|------|------|------|
| 移动端 | KMP | APK / 既有 iOS |
| 手表 / 戒指 / 传感器 | 按原设计 | — |

## 技术栈信号

- 检测到：ios, kmp

## 命令

```bash
./gradlew :composeApp:assembleDebug
```

## 状态

已归档。默认只做必要修复与知识库同步，不新开大范围需求。

## 助手约束

- Loop：先定可验证 Done 与停止条件；每切片过验证环；失败回灌；详见 docs/LOOP_ENGINEERING.md
- Harness：遵循根 AGENTS「Harness Engineering」；Guide=本文件，Sensor=测试/CI；同类失误二次须沉淀规则或测试（棘轮）。详见 docs/HARNESS_ENGINEERING.md
- 遵循仓库根 `/Users/skat/AGENTS.md` 与本文件的用途判定
- 可选 Target 可搁置；不适用形态不要硬加
- 改动聚焦；公共逻辑抽离复用
