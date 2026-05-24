package com.example.ui

import kotlin.random.Random

/**
 * 三国人生模拟器 · 战场剧情线扩展包
 *
 * 这个文件先作为低风险内容包加入工程：
 * - 不修改数据库结构
 * - 不破坏现有 RandomEvent 数据结构
 * - 事件可在 GameViewModel.triggerRandomEvent() 中接入
 * - outcome 可在 GameViewModel.handleEventOutcome() 中按 id 调用
 */
object SanguoBattleExpansion {

    val battleStoryEvents: List<RandomEvent> = listOf(
        RandomEvent(
            id = "battle_yellow_turban_vanguard",
            title = "黄巾夜袭 · 火照连营",
            description = "黄巾余部趁夜鼓噪，数千乱兵举火冲营。前锋营盘火光冲天，辎重将毁，军心欲溃。你披甲而起，必须在一炷香内稳住阵脚。",
            option1 = "亲率骑兵反冲敌阵，斩旗夺鼓（武力/骑兵）",
            option2 = "登高击鼓，重整三军阵列（统率/士气）",
            option3 = "弃前营保粮仓，收缩防线待天明（政治/保守）",
            statRequired = "统率",
            threshold = 58
        ),
        RandomEvent(
            id = "battle_hulao_duel",
            title = "虎牢关前 · 阵斩敌将",
            description = "虎牢关下战鼓如雷，敌将横槊叫阵，连败数名牙将。诸侯营中人心浮动，若此刻无人应战，联军锐气将折。",
            option1 = "拍马出阵，与敌将阵前单挑（武力）",
            option2 = "遣弓弩手暗伏两翼，诱其深入（智谋）",
            option3 = "高举军令，以重赏激励部曲轮番冲击（统率/金钱）",
            statRequired = "武力",
            threshold = 70
        ),
        RandomEvent(
            id = "battle_guandu_supply_raid",
            title = "官渡乌巢 · 焚粮奇袭",
            description = "斥候密报：敌军粮仓藏于乌巢，守军懈怠却路途险恶。若能一夜焚粮，十万大军将不战自乱。若失手，你部必陷重围。",
            option1 = "精选死士夜袭粮营，火焚乌巢（智谋/统率）",
            option2 = "佯攻正面大寨，替友军争取奇袭时间（统率/兵力）",
            option3 = "截杀敌方运粮队，稳扎稳打削其粮道（政治/低风险）",
            statRequired = "智谋",
            threshold = 75
        ),
        RandomEvent(
            id = "battle_chibi_fire_wind",
            title = "赤壁东风 · 火船破江",
            description = "江面雾气如墨，北军铁索连舟。东风忽起，火油已备。只等一声令下，火船便将撕开整片长江夜色。",
            option1 = "亲押火船直冲中军楼船（武力/健康高风险）",
            option2 = "计算风向水势，分三路点火夹击（智谋/统率）",
            option3 = "守住岸边接应线，专救落水盟军（魅力/名望）",
            statRequired = "智谋",
            threshold = 78
        ),
        RandomEvent(
            id = "battle_fancheng_flood",
            title = "樊城水围 · 七军震怖",
            description = "连日霖雨，汉水暴涨。敌军七营困于低洼，军中人马相践。若能开堤引水、趁乱攻营，便可一战成名。",
            option1 = "开堤放水，趁乱突入敌营（统率/高收益）",
            option2 = "先劝降敌军副将，瓦解其军心（魅力/政治）",
            option3 = "保守围城，断粮逼降（政治/低伤亡）",
            statRequired = "统率",
            threshold = 82
        ),
        RandomEvent(
            id = "battle_city_siege_ladder",
            title = "城池攻防 · 云梯血战",
            description = "你军压至城下，敌楼矢石如雨。云梯数次被焚，士卒望城胆寒。此城若破，整片郡县都将归入你的势力版图。",
            option1 = "亲登云梯，先士卒破城头（武力/健康风险）",
            option2 = "调弓兵压制城楼，步兵分段推进（统率/兵种）",
            option3 = "许诺城中百姓免掠，诱守军开门（政治/魅力）",
            statRequired = "统率",
            threshold = 76
        ),
        RandomEvent(
            id = "battle_cavalry_breakthrough",
            title = "铁骑突围 · 白刃裂阵",
            description = "敌军合围已成，旌旗从四面压来。你身边只余数百精骑，背后是伤兵与粮车，前方却有一线薄阵可破。",
            option1 = "聚拢骑兵，一鼓作气凿穿薄阵（武力/骑兵）",
            option2 = "弃辎重保主力，诱敌争抢后反击（智谋）",
            option3 = "命步兵结圆阵，稳步撤出包围（统率/士气）",
            statRequired = "武力",
            threshold = 72
        ),
        RandomEvent(
            id = "battle_final_fiefdom",
            title = "封侯之战 · 乱世定鼎",
            description = "多年征伐后，诸侯使者齐聚军帐。最后一座要城仍未归附。若此战取胜，你将由乱世武夫跃为一方封侯。",
            option1 = "倾全军总攻，三日内破城封侯（统率/兵力）",
            option2 = "离间守将家族，兵不血刃夺城（智谋/政治）",
            option3 = "广发檄文，收民心后围而不攻（魅力/名望）",
            statRequired = "统率",
            threshold = 88
        )
    )

