package com.example.ui

/**
 * 三国版“宗门模拟”内容包。
 *
 * 借鉴修仙宗门模拟器的做法：先用稳定数据表承载玩法内容，
 * UI 和 ViewModel 只消费这些配置，避免继续把内容硬塞进主逻辑。
 */
object SanguoSectInspiredPack {
    data class Facility(
        val id: String,
        val name: String,
        val type: String,
        val cost: Int,
        val effect: String,
        val actionText: String
    )

    data class StrategyManual(
        val id: String,
        val name: String,
        val school: String,
        val rarity: String,
        val effect: String
    )

    data class GovernanceRoute(
        val id: String,
        val name: String,
        val description: String,
        val coreStats: String
    )

    val facilities = listOf(
        Facility("war_hall", "讲武堂", "武备", 100, "研习刀枪骑射，提升武力。", "练武"),
        Facility("strategy_room", "谋略阁", "谋略", 100, "推演兵法权谋，提升智谋。", "读书"),
        Facility("drill_ground", "军营校场", "军政", 120, "整训部曲，提升统率与士气。", "练兵"),
        Facility("market_post", "集市驿馆", "经营", 80, "互市通货，积累金钱与人脉。", "经商"),
        Facility("clan_house", "宗祠府库", "家族", 150, "保存功业，培养继承人。", "治家"),
        Facility("office_yamen", "郡府衙署", "仕途", 180, "处理政务，累积声望与政治。", "理政")
    )

    val manuals = listOf(
        StrategyManual("six_tactics", "太公六韬", "兵法", "紫", "武力与统率成长路线。"),
        StrategyManual("three_strategies", "黄石三略", "谋略", "紫", "智谋与政治成长路线。"),
        StrategyManual("wei_wu", "魏武兵书", "军略", "橙", "适合曹魏/霸道路线。"),
        StrategyManual("longzhong", "隆中对策", "天下", "橙", "适合蜀汉/经营路线。"),
        StrategyManual("jiangdong", "江东水战图", "水战", "蓝", "适合东吴/水师路线。"),
        StrategyManual("merchant_book", "货殖通论", "经营", "蓝", "适合商贾/府库路线。")
    )

    val routes = listOf(
        GovernanceRoute("warlord", "诸侯霸业", "扩兵、攻城、封爵，走割据称雄路线。", "武力 / 统率 / 声望"),
        GovernanceRoute("official", "文臣仕途", "理政、升官、治郡，走庙堂权臣路线。", "政治 / 智谋 / 声望"),
        GovernanceRoute("merchant", "豪商府库", "经商、囤货、结交名士，走富甲一方路线。", "金钱 / 魅力 / 人脉"),
        GovernanceRoute("clan", "宗族传承", "婚姻、子嗣、家业继承，走多代经营路线。", "魅力 / 金钱 / 后嗣")
    )
}
