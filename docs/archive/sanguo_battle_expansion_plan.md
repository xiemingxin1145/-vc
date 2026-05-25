# 三国人生模拟器 · 战场线补充计划

## 已新增内容

新增 Kotlin 文件：

```text
app/src/main/java/com/example/ui/SanguoBattleExpansion.kt
```

包含：

- 8 条战场剧情事件。
- 按年份、势力、声望筛选战场事件。
- 对应胜败结算函数 `GameViewModel.applySanguoBattleOutcome(...)`。
- 不修改数据库结构。
- 不破坏现有 `RandomEvent` 数据结构。

## 新增战场事件

1. 黄巾夜袭 · 火照连营
2. 虎牢关前 · 阵斩敌将
3. 官渡乌巢 · 焚粮奇袭
4. 赤壁东风 · 火船破江
5. 樊城水围 · 七军震怖
6. 城池攻防 · 云梯血战
7. 铁骑突围 · 白刃裂阵
8. 封侯之战 · 乱世定鼎

## 下一步需要接入的位置

### 1. 接入年度事件池

在 `GameViewModel.triggerRandomEvent()` 里，把原来的：

```kotlin
val num = Random.nextInt(8)
val ev = when (num) { ... }
```

改成：

```kotlin
val useBattleStory = Random.nextInt(100) < 35
val ev = if (useBattleStory) {
    SanguoBattleExpansion.pickBattleEvent(
        currentYear = currentYear.value,
        reputation = reputation.value,
        currentFaction = currentFaction.value
    )
} else {
    // 原普通事件
}
```

建议概率：

- 早期 20%
- 玩家参军后 35%
- 声望 300 以上 45%
- 进入封侯阶段 55%

### 2. 接入结算逻辑

在 `GameViewModel.handleEventOutcome(ev, option)` 的 `when (ev.id)` 最前面加：

```kotlin
if (ev.id.startsWith("battle_")) {
    val battleLog = applySanguoBattleOutcome(ev, option)
    addLog(logPrefix + battleLog)
    _gameState.value = GameState.Playing
    saveCurrentGameToDb()
    return
}
```

这样不会影响原来的普通事件。

## 图片资源建议

当前可以先用矢量占位图，后续换正式图。

建议新增：

```text
app/src/main/res/drawable/illust_yellow_turban_night.xml
app/src/main/res/drawable/illust_hulao_duel.xml
app/src/main/res/drawable/illust_guandu_wuchao.xml
app/src/main/res/drawable/illust_chibi_fire.xml
app/src/main/res/drawable/illust_fancheng_flood.xml
app/src/main/res/drawable/illust_city_siege.xml
app/src/main/res/drawable/illust_cavalry_breakthrough.xml
app/src/main/res/drawable/illust_final_fiefdom.xml
```

正式美术关键词：

- 黄巾夜袭：夜火、营寨、乱兵、战鼓、黄巾旗。
- 虎牢单挑：关前、战马、长槊、万军观阵。
- 官渡乌巢：粮仓、火海、夜袭、黑甲死士。
- 赤壁火攻：铁索连舟、江风、火船、红色江面。
- 樊城水围：暴雨、洪水、困军、城墙。
- 云梯攻城：城楼、滚木、弓箭、云梯、血战。
- 铁骑突围：骑兵、包围圈、尘土、断旗。
- 封侯之战：军帐、城池、旌旗、诸侯使者。

## 音效资源建议

安卓资源路径建议：

```text
app/src/main/res/raw/sfx_war_drum.mp3
app/src/main/res/raw/sfx_sword_clash.mp3
app/src/main/res/raw/sfx_horse_charge.mp3
app/src/main/res/raw/sfx_fire_attack.mp3
app/src/main/res/raw/sfx_arrow_rain.mp3
app/src/main/res/raw/sfx_victory_fanfare.mp3
app/src/main/res/raw/sfx_defeat_low.mp3
app/src/main/res/raw/sfx_city_gate_break.mp3
```

先不用急着上真实音频。第一版可只保留文件名和播放接口，避免编译缺资源。

## 战场线成长节奏

建议玩家生命周期：

```text
15-20岁：黄巾乱世、山贼、募兵、初阵
20-28岁：投靠诸侯、虎牢关、讨董、城池攻防
28-38岁：官渡、赤壁、封将、经营地盘
38-50岁：樊城、北伐、夺城、封侯
50岁以后：传位、结局列传、青史评价
```

## 验收标准

完成接入后，至少要做到：

- 每年事件有概率刷出战场事件。
- 战场事件有三选项。
- 胜负会影响兵力、健康、声望、金钱、士气、统一进度。
- 城池攻防可触发占城。
- 封侯事件可改变职位。
- 不破坏存档读取。
- 能通过 `gradle assembleDebug`。

## 后续重点

1. 把 `SanguoBattleExpansion` 接进 `triggerRandomEvent()`。
2. 把 `applySanguoBattleOutcome()` 接进 `handleEventOutcome()`。
3. 增加战场图标/插图显示。
4. 增加战鼓、刀剑、马蹄、火攻、胜利失败音效。
5. 跑一次 GitHub Actions 构建 APK。