    fun pickBattleEvent(currentYear: Int, reputation: Int, currentFaction: String): RandomEvent {
        val weighted = battleStoryEvents.filter { event ->
            when (event.id) {
                "battle_yellow_turban_vanguard" -> currentYear in 184..190
                "battle_hulao_duel" -> currentYear >= 190
                "battle_guandu_supply_raid" -> currentYear >= 198 && (currentFaction == "曹魏" || reputation >= 180)
                "battle_chibi_fire_wind" -> currentYear >= 206 && (currentFaction == "蜀汉" || currentFaction == "东吴" || reputation >= 220)
                "battle_fancheng_flood" -> currentYear >= 216 && (currentFaction == "蜀汉" || reputation >= 260)
                "battle_final_fiefdom" -> reputation >= 450
                else -> true
            }
        }
        return (if (weighted.isNotEmpty()) weighted else battleStoryEvents).random()
    }
}

fun GameViewModel.applySanguoBattleOutcome(ev: RandomEvent, option: Int): String {
    val basePower = command.value + martial.value / 2 + training.value / 3 + morale.value / 3
    val troopBonus = (infantry.value + cavalry.value * 2 + archers.value) / 240
    val talentBonus = if (talents.value.contains("统兵良将")) 18 else 0
    val roll = Random.nextInt(1, 41)
    val power = basePower + troopBonus + talentBonus + roll

    fun spendTroops(low: Int, high: Int) {
        val loss = Random.nextInt(low, high + 1)
        conscripts.value = (conscripts.value - loss).coerceAtLeast(0)
        infantry.value = (infantry.value - loss / 3).coerceAtLeast(0)
        cavalry.value = (cavalry.value - loss / 5).coerceAtLeast(0)
        archers.value = (archers.value - loss / 4).coerceAtLeast(0)
    }

    fun victoryLog(extra: String): String {
        reputation.value += Random.nextInt(90, 151)
        gold.value += Random.nextInt(180, 421)
        unificationProgress.value = (unificationProgress.value + Random.nextInt(3, 8)).coerceAtMost(100)
        morale.value = (morale.value + Random.nextInt(5, 13)).coerceAtMost(100)
        return "$extra 战鼓震野，捷报入帐：声望、金钱、统一进度与军心皆上涨。"
    }

    fun defeatLog(extra: String): String {
        health.value = (health.value - Random.nextInt(8, 21)).coerceAtLeast(1)
        reputation.value = (reputation.value - Random.nextInt(10, 36)).coerceAtLeast(0)
        morale.value = (morale.value - Random.nextInt(8, 20)).coerceAtLeast(0)
        spendTroops(220, 760)
        return "$extra 此战虽未全军覆没，却折损兵马、伤及元气，声望与士气受挫。"
    }

    return when (ev.id) {
        "battle_yellow_turban_vanguard" -> when (option) {
            1 -> if (martial.value + cavalry.value / 80 + roll >= 64) {
                spendTroops(120, 360)
                victoryLog("你披甲跃马，带骑兵从火光中反冲，连斩三名渠帅，黄巾夜袭当场崩散。")
            } else defeatLog("你冲入敌阵过深，乱兵举火封路，亲兵拼死才将你抢回营中。")
            2 -> if (power >= ev.threshold + 18) {
                training.value = (training.value + 8).coerceAtMost(100)
                victoryLog("你登台擂鼓，令旗三变，乱营重新合拢成铁桶，反把夜袭贼军围在营门。")
            } else defeatLog("鼓声未能压住营中惊乱，数处营栅被焚，辎重损失惨重。")
            else -> {
                gold.value = (gold.value - 80).coerceAtLeast(0)
                morale.value = (morale.value - 6).coerceAtLeast(0)
                "你放弃前营保住粮仓，虽失去部分辎重，却避免了全军夜溃。"
            }
        }
        "battle_hulao_duel" -> when (option) {
            1 -> if (martial.value + roll >= ev.threshold) {
                martial.value += 4
                victoryLog("你拍马出阵，三合挑飞敌将长槊，虎牢关前万军齐呼你的名号。")
            } else defeatLog("敌将力沉如山，你肩甲被劈裂，败回本阵。")
            2 -> if (intelligence.value + archers.value / 90 + roll >= ev.threshold) victoryLog("你假意退让，诱敌将追入伏弩之地，箭雨一落，敌阵气焰全消。") else defeatLog("敌将识破伏弩，反冲弓手阵脚，你军一时大乱。")
            else -> {
                val cost = 220
                gold.value = (gold.value - cost).coerceAtLeast(0)
                if (power >= ev.threshold) victoryLog("你重赏敢死士，三队轮击终于压垮敌将亲卫。") else defeatLog("重赏之下仍无人能破敌将锋芒，军中颇有怯意。")
            }
        }
        "battle_guandu_supply_raid" -> when (option) {
            1 -> if (intelligence.value + command.value / 2 + roll >= ev.threshold) {
                gold.value += 300
                victoryLog("你率死士衔枚夜行，火把落入乌巢粮垛，敌军十万粮道一夜化灰。")
            } else defeatLog("乌巢守军早有暗哨，你部被截在粮垛外，苦战到天明才突围。")
            2 -> if (power >= ev.threshold + 10) victoryLog("你佯攻正寨牵住敌军主力，友军奇袭成功，官渡战局由此逆转。") else defeatLog("正面佯攻变成硬仗，敌军重骑压来，你部被迫撤回。")
            else -> {
                gold.value += 120
                reputation.value += 45
                "你没有冒险夜袭，而是连续截断三批运粮车。敌军虽未崩溃，却已粮心动摇。"
            }
        }
        "battle_chibi_fire_wind" -> when (option) {
            1 -> if (martial.value + health.value / 2 + roll >= ev.threshold) {
                health.value = (health.value - 10).coerceAtLeast(1)
                victoryLog("你亲押火船撞入楼船，烈焰沿铁索狂奔，江面一夜红透。")
            } else defeatLog("火船未及中军便被拍杆击碎，你坠入江中，幸被亲兵拖回。")
            2 -> if (intelligence.value + command.value / 2 + roll >= ev.threshold) victoryLog("你按风向分三路纵火，火势连环爆起，北军水寨顷刻崩裂。") else defeatLog("江风忽乱，火船偏离水道，未能烧穿敌军主阵。")
            else -> {
                charisma.value += 4
                reputation.value += 90
                "你守住岸边接应线，救回大批盟军落水士卒。虽非首功，却收尽军心。"
            }
        }
        "battle_fancheng_flood" -> when (option) {
            1 -> if (power >= ev.threshold + 8) victoryLog("你开堤引水，趁七军混乱直扑帅帐，敌营战马浮尸遍野。") else defeatLog("水势过猛反冲己方前阵，攻势被迫中止。")
            2 -> if (charisma.value + politics.value + roll >= ev.threshold + 20) victoryLog("你密遣书信打动敌副将，其夜献营门，七军不战自乱。") else defeatLog("劝降密信被截，敌军反倒加固营门。")
            else -> {
                politics.value += 5
                reputation.value += 60
                "你围城断粮，不急攻硬啃。月余后敌军粮尽，军心渐散。"
            }
        }
        "battle_city_siege_ladder" -> when (option) {
            1 -> if (martial.value + health.value / 2 + roll >= ev.threshold) {
                health.value = (health.value - 12).coerceAtLeast(1)
                handleCityConquest(citiesList.random().id)
                victoryLog("你亲登云梯，血染甲叶，第一个翻上城头，守军惊惧弃械。")
            } else defeatLog("城头滚木砸断云梯，你险些坠城，攻城暂时受阻。")
            2 -> if (power + archers.value / 140 >= ev.threshold + 12) {
                handleCityConquest(citiesList.random().id)
                victoryLog("弓兵压住城楼，步兵分段推进，终于从西北角撕开缺口。")
            } else defeatLog("城楼箭石太密，你军推进数次皆被压回壕沟。")
            else -> if (politics.value + charisma.value + roll >= ev.threshold + 18) {
                handleCityConquest(citiesList.random().id)
                victoryLog("你许诺免掠安民，城中父老联络守卒开门，城池兵不血刃归降。")
            } else defeatLog("守将不信你的檄文，反将来使悬于城头示众。")
        }
        "battle_cavalry_breakthrough" -> when (option) {
            1 -> if (martial.value + cavalry.value / 70 + roll >= ev.threshold) victoryLog("你聚拢精骑，枪锋如雪，一口气凿穿敌军薄阵，护住伤兵粮车突围。") else defeatLog("敌军长枪阵比预想更厚，骑兵冲锋被硬生生截断。")
            2 -> if (intelligence.value + roll >= ev.threshold) victoryLog("你弃辎重诱敌争抢，趁敌阵散乱回马反杀，竟从死地打出胜机。") else defeatLog("敌军不上当，反从两翼合围，你部被迫丢盔弃甲。")
            else -> if (command.value + morale.value / 2 + roll >= ev.threshold) victoryLog("你命步兵结圆阵缓退，枪盾如林，敌骑数次冲击皆不能入。") else defeatLog("圆阵移动太慢，后队被敌军咬住，折损惨重。")
        }
        "battle_final_fiefdom" -> when (option) {
            1 -> if (power + conscripts.value / 300 >= ev.threshold + 18) {
                currentJob.value = "封侯将军"
                reputation.value += 260
                victoryLog("你倾全军三日猛攻，城门终破。诸侯使者入帐拜贺，你正式封侯。")
            } else defeatLog("总攻未能破城，精锐折损，封侯之机暂时错过。")
            2 -> if (intelligence.value + politics.value + roll >= ev.threshold + 24) {
                currentJob.value = "开府谋侯"
                reputation.value += 240
                victoryLog("你离间守将宗族，城中夜半倒戈。你未流多少血，便取下最后要城。")
            } else defeatLog("离间计被识破，守将反用假降诱你损兵。")
            else -> if (charisma.value + reputation.value / 4 + roll >= ev.threshold + 12) {
                currentJob.value = "仁威侯"
                reputation.value += 220
                victoryLog("你广发檄文、安抚百姓，城中民心先降，守军最终开门献印。")
            } else defeatLog("檄文虽美，却未能撼动城中豪族，围城旷日持久。")
        }
        else -> "战场扩展包未识别事件：${ev.id}。"
    }
}
