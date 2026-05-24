package com.example.ui

import kotlin.random.Random

/**
 * 三国年度事件选择器。
 *
 * 用途：把普通人生事件与战场剧情线混合起来，避免 GameViewModel 主文件继续膨胀。
 */
object SanguoAnnualEventPicker {

    fun shouldUseBattleStory(
        currentYear: Int,
        reputation: Int,
        currentFaction: String,
        currentJob: String
    ): Boolean {
        val chance = when {
            currentJob.contains("侯") || reputation >= 450 -> 55
            currentFaction != "在野游侠" || currentJob.contains("将") -> 45
            reputation >= 300 -> 40
            currentYear >= 190 -> 35
            else -> 25
        }
        return Random.nextInt(100) < chance
    }

    fun pickBattleStory(
        currentYear: Int,
        reputation: Int,
        currentFaction: String
    ): RandomEvent {
        return SanguoBattleExpansion.pickBattleEvent(
            currentYear = currentYear,
            reputation = reputation,
            currentFaction = currentFaction
        )
    }
}
