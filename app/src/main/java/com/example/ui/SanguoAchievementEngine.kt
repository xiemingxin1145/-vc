package com.example.ui

/**
 * 成就判断引擎。
 *
 * 这个对象不侵入现有存档结构，只根据 GameViewModel 当前公开状态计算已达成成就。
 * 后续可以在主界面加一个“成就”入口，直接调用 unlockedAchievements(viewModel)。
 */
object SanguoAchievementEngine {

    data class AchievementResult(
        val id: String,
        val title: String,
        val description: String,
        val unlocked: Boolean
    )

    fun unlockedAchievements(viewModel: GameViewModel): List<AchievementResult> {
        val ownedCityCount = viewModel.cityOwners.value.values.count { it == viewModel.currentFaction.value }

        return listOf(
            AchievementResult(
                id = "ach_first_fame",
                title = "初露锋芒",
                description = "名望突破 100，开始被乱世豪杰注意。",
                unlocked = viewModel.reputation.value >= 100
            ),
            AchievementResult(
                id = "ach_rich_family",
                title = "富甲一郡",
                description = "金钱突破 3000 两，家资足以左右一方。",
                unlocked = viewModel.gold.value >= 3000
            ),
            AchievementResult(
                id = "ach_war_master",
                title = "百战名将",
                description = "统率突破 120，并拥有 5000 以上兵力。",
                unlocked = viewModel.command.value >= 120 && viewModel.conscripts.value >= 5000
            ),
            AchievementResult(
                id = "ach_mastermind",
                title = "帷幄谋主",
                description = "智谋突破 130，已有运筹天下之才。",
                unlocked = viewModel.intelligence.value >= 130
            ),
            AchievementResult(
                id = "ach_true_lord",
                title = "一方明主",
                description = "本势力至少拥有 3 座城池。",
                unlocked = ownedCityCount >= 3
            ),
            AchievementResult(
                id = "ach_long_life",
                title = "寿考传家",
                description = "活到 75 岁以上，见证数代风云。",
                unlocked = viewModel.age.value >= 75
            )
        )
    }
}
