package com.example.ui

import com.example.R

/**
 * 三国人生模拟器内容增强包。
 *
 * 先作为独立素材库加入项目，不改动原 GameViewModel，避免破坏现有编译。
 * 下一步可把 premiumEvents 接入 triggerRandomEvent()，
 * 并在 EventChoiceDialog 中根据 illustrationRes 显示插图。
 */
object SanguoContentPack {

    data class IllustratedEvent(
        val event: RandomEvent,
        val illustrationRes: Int,
        val rewardHint: String,
        val riskHint: String
    )

    val premiumEvents: List<IllustratedEvent> = listOf(
        IllustratedEvent(
            event = RandomEvent(
                id = "evt_yellow_turban_oath",
                title = "黄巾乱起",
                description = "巨鹿张角举事，黄巾遍地。县城门外鼓声如雷，百姓惊逃，豪强观望。你被推到风口浪尖：是趁乱建功，还是保全乡里？",
                option1 = "召集乡勇守城，正面迎战黄巾军（统率检定）",
                option2 = "暗访渠帅营帐，以粮草换百姓平安（智谋检定）",
                option3 = "护送老弱南迁，暂避锋芒（稳妥路线）",
                statRequired = "统率",
                threshold = 58
            ),
            illustrationRes = R.drawable.illust_yellow_turban_oath,
            rewardHint = "成功可提升名望、统率与兵力。",
            riskHint = "失败会损失健康、金钱与乡勇。"
        ),
        IllustratedEvent(
            event = RandomEvent(
                id = "evt_luoyang_night",
                title = "洛阳夜行",
                description = "董卓挟天子西迁，洛阳人心浮动。你在夜色中发现一批宫中密卷和离散官吏，带走他们也许会改变你的命运。",
                option1 = "进入宫阙旧署，抢救密卷线索（武力检定）",
                option2 = "收拢官吏，打探朝廷秘闻（政治检定）",
                option3 = "带百姓撤离是非之地（名望路线）",
                statRequired = "政治",
                threshold = 62
            ),
            illustrationRes = R.drawable.illust_luoyang_night,
            rewardHint = "成功可获得政治、名望或朝廷线索。",
            riskHint = "行动失败会造成健康和声望损失。"
        ),
        IllustratedEvent(
            event = RandomEvent(
                id = "evt_longzhong_strategy",
                title = "隆中对策",
                description = "茅庐外细雨如丝，一位布衣青年摊开天下地图，指点荆益、江东、关中之势。若你能听懂此局，乱世将不再只是乱世。",
                option1 = "请其出山共谋天下（三顾礼贤，魅力检定）",
                option2 = "与其彻夜推演三分之策（智谋检定）",
                option3 = "留下金帛与粮草，结一段善缘（稳妥交情）",
                statRequired = "智谋",
                threshold = 70
            ),
            illustrationRes = R.drawable.illust_longzhong_strategy,
            rewardHint = "成功可提升智谋、政治，并增加诸葛亮关系。",
            riskHint = "魅力不足会被视为俗客，收益降低。"
        ),
        IllustratedEvent(
            event = RandomEvent(
                id = "evt_red_cliff_wind",
                title = "赤壁东风",
                description = "江面铁索连舟，曹军旗帜遮天。东南风迟迟不来，营中诸将心神动摇。你被推到祭坛前，必须在军心动摇前定计。",
                option1 = "登坛借风，鼓舞三军火攻曹营（魅力检定）",
                option2 = "夜派小船断索，暗中制造破绽（智谋检定）",
                option3 = "建议保守撤军，保存实力（政治路线）",
                statRequired = "魅力",
                threshold = 66
            ),
            illustrationRes = R.drawable.illust_red_cliff_wind,
            rewardHint = "成功可大幅提升名望、统率，并改变统一进度。",
            riskHint = "失败会导致兵力与名望受损。"
        )
    )

    val endingTitles: List<String> = listOf(
        "乱世枭雄", "汉室忠臣", "江东名宿", "蜀汉柱石", "魏廷权臣",
        "白手称王", "富甲天下", "一代军神", "青史谋主", "归隐山林"
    )
}
