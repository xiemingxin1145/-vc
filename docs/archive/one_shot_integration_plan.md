# 一步到位接入方案

这个文件用于统一 ChatGPT、Grok、Claude 后续协作，避免多方同时乱改核心文件。

## 为什么不要乱改核心文件

当前 `MainActivity.kt` 和 `GameViewModel.kt` 文件都很大，直接整文件替换有风险：

- 容易覆盖别人刚改的内容。
- 容易造成 Compose import 缺失。
- 容易引入 Kotlin 编译错误。
- 手机端确认 GitHub 写入很麻烦，每个写入动作都要点确认。

因此后续采用“一次一块功能”的方式接入。

## 当前已经完成

### 内容模块

- `app/src/main/java/com/example/ui/SanguoContentPack.kt`
- `app/src/main/java/com/example/ui/SanguoGallery.kt`
- `app/src/main/java/com/example/ui/SanguoAchievementEngine.kt`

### 插图资源

- `app/src/main/res/drawable/illust_yellow_turban_oath.xml`
- `app/src/main/res/drawable/illust_luoyang_night.xml`
- `app/src/main/res/drawable/illust_longzhong_strategy.xml`
- `app/src/main/res/drawable/illust_red_cliff_wind.xml`
- `app/src/main/res/drawable/illust_market_silk_road.xml`
- `app/src/main/res/drawable/illust_palace_shadow.xml`

### 文档整理

- `docs/project_roadmap.md`
- `docs/task_board.md`

## 下一步一：事件弹窗显示插图

目标：在 `EventChoiceDialog` 中显示事件插图。

推荐改法：

1. 在 `MainActivity.kt` imports 区域增加：

```kotlin
import androidx.compose.ui.res.painterResource
```

2. 在 `EventChoiceDialog` 的 `text = { Column(...) { ... } }` 里，事件描述 `Text(event.description...)` 之前加入：

```kotlin
val eventArt = SanguoGallery.eventArts.firstOrNull { art ->
    event.id.contains(art.id) ||
    event.title.contains(art.title.take(2))
}

if (eventArt != null) {
    Image(
        painter = painterResource(id = eventArt.resId),
        contentDescription = eventArt.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111111))
    )
    Text(
        text = eventArt.caption,
        color = Color(0xFFD4AF37),
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}
```

注意：如果 `Image` 找不到，需要确认 `androidx.compose.foundation.*` 是否已经覆盖了 Image。当前项目已有 foundation 通配 import，理论上可用。

## 下一步二：成就入口

目标：在游戏主界面或记录界面增加成就按钮。

推荐新增 Composable：

```kotlin
@Composable
fun AchievementDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    val achievements = SanguoAchievementEngine.unlockedAchievements(viewModel)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("功业成就", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(achievements) { ach ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (ach.unlocked) "已达成：${ach.title}" else "未达成：${ach.title}",
                                color = if (ach.unlocked) Color(0xFFD4AF37) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(ach.description, color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        containerColor = Color(0xFF1E1E22)
    )
}
```

## 下一步三：高级事件接入年度事件池

目标：让 `SanguoContentPack.premiumEvents` 真正出现在每年事件中。

推荐在 `GameViewModel.triggerRandomEvent()` 开头增加：

```kotlin
if (Random.nextInt(100) < 30) {
    val premium = SanguoContentPack.premiumEvents.random().event
    _gameState.value = GameState.EventChoice(premium) { opIndex ->
        handleEventOutcome(premium, opIndex)
    }
    return
}
```

这样 30% 概率刷高级事件，70% 保留原普通事件。

## 下一步四：补高级事件结果

当前 `handleEventOutcome` 没有专门处理新增事件 id。新增事件暂时会走默认逻辑或无专属收益。

后续需要为这些 id 增加分支：

- `evt_yellow_turban_oath`
- `evt_luoyang_night`
- `evt_longzhong_strategy`
- `evt_red_cliff_wind`
- `evt_market_silk_road`
- `evt_palace_shadow`

每个事件建议产生：

- 金钱变化
- 名望变化
- 属性变化
- 人物关系变化
- 兵力变化
- 健康变化

## 推荐执行顺序

1. 事件弹窗显示插图。
2. 高级事件接入年度事件池。
3. 高级事件结果分支。
4. 成就入口。
5. 图库入口。
6. 跑 `./gradlew assembleDebug`。

## 手机确认太烦怎么办

ChatGPT 写 GitHub 文件时会弹确认，这是安全机制，无法关闭。

减少确认次数的方法：

- 尽量一次只做一个大功能。
- 不拆太多小文件。
- 能写文档先写文档，核心代码等确认思路后一次改。
- 让 Grok 或本地 Android Studio 跑构建，把报错集中反馈。

## 当前结论

项目已经有骨架、有内容包、有插图、有成就引擎。真正缺的是 UI 接入和年度事件接入。

最优先做：事件弹窗显示插图。
