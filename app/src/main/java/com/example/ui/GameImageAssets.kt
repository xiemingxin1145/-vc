package com.example.ui

import com.example.R

/**
 * 三国人生模拟器图像资源索引。
 * v1: 地图 + 六名核心人物头像 + 三张战场事件图。
 */
object GameImageAssets {
    object Maps {
        val SANGUO_OVERVIEW = R.drawable.map_sanguo_overview
    }

    object Portraits {
        val LIU_BEI = R.drawable.portrait_liu_bei
        val GUAN_YU = R.drawable.portrait_guan_yu
        val ZHUGE_LIANG = R.drawable.portrait_zhuge_liang
        val CAO_CAO = R.drawable.portrait_cao_cao
        val SUN_QUAN = R.drawable.portrait_sun_quan
        val ZHAO_YUN = R.drawable.portrait_zhao_yun
    }

    object Battles {
        val YELLOW_TURBAN_NIGHT = R.drawable.battle_yellow_turban_night
        val HULAO_DUEL = R.drawable.battle_hulao_duel
        val RED_CLIFF_FIRE = R.drawable.battle_red_cliff_fire
    }

    fun imageForEvent(eventId: String): Int? = when (eventId) {
        // 战场事件
        "battle_yellow_turban_vanguard" -> Battles.YELLOW_TURBAN_NIGHT
        "battle_hulao_duel" -> Battles.HULAO_DUEL
        "battle_chibi_fire_wind" -> Battles.RED_CLIFF_FIRE
        // 图文premium事件（SanguoContentPack）
        "evt_yellow_turban_oath" -> R.drawable.illust_yellow_turban_oath
        "evt_luoyang_night" -> R.drawable.illust_luoyang_night
        "evt_longzhong_strategy" -> R.drawable.illust_longzhong_strategy
        "evt_red_cliff_wind" -> R.drawable.illust_red_cliff_wind
        "evt_market_silk_road" -> R.drawable.illust_market_silk_road
        "evt_palace_shadow" -> R.drawable.illust_palace_shadow
        else -> null
    }

    fun portraitForFigure(name: String): Int? = when (name) {
        "刘备" -> Portraits.LIU_BEI
        "关羽" -> Portraits.GUAN_YU
        "诸葛亮" -> Portraits.ZHUGE_LIANG
        "曹操" -> Portraits.CAO_CAO
        "孙权" -> Portraits.SUN_QUAN
        "赵云" -> Portraits.ZHAO_YUN
        else -> null
    }
}
