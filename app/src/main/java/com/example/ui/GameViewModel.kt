package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Game state
sealed class GameState {
    object Title : GameState()
    object BirthSetup : GameState()
    object Playing : GameState()
    data class EventChoice(val event: RandomEvent, val onOptionSelected: (Int) -> Unit) : GameState()
    data class DuelScene(val opponentName: String, val opponentMartial: Int, val onDuelComplete: (Boolean, String) -> Unit) : GameState()
    data class BattleScene(val campaign: Campaign, val onBattleComplete: (Boolean, String) -> Unit) : GameState()
    data class GameOver(val finalRecord: CharacterRecord, val hasHeir: Boolean) : GameState()
}

// Famous historical campaigns to join
data class Campaign(
    val id: String,
    val name: String,
    val year: Int,
    val enemyName: String,
    val factionRequired: String,
    val enemyForceStrength: Int, // Difficulty (1-100)
    val description: String
)

// Random Choose-Your-Own-Adventure Event
data class RandomEvent(
    val id: String,
    val title: String,
    val description: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val statRequired: String = "武力", 
    val threshold: Int = 50
)

// Market Merchant item
data class TradeItem(
    val name: String,
    val basePrice: Int,
    var currentPrice: Int = basePrice
)

// Tactical Battle Round Status
data class BattleRoundInfo(
    val roundNum: Int,
    val logText: String,
    val playerLoss: Int,
    val enemyLoss: Int
)

// Heir / Child description
data class ChildInfo(
    var name: String,
    var age: Int,
    var gender: String, // 男 / 女
    var martial: Int,
    var intelligence: Int,
    var command: Int,
    var politics: Int,
    var charisma: Int
)

// City description
data class CityInfo(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val factionDefault: String,
    val baseIncome: Int,
    val description: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _allRecordsFromDb = MutableStateFlow<List<CharacterRecord>>(emptyList())
    val recordsListSnapshot: StateFlow<List<CharacterRecord>> = _allRecordsFromDb

    val allPastRecords: StateFlow<List<CharacterRecord>> = _allRecordsFromDb
    
    private val _activeSession = MutableStateFlow<ActiveGameSession?>(null)
    val activeSession: StateFlow<ActiveGameSession?> = _activeSession

    // Current screen layout state
    private val _gameState = MutableStateFlow<GameState>(GameState.Title)
    val gameState: StateFlow<GameState> = _gameState

    // In-game dynamic values
    val surname = MutableStateFlow("")
    val courtesy = MutableStateFlow("")   // 字
    val alias    = MutableStateFlow("")   // 号
    val name = MutableStateFlow("")
    val gender = MutableStateFlow("男")
    val hometown = MutableStateFlow("幽州")
    val origin = MutableStateFlow("寒门庶民")
    val talents = MutableStateFlow(listOf<String>())
    
    val age = MutableStateFlow(15)
    val currentYear = MutableStateFlow(184)
    val health = MutableStateFlow(100)
    val gold = MutableStateFlow(500)
    val reputation = MutableStateFlow(50)
    val unificationProgress = MutableStateFlow(10)

    val martial = MutableStateFlow(50)
    val intelligence = MutableStateFlow(50)
    val command = MutableStateFlow(50)
    val politics = MutableStateFlow(50)
    val charisma = MutableStateFlow(50)

    val currentFaction = MutableStateFlow("在野游侠")
    val currentJob = MutableStateFlow("江湖豪侠")
    val spouse = MutableStateFlow("无")
    val childrenCount = MutableStateFlow(0)

    val lifeLogs = MutableStateFlow(listOf<String>())
    val inventory = MutableStateFlow(mutableMapOf<String, Int>()) // ItemName -> Qty
    val relationships = MutableStateFlow(mutableMapOf<String, Int>()) // FigureName -> Friendship

    // MILITARY EXPANSION STATES
    val conscripts = MutableStateFlow(1000)
    val infantry = MutableStateFlow(400)
    val cavalry = MutableStateFlow(300)
    val archers = MutableStateFlow(300)
    val morale = MutableStateFlow(75)
    val training = MutableStateFlow(75)

    // HEIR EXPANSION STATES
    val childrenList = MutableStateFlow(listOf<ChildInfo>())

    // MAP CITIES EXPANSION STATES
    val cityOwners = MutableStateFlow<Map<String, String>>(mapOf())

    // Static list of 10 Provinces for the Interactive visual map
    val citiesList = listOf(
        CityInfo("luoyang", "洛阳", 0.5f, 0.45f, "董卓", 300, "帝都神枢，中原锁钥。"),
        CityInfo("changan", "长安", 0.35f, 0.45f, "董卓", 260, "秦川门户，丝路起点。"),
        CityInfo("xuchang", "许昌", 0.62f, 0.52f, "曹魏", 220, "豫州都会，曹瞒本据。"),
        CityInfo("jianye", "建业", 0.78f, 0.65f, "东吴", 250, "江东帝王宅，长江天堑。"),
        CityInfo("chengdu", "成都", 0.28f, 0.72f, "在野", 240, "益州天府，沃野千里。"),
        CityInfo("xiangyang", "襄阳", 0.5f, 0.6f, "黄巾贼", 200, "兵家必争，荆襄重镇。"),
        CityInfo("yecheng", "邺城", 0.58f, 0.32f, "袁绍", 210, "河北坚城，魏郡治所。"),
        CityInfo("beiping", "北平", 0.72f, 0.22f, "袁绍", 180, "白马塞口，北阻胡尘。"),
        CityInfo("xiapi", "下邳", 0.72f, 0.45f, "曹魏", 200, "徐州重足，吕布授首处。"),
        CityInfo("tianshui", "天水", 0.22f, 0.46f, "在野", 150, "陇右重镇，姜维故里。")
    )

    // Custom tactical campaign/battle configuration state
    var selectedCampaignTarget = MutableStateFlow<CityInfo?>(null)

    // Market trading price definitions
    val marketItems = MutableStateFlow(listOf(
        TradeItem("蜀锦", 200),
        TradeItem("战马", 450),
        TradeItem("贡茶", 80),
        TradeItem("青铜戈", 150),
        TradeItem("七星宝刀", 1800)
    ))

    // List of historical figures for standard banquet/friend interactions
    val historicalFigures = listOf(
        "赵云" to 190, "关羽" to 184, "张飞" to 184, "诸葛亮" to 201, "曹操" to 184, 
        "刘备" to 184, "孙权" to 196, "周瑜" to 192, "貂蝉" to 189, "孙尚香" to 198,
        "黄月英" to 201, "甄姬" to 200, "大乔" to 195, "小乔" to 195, "司马懿" to 205
    )

    init {
        // Initialize records from database flow
        viewModelScope.launch {
            repository.allRecords.collect { records ->
                _allRecordsFromDb.value = records
            }
        }
        viewModelScope.launch {
            repository.activeSessionFlow.collect { session ->
                _activeSession.value = session
            }
        }
        resetCitiesMap()
    }

    fun resetCitiesMap() {
        val initialMap = mutableMapOf<String, String>()
        citiesList.forEach { city ->
            initialMap[city.id] = city.factionDefault
        }
        cityOwners.value = initialMap
    }

    fun enterBirthSetup() {
        surname.value = getRandomSurname()
        this.name.value = getRandomGivenName(gender.value)
        _gameState.value = GameState.BirthSetup
    }

    fun exitToTitle() {
        _gameState.value = GameState.Title
    }

    // Allocate random start talents
    fun rollSetupTalents(): List<String> {
        val pool = listOf("枭雄之姿", "万夫莫敌", "神机妙算", "富甲一方", "仁德传世", "统兵良将", "长寿延绵")
        return pool.shuffled().take(3)
    }

    fun randomizeIdentity() {
        surname.value = getRandomSurname()
        this.name.value = getRandomGivenName(gender.value)
        courtesy.value = getRandomCourtesy(gender.value)
        alias.value    = ""
    }
    private fun getRandomCourtesy(g: String): String {
        val male   = listOf("子龙","孟德","玄德","仲谋","孔明","元直","伯符","公瑾","翼德","子义",
                            "文远","子廉","文则","公达","奉孝","德祖","文若","仲颖","兴霸","伯言")
        val female = listOf("月英","仁寿","思远","清漪","慕云","婵月","玉怀","芝兰","素雪","冬梅")
        return if (g == "女") female.random() else male.random()
    }

    // Trigger starting stats and play the game
    fun startNewGame(
        allocatedMartial: Int,
        allocatedIntel: Int,
        allocatedCommand: Int,
        allocatedPol: Int,
        allocatedCharisma: Int,
        selectedTalents: List<String>
    ) {
        age.value = 15
        currentYear.value = 184
        health.value = 100
        spouse.value = "无"
        childrenCount.value = 0
        unificationProgress.value = 15
        currentFaction.value = "在野游侠"
        currentJob.value = "江湖豪侠"

        // Applied origin stats shifts and starting gold
        var startGold = 200
        var mBonus = 0
        var iBonus = 0
        var cBonus = 0
        var pBonus = 0
        var chBonus = 0

        when (origin.value) {
            "寒门庶民" -> { mBonus = 15; iBonus = -5; startGold = 100 }
            "破落士族" -> { iBonus = 10; pBonus = 5; startGold = 200 }
            "豪强子弟" -> { cBonus = 10; mBonus = 5; startGold = 350 }
            "商贾大富" -> { chBonus = 10; mBonus = -5; startGold = 1000 }
            "边军孤儿" -> { mBonus = 20; cBonus = 5; startGold = 80 }
            "黄巾遗民" -> { mBonus = 10; chBonus = 5; startGold = 50 }
            "没落宗亲" -> { pBonus = 15; chBonus = 5; startGold = 150 }
            "游侠少年" -> { mBonus = 10; chBonus = 8; startGold = 120 }
            "县吏之家" -> { pBonus = 10; iBonus = 5; startGold = 250 }
            "书院弟子" -> { iBonus = 20; pBonus = 5; startGold = 180 }
            "工匠之家" -> { iBonus = 8; startGold = 220 }
            "世家子弟" -> { iBonus = 15; cBonus = 5; pBonus = 10; startGold = 400 }
        }

        // Apply hometown bonuses
        when (hometown.value) {
            "幽州" -> mBonus += 5
            "冀州" -> pBonus += 5
            "并州" -> { mBonus += 3; cBonus += 3 }
            "青州" -> iBonus += 5
            "徐州" -> startGold += 200
            "兖州" -> { pBonus += 3; iBonus += 3 }
            "豫州" -> chBonus += 5
            "司隶" -> { pBonus += 8; startGold += 100 }
            "凉州" -> cBonus += 5
            "荆州" -> iBonus += 5
            "扬州" -> { startGold += 150; chBonus += 3 }
            "益州" -> { iBonus += 3; startGold += 100 }
            "交州" -> chBonus += 8
        }

        gold.value = startGold
        martial.value = allocatedMartial + mBonus
        intelligence.value = allocatedIntel + iBonus
        command.value = allocatedCommand + cBonus
        politics.value = allocatedPol + pBonus
        charisma.value = allocatedCharisma + chBonus
        talents.value = selectedTalents

        // Apply starting talent bonuses immediately
        if (selectedTalents.contains("万夫莫敌")) martial.value += 15
        if (selectedTalents.contains("神机妙算")) intelligence.value += 15
        if (selectedTalents.contains("统兵良将")) command.value += 15
        if (selectedTalents.contains("富甲一方")) gold.value += 500
        if (selectedTalents.contains("仁德传世")) charisma.value += 10

        reputation.value = 50 + (if (selectedTalents.contains("枭雄之姿")) 50 else 0)

        inventory.value = mutableMapOf()
        relationships.value = mutableMapOf()
        
        // Reset Military Details
        conscripts.value = 1200
        infantry.value = 500
        cavalry.value = 300
        archers.value = 400
        morale.value = 75
        training.value = 75

        // Reset Children
        childrenList.value = emptyList()

        // Reset Cities
        resetCitiesMap()

        lifeLogs.value = mutableListOf(
            "公元${currentYear.value}年（${age.value}岁）：在${hometown.value}的宿命之野，狼烟四起，生而为“${origin.value}”的${surname.value}${this.name.value}抱负在胸，起兵乱世高歌前行。"
        )

        regenerateMarketPrices()
        _gameState.value = GameState.Playing
        saveCurrentGameToDb()
    }

    // Resume saved life simulator details
    fun loadSavedGame() {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                surname.value = session.surname
                courtesy.value = session.courtesy ?: ""
                alias.value    = session.alias    ?: ""
                this@GameViewModel.name.value = session.name
                gender.value = session.gender
                age.value = session.age
                currentYear.value = session.currentYear
                hometown.value = session.hometown
                origin.value = session.origin
                martial.value = session.martial
                intelligence.value = session.intelligence
                command.value = session.command
                politics.value = session.politics
                charisma.value = session.charisma
                reputation.value = session.reputation
                gold.value = session.gold
                currentFaction.value = session.currentFaction
                currentJob.value = session.currentJob
                spouse.value = session.spouse
                childrenCount.value = session.childrenCount
                unificationProgress.value = session.unificationProgress

                // Parse talents
                talents.value = if (session.talents.isEmpty()) emptyList() else session.talents.split(",")

                // Parse inventory (and unpacked custom military variables)
                val invMap = mutableMapOf<String, Int>()
                var milCons = 1200
                var milInf = 500
                var milCav = 300
                var milArc = 400
                var milMor = 75
                var milTra = 75
                
                val heirsRecovered = mutableListOf<ChildInfo>()
                val restoredCityMap = mutableMapOf<String, String>()
                citiesList.forEach { restoredCityMap[it.id] = it.factionDefault }

                if (session.inventory.isNotEmpty()) {
                    session.inventory.split(",").forEach { itemStr ->
                        val parts = itemStr.split(":")
                        if (parts.size == 2) {
                            val key = parts[0]
                            val valu = parts[1].toIntOrNull() ?: 1
                            if (key.startsWith("MIL_")) {
                                when (key) {
                                    "MIL_CONS" -> milCons = valu
                                    "MIL_INF" -> milInf = valu
                                    "MIL_CAV" -> milCav = valu
                                    "MIL_ARC" -> milArc = valu
                                    "MIL_MOR" -> milMor = valu
                                    "MIL_TRA" -> milTra = valu
                                }
                            } else if (key.startsWith("HEIR_")) {
                                // HEIR_name:age_gender_martial_intel_command_politics_charisma
                                val sVal = parts[1]
                                val subP = sVal.split("_")
                                if (subP.size >= 7) {
                                    heirsRecovered.add(
                                        ChildInfo(
                                            name = key.replace("HEIR_", ""),
                                            age = subP[0].toIntOrNull() ?: 5,
                                            gender = subP[1],
                                            martial = subP[2].toIntOrNull() ?: 40,
                                            intelligence = subP[3].toIntOrNull() ?: 40,
                                            command = subP[4].toIntOrNull() ?: 40,
                                            politics = subP[5].toIntOrNull() ?: 40,
                                            charisma = subP[6].toIntOrNull() ?: 40
                                        )
                                    )
                                }
                            } else if (key.startsWith("MAPOWNER_")) {
                                val cityId = key.replace("MAPOWNER_", "")
                                restoredCityMap[cityId] = parts[1] // Store string faction value
                            } else {
                                invMap[key] = valu
                            }
                        }
                    }
                }
                
                conscripts.value = milCons
                infantry.value = milInf
                cavalry.value = milCav
                archers.value = milArc
                morale.value = milMor
                training.value = milTra
                childrenList.value = heirsRecovered
                cityOwners.value = restoredCityMap
                inventory.value = invMap

                // Parse relations
                val relMap = mutableMapOf<String, Int>()
                if (session.relations.isNotEmpty()) {
                    session.relations.split(";").forEach { relStr ->
                        val parts = relStr.split(":")
                        if (parts.size == 2) {
                            relMap[parts[0]] = parts[1].toIntOrNull() ?: 10
                        }
                    }
                }
                relationships.value = relMap

                // Parse lifeLogs
                lifeLogs.value = if (session.lifeLogs.isEmpty()) emptyList() else session.lifeLogs.split("|")

                regenerateMarketPrices()
                _gameState.value = GameState.Playing
            }
        }
    }

    // Save active state into Room Database
    fun saveCurrentGameToDb() {
        // Collect inventory entries
        val activeInvList = inventory.value.map { "${it.key}:${it.value}" }.toMutableList()
        
        // Pack military attributes
        activeInvList.add("MIL_CONS:${conscripts.value}")
        activeInvList.add("MIL_INF:${infantry.value}")
        activeInvList.add("MIL_CAV:${cavalry.value}")
        activeInvList.add("MIL_ARC:${archers.value}")
        activeInvList.add("MIL_MOR:${morale.value}")
        activeInvList.add("MIL_TRA:${training.value}")

        // Pack heirs children list
        childrenList.value.forEach { child ->
            activeInvList.add("HEIR_${child.name}:${child.age}_${child.gender}_${child.martial}_${child.intelligence}_${child.command}_${child.politics}_${child.charisma}")
        }

        // Pack city owners map data
        cityOwners.value.forEach { (cityId, ownedFaction) ->
            activeInvList.add("MAPOWNER_$cityId:$ownedFaction")
        }

        val currentInvStr = activeInvList.joinToString(",")
        val currentRelStr = relationships.value.map { "${it.key}:${it.value}" }.joinToString(";")
        val currentLogStr = lifeLogs.value.joinToString("|")
        val currentTalentStr = talents.value.joinToString(",")

        val session = ActiveGameSession(
            name = name.value,
            surname = surname.value,
            courtesy = courtesy.value,
            alias = alias.value,
            gender = gender.value,
            age = age.value,
            currentYear = currentYear.value,
            hometown = hometown.value,
            origin = origin.value,
            martial = martial.value,
            intelligence = intelligence.value,
            command = command.value,
            politics = politics.value,
            charisma = charisma.value,
            reputation = reputation.value,
            gold = gold.value,
            currentFaction = currentFaction.value,
            currentJob = currentJob.value,
            spouse = spouse.value,
            childrenCount = childrenList.value.size,
            lifeLogs = currentLogStr,
            unificationProgress = unificationProgress.value,
            inventory = currentInvStr,
            relations = currentRelStr,
            talents = currentTalentStr
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.saveSession(session)
            }
        }
    }

    // Advance 1 Year (YEAR ADVANCE BUTTON)
    fun advanceAge() {
        if (health.value <= 10) {
            triggerDeath("由于常年征伐积劳成疾、伤病复发，你终无法支撑疲敝的身躯倒在床榻之上，遗憾离世。")
            return
        }

        age.value += 1
        currentYear.value += 1

        // Age up children too!
        val upgradedChildren = childrenList.value.map { child ->
            child.copy(age = child.age + 1)
        }
        childrenList.value = upgradedChildren

        // Add year-end salary to gold
        val salary = JOB_SALARY[currentJob.value] ?: 50
        gold.value += salary

        // Health decaying a little at high ages
        if (age.value > 55) {
            val naturalDecay = Random.nextInt(2, 6)
            health.value = (health.value - naturalDecay).coerceAtLeast(1)
        }

        // Random health fluctuations
        if (Random.nextInt(100) < 15) {
            health.value = (health.value - Random.nextInt(5, 12)).coerceAtLeast(1)
            addLog("公元${currentYear.value}年（${age.value}岁）：偶染风寒气候无常，身体有些吃不消，气血受损，需加以调理。")
        }

        // Unification progress shifts depending on battles
        unificationProgress.value = (unificationProgress.value + Random.nextInt(-1, 3)).coerceIn(10, 95)

        // Generate dynamic price fluctuations
        regenerateMarketPrices()

        // Core death chance check
        var maxLifeLimit = 65
        if (talents.value.contains("长寿延绵")) maxLifeLimit += 15

        if (age.value >= maxLifeLimit) {
            val deathOdds = (age.value - maxLifeLimit + 1) * 12
            if (Random.nextInt(100) < deathOdds || health.value <= 0) {
                triggerDeath("安详地在岁月的流河中合上双眼，子弟环侍榻前，执手含笑归西，寿终正寝。")
                return
            }
        }

        // User requested: "玩家每一年选择一次人生事件" (Players select a life event every single year)
        // Set event trigger to 100% (Guaranteed Choose-Your-Own-Adventure Interactive Event every single year)!
        triggerRandomEvent()
    }

    private fun addLog(text: String) {
        val updated = lifeLogs.value.toMutableList()
        updated.add(text)
        lifeLogs.value = updated
    }

    private fun triggerRandomEvent() {
        val ev = if (SanguoAnnualEventPicker.shouldUseBattleStory(
                currentYear = currentYear.value,
                reputation = reputation.value,
                currentFaction = currentFaction.value,
                currentJob = currentJob.value
            )) {
            SanguoAnnualEventPicker.pickBattleStory(
                currentYear = currentYear.value,
                reputation = reputation.value,
                currentFaction = currentFaction.value
            )
        } else {
            // 30%概率触发图文premium事件
            val premiumRoll = Random.nextInt(10)
            if (premiumRoll < 3 && SanguoContentPack.premiumEvents.isNotEmpty()) {
                SanguoContentPack.premiumEvents.random().event
            } else when (Random.nextInt(40)) {
            0 -> RandomEvent(
                "evt_bandit",
                "剪径山贼",
                "正值大雪四方，前方崎岖山道冲出一彪绿林好汉，将你与战马重重围住挑衅抢掠！",
                "横刀大喝，领麾下亲兵冲锋突击（武力+兵力）",
                "舌发利箭，剖明利害大义收编（智谋+声望）",
                "破财消灾，交付150两纹银绕行（求财避祸，必成）",
                "武力",
                60
            )
            1 -> RandomEvent(
                "evt_hermit",
                "仙陵奇遇",
                "你游历至终南山幽谷中，一位鹤发童颜、身披羽衣的古德奇人拦路，愿授你平生绝学术术。",
                "求取玄门太极丹，重塑周身（恢复健康，体力+100）",
                "求取太公奇门奇袭阵图（统率、骑兵技术+25）",
                "婉拒飞升，求问拯救乱世生民之道（名望与德行暴增）",
                "德行",
                0
            )
            2 -> RandomEvent(
                "evt_famine",
                "流民叩关",
                "关外漫山遍野皆是遭灾饥民，数万饿殍携老扶幼围攻你所在郡邑，哭声盈天，乱而生变。",
                "倾家荡产开粥厂施粥大拯饥荒（花费400金，名望暴涨+200）",
                "铁血围困，强征流民中青壮入伍充军（征发兵力+1200，失名）",
                "紧闭城防，壁垒高悬，明哲保身（无变化，安危必保）",
                "政治",
                55
            )
            3 -> RandomEvent(
                "evt_challenge",
                "席间挑衅",
                "在庆功大宴上，敌对阵营一位大将醉意醺醺，出列舞剑削断你宴桌一角，睥睨高呼问敢切磋否？",
                "慨然拂面，按剑下场赏其两记铁拳（武力单挑挑战）",
                "神情泰然，浮一大白高唱英雄志向（魅力德行挑战）",
                "隐忍不发，自领下座避其锋芒（政治精深+10，降名声）",
                "武力",
                70
            )
            4 -> RandomEvent(
                "evt_investment",
                "巨商引资",
                "一位徽商巨鳄私下会见，托买一批绝迹名驹，若你能帮其在关防中签字行融，必以重金酬复。",
                "大举斥资500白银买通戍卒合流（买马神驹或巨资反馈）",
                "暗下都衙查封走私大马直接法办（收公300两，统率+10）",
                "严词驳回，大发其通政名士大度（声望德行大涨）",
                "智谋",
                65
            )
            5 -> RandomEvent(
                "evt_recruit",
                "虎将入庄",
                "山庄之前，一位身长八尺、手提青龙偃月、身跨赤兔之美髯英雄正在酒楼买醉，意气萧索。",
                "急携府库名酒、奉上300金金叶竭诚招募（花费300金，狂增情谊纽带）",
                "在客栈大堂题壁泼墨，大发慷慨论（名望+45，智谋+10）",
                "清心茶罢，点头作揖赞其神姿（交游渐深）",
                "魅力",
                65
            )
            6 -> RandomEvent(
                "evt_patron",
                "明主垂青",
                "当今各道诸侯割据。你名声惊扰了一路英主。对方特遗使臣携玉带锦帛下书，招纳你行刺探、掌内政。",
                "顺天承命，携部曲前投其麾下（全额官职直升，兵+500）",
                "厚礼相拒，称卧龙在泥不敢惊天（政治大涨+15，名声+40）",
                "收下玉带，将礼物散发乡邻（魅力+15）",
                "政治",
                60
            )
            else -> RandomEvent(
                "evt_plague",
                "疫流天殃",
                "郡野瘟疫四处横行。哀鸿铺野，流医皆避。百余县无一寸生息，死伤日巨。",
                "倾尽药物亲率全军扎营行义施药（名声+180，道德重铸）",
                "耗150金高挂艾草焚香紧缩，静养百骸（生命全面复原）",
                "囤积板蓝根提价十倍倾售（横财+1200金，失政治声望）",
                "政治",
                60
            )

            8 -> RandomEvent("evt_spy","奸细落网","你麾下斥候抓住一名敌营细作，对方愿以情报换命。","严刑逼问，套取军情（统率检定）","假意放人，暗中跟踪（智谋检定）","直接处决，震慑军心（威严+20）","统率",55)
            9 -> RandomEvent("evt_drought","大旱告急","连月无雨，粮草告急，军心浮动。你须做出决断。","开仓放粮，稳定民心（花400金，名望+80）","强征周边郡县粮草（兵力+500，失名望）","组织人力挖渠引水（政治+15，德行+10）","政治",60)
            10 -> RandomEvent("evt_duel_invite","武林帖战","一位游侠名宿下帖，邀你月下比武，胜者名扬天下。","欣然赴约，以武会友（武力检定）","婉拒帖战，赠诗一首化干戈（智谋+魅力）","不战而屈，以名位压制（政治检定）","武力",72)
            11 -> RandomEvent("evt_merchant_caravan","丝路商队","一支西域商队途经，带来奇货异宝，也带来绝密情报。","重金购入情报（花300金，智谋+10）","强买奇货，充实军资（金钱+500，失道义）","礼待商队，开通贸易线（每年额外收入+150）","魅力",58)
            12 -> RandomEvent("evt_flood","洪水暴发","黄河决口，数县被淹，灾民涌入你辖区。","倾尽府库赈灾（花500金，名望+120）","征调灾民为兵（兵+800，失名）","上书朝廷申请援助（政治+10，等待结果）","政治",65)
            13 -> RandomEvent("evt_assassin","刺客夜袭","深夜，一名蒙面刺客翻墙而入，直扑你卧房！","拔剑应战，格杀刺客（武力检定）","高呼侍卫，布阵围捕（统率检定）","以柔制刚，生擒审问（智谋检定）","武力",68)
            14 -> RandomEvent("evt_defection","降将来投","一位敌方大将兵败后孤身来投，愿效犬马之劳。","大礼相迎，委以重任（魅力检定，兵+600）","审慎接纳，先考察（统率+10）","婉拒，以礼送还（名望+30，失兵力）","魅力",62)
            15 -> RandomEvent("evt_plague2","瘟疫蔓延","军营内爆发疫症，士卒死伤过半，军心涣散。","重金请神医，全力救治（花400金，兵力保住）","隔离营区，壮士断腕（损兵400，保主力）","分兵撤退，保存实力（统率检定）","统率",60)
            16 -> RandomEvent("evt_scholar","名士拜访","一位天下闻名的名士登门拜访，愿为你出谋划策。","倒履相迎，以师礼待之（智谋+20，政治+10）","相谈甚欢，招入幕府（政治检定，+谋士）","虚以委蛇，套取情报（智谋检定）","智谋",70)
            17 -> RandomEvent("evt_border_war","边境摩擦","与邻近势力在边境发生冲突，需你定夺是战是和。","主动出击，趁势夺城（统率检定）","派使议和，以利诱之（政治检定）","按兵不动，坐收渔利（观望）","统率",65)
            18 -> RandomEvent("evt_treasure","发现密窖","整修府邸时，工匠在地下挖出一批前朝密藏的金银财宝。","秘密独吞（金+1500，风险）","上报朝廷，获赏赐（名望+60，金+300）","分给部下，收买人心（魅力+20，金-200）","政治",50)
            19 -> RandomEvent("evt_spy_ring","发现间谍网","你的情报官发现敌方在你军中布下了一张间谍网络。","一网打尽，清除内鬼（统率+15，安全）","将计就计，传递假情报（智谋检定）","秘而不发，钓大鱼（智谋+20，风险）","智谋",68)
            20 -> RandomEvent("evt_heir_talent","子嗣显才","你的孩子在一次演武中展露出惊人的天赋，引发各方关注。","重金延请名师悉心培养（花200金，子嗣才能+30）","亲自督导文武兼修（统率+5，子嗣成长）","低调处理，以防招嫉（政治+10）","魅力",55)
            21 -> RandomEvent("evt_stratagem","奇谋献策","你的谋士献上一条奇计，可一举逆转战局，但风险极大。","全力支持，孤注一掷（智谋检定，成功扭转战局）","稍加修改，稳妥推进（统率+15）","婉言谢绝，依正道行事（名望+20）","智谋",72)
            22 -> RandomEvent("evt_marriage_alliance","联姻邀请","一方诸侯遣使来访，请求与你联姻结盟，共抗强敌。","慨然应允，结成同盟（魅力+20，外交+1）","提出条件，漫天要价（政治检定）","婉言拒绝，保持独立（名望+15）","政治",60)
            23 -> RandomEvent("evt_uprising","境内民变","治下一县爆发小规模民变，乱民攻占县衙。","亲率精兵平叛（武力+统率检定）","派遣说客，招抚安民（政治检定）","拖延观望，待其自灭（风险极高）","统率",58)
            24 -> RandomEvent("evt_weapon_master","铸造大师","一位名闻天下的铸剑大师愿为你锻造一把绝世神兵。","倾囊相助（花500金，武力+25）","以义气打动，半价定制（魅力检定，武力+15）","婉拒神兵，赠以厚礼（名望+20）","魅力",65)
            25 -> RandomEvent("evt_eclipse","天象示警","日食当空，朝野震动，人心惶惶，传言天命将移。","祭天安民，稳定军心（政治+魅力检定）","借天象发难，趁机扩张（统率检定，声誉风险）","冷静分析，不为所动（智谋+15）","政治",60)
            26 -> RandomEvent("evt_old_friend","故友重逢","一位少年时的挚友出现在你面前，如今已是敌营大将。","以旧情相叙，劝其来投（魅力检定）","公事公办，不念旧情（名望+10，关系断裂）","暗中通款，互通情报（智谋检定，风险）","魅力",68)
            27 -> RandomEvent("evt_granary_fire","粮仓大火","军中粮仓突发大火，半年军粮付之一炬，军心大乱。","全力救火，组织补给（统率检定）","严查纵火，揪出内鬼（武力+智谋）","紧急征粮，以钱补缺（花600金）","统率",62)
            28 -> RandomEvent("evt_rogue_general","骄将难制","你麾下一员猛将居功自傲，目无军纪，诸将敢怒不敢言。","雷霆处置，杀鸡儆猴（统率+25，失此将）","晓以大义，收其心服（魅力检定）","借刀杀人，借敌手除之（智谋检定，风险）","统率",70)
            29 -> RandomEvent("evt_secret_edict","密诏到手","你截获了天子的一份密诏，内容足以颠覆某大诸侯。","秘密利用，要挟诸侯（政治检定，高风险高收益）","上交天子，获封赏（名望+80，金+500）","焚毁密诏，明哲保身（安全，声望+30）","政治",70)
            30 -> RandomEvent("evt_cavalry_gift","名马相赠","西凉商人带来一匹千里马，此马神骏，可日行千里。","重金购入（花400金，武力+10，机动性+20）","以计骗取（智谋检定，武力+10）","婉拒，以礼相待（名望+15）","魅力",55)
            31 -> RandomEvent("evt_supply_ambush","辎重遇袭","押送粮草的队伍在山道中遭到伏击，情况危急！","亲率骑兵驰援（武力+统率检定）","放弃粮草，保全人员（损金300，保兵力）","智取包围，以逸待劳（智谋检定）","武力",65)
            32 -> RandomEvent("evt_fortune_teller","奇人卜算","一位半仙之体的卜算奇人自称能预知你的命运走向。","虚心请教，广纳异见（智谋+10，情报一则）","斥其为惑众妖言（名望+5，错失机缘）","以重金相酬，请其随军（每年提供一次预警）","魅力",50)
            33 -> RandomEvent("evt_night_banquet","夜宴鸿门","你赴一场疑云密布的夜宴，宾主间暗流涌动，刀光潜伏。","主动探明虚实（智谋检定）","带足护卫，以势压人（统率+武力）","称病推辞，避而不往（安全，失机遇）","智谋",65)
            34 -> RandomEvent("evt_river_battle","水战考验","大军须渡过一条水流湍急的大河，敌军水师已在对岸列阵。","强攻渡河（统率检定，损兵）","迂回寻找渡口（智谋检定，绕行）","以火攻扰乱敌军水师（智谋+统率）","统率",68)
            35 -> RandomEvent("evt_spy_caught","我方细作被擒","我方潜伏在敌营的心腹细作被捕，对方以此要挟。","断然拒绝，弃子保局（智谋+名望）","以重金赎回（花500金，情报线保全）","将计就计，输送假情报（智谋检定）","智谋",70)
            36 -> RandomEvent("evt_young_hero","少年奇才","一名十五六岁的少年孤身来投，据说自幼熟读兵书。","破格擢用，委以重任（赌一把，可能+大将）","严格历练，从基层做起（稳健成长）","婉拒，赐以盘缠（失一潜在大将）","魅力",58)
            37 -> RandomEvent("evt_coalition","诸侯会盟","各路诸侯相约会盟，共商天下大势，你受邀出席。","慷慨陈词，争取盟主之位（魅力检定）","低调出席，暗中联络各方（政治检定）","借故推辞，独善其身（名望稳定）","魅力",72)
            38 -> RandomEvent("evt_drought_relief","以工代赈","旱灾之年，你推行以工代赈，令灾民修缮城防道路。","全力推进（花300金，城防+道义+）","半途而废（浪费100金）","借此大力征发劳役（声望-30，得城防）","政治",62)
            39 -> RandomEvent("evt_defector_spy","降将是奸细","你收容的一名降将被证实是敌方派来的卧底。","当众处斩，以儆效尤（名望稳定，威严+）","秘密控制，利用其传递假情报（智谋检定）","以大局为重，秘密驱逐（外交+10）","智谋",65)
            } // end when
        } // end else
        _gameState.value = GameState.EventChoice(ev) { opIndex ->
            handleEventOutcome(ev, opIndex)
        }
    }

    private fun handleEventOutcome(ev: RandomEvent, option: Int) {
        val logPrefix = "公元${currentYear.value}年（${age.value}岁）- [${ev.title}]: "
        var logSuffix = ""

        if (ev.id.startsWith("battle_")) {
            val battleLog = applySanguoBattleOutcome(ev, option)
            addLog(logPrefix + battleLog)
            _gameState.value = GameState.Playing
            saveCurrentGameToDb()
            return
        }

        when (ev.id) {
            "evt_bandit" -> {
                if (option == 1) { 
                    val skill = martial.value + conscripts.value/200 + (if (talents.value.contains("万夫莫敌")) 20 else 0)
                    if (skill + Random.nextInt(35) >= ev.threshold) {
                        reputation.value += 70
                        gold.value += 180
                        conscripts.value = (conscripts.value - 100).coerceAtLeast(100)
                        logSuffix = "你横刀纵马长声叱喝，指挥义勇将士三合冲锋杀散大贼，缴纳贼首佩刀与囤金钱货180两，雄名远扬！"
                    } else {
                        health.value = (health.value - 25).coerceAtLeast(1)
                        conscripts.value = (conscripts.value - 400).coerceAtLeast(100)
                        logSuffix = "寇贼林深树茂有诡计合围。你抵挡不及重负刀伤，将士散失近半，丢盔弃甲，狼狈出林。"
                    }
                } else if (option == 2) { 
                    val skill = intelligence.value + reputation.value/10 + (if (talents.value.contains("神机妙算")) 20 else 0)
                    if (skill + Random.nextInt(35) >= ev.threshold) {
                        reputation.value += 80
                        conscripts.value += 600
                        logSuffix = "你白衣纶巾面不改色，论透中原大势与受诏王师荣华，当场说哭叛首。五百山林大汉掷刀便拜降顺归部！"
                    } else {
                        health.value = (health.value - 20).coerceAtLeast(1)
                        gold.value = (gold.value - 150).coerceAtLeast(0)
                        logSuffix = "贼匪根本听不懂大文墨字，狂怒大呼‘竖儒受死！’合围抢光了你身旁的供车行李，大哭遁走。"
                    }
                } else {
                    gold.value = (gold.value - 150).coerceAtLeast(0)
                    logSuffix = "你长太息于中原草莽涂炭，扔下百两求得开路。虽失资财但全军平安走出生天。"
                }
            }
            "evt_hermit" -> {
                if (option == 1) {
                    health.value = 100
                    reputation.value += 40
                    logSuffix = "你盘坐在仙翁座前服下一粒金丹，霎时浑身燥热，精气如波涛重整，久久宿战风湿恶伤一律荡然全消！"
                } else if (option == 2) {
                    command.value += 15
                    training.value = (training.value + 15).coerceAtMost(100)
                    logSuffix = "高人赐你大汉武侯手稿阵书。你回庄后彻夜点校奇门遁甲。不仅统兵大悟，手下将士列阵行伍一日千里！"
                } else {
                    politics.value += 15
                    charisma.value += 15
                    logSuffix = "你抚膝大叹不贪长生、但全社稷。高人长叹：‘真乃人杰也！’随赠玉璧，你名播诸侯朝廷赏识。"
                }
            }
            "evt_famine" -> {
                if (option == 1) {
                    gold.value = (gold.value - 400).coerceAtLeast(0)
                    reputation.value += 200
                    politics.value += 15
                    logSuffix = "你自负千金，散尽府中底库搭米厂。救活数万孤儿寡母。满城高颂大贤之名，你美德传天下。"
                } else if (option == 2) {
                    conscripts.value += 1200
                    infantry.value += 600
                    archers.value += 600
                    reputation.value = (reputation.value - 40).coerceAtLeast(0)
                    logSuffix = "你强抽郡中流民中青壮入部，严查刺字，扩充兵车！你统部扩建，但因大动肝火而流失了百里人心。"
                } else {
                    politics.value += 10
                    logSuffix = "你认为大浪难收，紧阖都门静气。灾劫之下城外白骨皑皑。你虽存自保但胸中少了一份悲心。"
                }
            }
            "evt_challenge" -> {
                if (option == 1) {
                    val skill = martial.value + (if (talents.value.contains("万夫莫敌")) 20 else 0)
                    if (skill + Random.nextInt(40) >= ev.threshold) {
                        martial.value += 5
                        reputation.value += 120
                        logSuffix = "你冷笑跃起拔剑激撞。数回连刺将其手中金戟挑飞！席间豪杰太守惊呼喝彩。你剑术神威风震四方！"
                    } else {
                        health.value = (health.value - 20).coerceAtLeast(1)
                        reputation.value = (reputation.value - 40).coerceAtLeast(0)
                        logSuffix = "敌将乃是天生根骨奇拔之武夫。你抵挡数剑力竭被划烂领袍、削掉发笄。当场出尽洋相声名下落。"
                    }
                } else if (option == 2) {
                    val skill = politics.value + charisma.value
                    if (skill + Random.nextInt(40) >= 80) {
                        charisma.value += 10
                        reputation.value += 80
                        logSuffix = "你长立抚瑟引吭，纵论三国诸侯在谈笑间灰飞烟灭。敌将汗颜面赤扔剑伏法，惊艳全桌大儒名帅。"
                    } else {
                        reputation.value = (reputation.value - 15).coerceAtLeast(0)
                        logSuffix = "你吟作大意口风未调，被烈酒呛得面红大咳。敌军哄堂嘲笑你装点斯文，徒惹话柄。"
                    }
                } else {
                    reputation.value = (reputation.value - 10).coerceAtLeast(0)
                    politics.value += 8
                    logSuffix = "你敛眉作揖退出席列。冷脸不犯风浪，虽稍被议论有失大风气，但心智更发隐蔽坚韧。"
                }
            }
            "evt_investment" -> {
                if (option == 1) {
                    if (gold.value >= 500) {
                        gold.value -= 500
                        val lottery = Random.nextInt(100)
                        if (lottery < 45) {
                            gold.value += 1600
                            reputation.value += 50
                            logSuffix = "那巨商信义卓伦！运售胡地宝骏一举通关，大分利润，派管家密车护送黄金1600两入你府庄！"
                        } else if (lottery < 80) {
                            inventory.value["神雕绝影马"] = (inventory.value["神雕绝影马"] ?: 0) + 1
                            martial.value += 10
                            logSuffix = "巨商重诺千里运回。将一匹追风逐雷、日行千里的宝骏“神雕绝影马”密纳你厩中。你武力天限暴涨！"
                        } else {
                            logSuffix = "坏账不佳！那一批宝骏犯了大汗违封，半途半关被天兵统将抄充扣押，你的投金全数付之东流。"
                        }
                    } else {
                        logSuffix = "你想拿下这商机，但手头连五百金元通融都没有。胡商满心失望退出，空留悔意。"
                    }
                } else if (option == 2) {
                    gold.value += 300
                    reputation.value += 30
                    logSuffix = "你背地告发缉拿其走私，分得太守悬红府库银钱300两。但在江湖游侠中传出无信义之讥。"
                } else {
                    politics.value += 10
                    logSuffix = "坚决严词拒贿，气调纯澈。太守听闻后大赞你官风正派，在考功部上加重评语。"
                }
            }
            "evt_recruit" -> {
                if (option == 1) {
                    if (gold.value >= 300) {
                        gold.value -= 300
                        relationships.value["关羽"] = (relationships.value["关羽"] ?: 0) + 75
                        reputation.value += 80
                        logSuffix = "你命仆抬出上等杜康名酒，十里策马追上关云长，对月热酌！关公大笑相见恨晚，称你为一世至交！"
                    } else {
                        logSuffix = "浑身上下清白无两，连一瓮女儿红都付不起。无奈望着那赤色神骏呼啸掠过，虚怀未就。"
                    }
                } else if (option == 2) {
                    intelligence.value += 5
                    reputation.value += 40
                    logSuffix = "你高歌慷慨武圣赋。其辞雄劲，关云长打马过处不禁勒马驻足，大抚美须赞你在野旷才，名震关塞。"
                } else {
                    logSuffix = "冷眼叹其世间凡心，自顾整理兵甲鞍垫。红尘纷纷，本就不入眼。"
                }
            }
            "evt_patron" -> {
                if (option == 1) {
                    val targetFactionName = listOf("曹魏", "蜀汉", "东吴").random()
                    currentFaction.value = targetFactionName
                    currentJob.value = "偏将军"
                    conscripts.value += 500
                    reputation.value += 60
                    logSuffix = "你欣然叩领节印，率手下部曲星夜兼程赶往【$targetFactionName】都衙！即封“偏将军”，赐下亲兵五百！"
                } else {
                    politics.value += 12
                    reputation.value += 50
                    logSuffix = "你婉转上书谢大君美意，意重隐忍。文坛大佬听闻绝尘无骨，惊呼真傲雪岁寒名士也，德誉暴增。"
                }
            }
            else -> { // Plague
                if (option == 1) {
                    health.value = (health.value - 10).coerceAtLeast(1)
                    reputation.value += 150
                    politics.value += 8
                    logSuffix = "你亲登大棚看望流浪孤孩，耗干存药。引救无数人命，你因此染发轻疾但是海内大圣德！"
                } else if (option == 2) {
                    if (gold.value >= 150) {
                        gold.value -= 150
                        health.value = 100
                        logSuffix = "你重购熏蒸奇香，深宅不犯兵水。气血经过一岁养息全然满盈，元神饱满！"
                    } else {
                        logSuffix = "穷困抓不得重药，只得以井水硬挨重症。万幸气魄过人，一岁间虽无大补但安度危情。"
                    }
                } else {
                    gold.value += 1200
                    reputation.value = (reputation.value - 150).coerceAtLeast(0)
                    politics.value = (politics.value - 12).coerceAtLeast(0)
                    logSuffix = "你暗地与大户合流高垄断中药并哄抬百倍售！赚满1200两金，但引百万生民唾骂，指为‘吸血硕鼠’。"
                }
            }
        }

        addLog(logPrefix + logSuffix)
        _gameState.value = GameState.Playing
        saveCurrentGameToDb()
    }

    private fun rollStandardAnnualLog(): String {
        val standardEvents = listOf(
            "潜心于府中研习策论与修射之道，神清气爽。",
            "闻听北方边祸又起，公孙瓒在幽州练得‘白马义从’四处冲锋，天下一片躁动。",
            "结识了地方上一干乡绅子弟，高谈论阔。痛饮十余斛美酒。",
            "在当地县衙承办钱粮漕运，差事稳妥，博得乡邻交口重誉。",
            "勤加精研生平武艺。马背上枪出游龙，气力小有长进。",
            "偶在名山上结庐与过往的高人宿德论古今弈局，对乱世风云又多了一层悟解。"
        )
        val statsBoostType = Random.nextInt(5)
        var boostString = ""
        when (statsBoostType) {
            0 -> { martial.value += 2; boostString = "武力上升2点。" }
            1 -> { intelligence.value += 2; boostString = "智谋上升2点。" }
            2 -> { command.value += 2; boostString = "统率上升2点。" }
            3 -> { politics.value += 2; boostString = "政治上升2点。" }
            4 -> { charisma.value += 2; boostString = "魅力上升2点。" }
        }

        return "公元${currentYear.value}年（${age.value}岁）：${standardEvents.random()} $boostString"
    }

    // Spend Gold to Train Attributes
    fun trainSkill(skillType: String) {
        if (gold.value < 100) {
            addLog("由于缺少盘缠资财（训练需要100金），无法请大儒名师或置备器械操演。")
            return
        }
        gold.value -= 100
        val points = Random.nextInt(4, 9)
        var logText = ""
        when (skillType) {
            "武力" -> {
                martial.value += points
                logText = "你花费100金在演武场大熬筋骨、负重长枪狂挥，在教头切磋下武功大涨${points}点！"
            }
            "智谋" -> {
                intelligence.value += points
                logText = "你花费100金订购并研习名儒竹简，彻晓六韬三略。心智聪略大涨${points}点！"
            }
            "统率" -> {
                command.value += points
                logText = "你花费100金召集在野亲卫，在猎谷练习鱼鳞大阵行止，纪律统驭提升${points}点！"
            }
            "政治" -> {
                politics.value += points
                logText = "你买大礼大设雅座请老曹吏，剖析钱谷、户籍考评要理，行政能力上升${points}点！"
            }
            "魅力" -> {
                charisma.value += points
                logText = "你花费100金裁制名袍，手抚雅琴引高山曲，结交地方侠客，仪容魅力挺增${points}点！"
            }
        }
        addLog("公元${currentYear.value}年（${age.value}岁）：$logText")
        saveCurrentGameToDb()
    }

    // Travel to change Province (TRAVEL ACTION)
    fun travelTo(province: String) {
        if (gold.value < 50) {
            addLog("盘缠短促（迁移别郡需50金），难以动身上路。")
            return
        }
        gold.value -= 50
        hometown.value = province
        regenerateMarketPrices()
        addLog("公元${currentYear.value}年（${age.value}岁）：你整理简朴部曲箱行，正式迁居涉入神州重州郡 -- 【$province】！")
        saveCurrentGameToDb()
    }

    // Apply for Career Promotion / Serve Faction
    fun applyForJobDirectly(jobName: String, faction: String, requiredRep: Int, salaryBonus: Int) {
        if (reputation.value < requiredRep) {
            addLog("公元${currentYear.value}年（${age.value}岁）：你拜谒【$faction】游说晋升【$jobName】。然掾属评判你当世海内清誉不显（需要声望: $requiredRep），只得婉退。")
            return
        }

        currentFaction.value = faction
        currentJob.value = jobName
        reputation.value += 60
        addLog("公元${currentYear.value}年（${age.value}岁）：【晋升告发！】你获【$faction】重器提擢授印，正式代理主政【$jobName】要职！每年俸禄与名誉节节挺高。")
        saveCurrentGameToDb()
    }

    // Hold Banquet & Meet Historical Figures
    fun holdBanquetAndMeet() {
        if (gold.value < 200) {
            addLog("金水短紧（置备儒酒宴宾需200金），名士概不捧场。")
            return
        }
        gold.value -= 200

        // Find available figures in current timeline
        val unlocked = historicalFigures.filter { currentYear.value >= it.second }.map { it.first }
        val target = if (unlocked.isNotEmpty()) unlocked.random() else "野外隐世名高"

        // Initialize affection
        if (!relationships.value.containsKey(target)) {
            relationships.value[target] = Random.nextInt(15, 41)
        } else {
            relationships.value[target] = (relationships.value[target] ?: 0) + Random.nextInt(12, 22)
        }

        val updatedRel = relationships.value.toMutableMap()
        relationships.value = updatedRel

        val affection = relationships.value[target] ?: 20
        addLog("公元${currentYear.value}年（${age.value}岁）：你设一斛美酿大开庭座，请当世豪英【$target】。宴中畅谈治天下见，对方大敬！情谊亲密达:$affection")
        saveCurrentGameToDb()
    }

    // Give Custom Gift
    fun giveGiftTo(figure: String) {
        if (gold.value < 150) {
            addLog("囊中金流不足（礼物置备金需150两）。")
            return
        }
        gold.value -= 150
        val points = Random.nextInt(15, 30)
        relationships.value[figure] = (relationships.value[figure] ?: 10) + points

        val updatedRel = relationships.value.toMutableMap()
        relationships.value = updatedRel

        val affection = relationships.value[figure] ?: 20
        addLog("公元${currentYear.value}年（${age.value}岁）：你觅得一件西周古樽珍物赠予【$figure】。其摩裟抚弄狂喜不已，对你亲密度飙升至:$affection")
        saveCurrentGameToDb()
    }

    // Propose Marriage
    fun proposeMarriageTo(figure: String) {
        val affection = relationships.value[figure] ?: 0
        if (spouse.value != "无") {
            addLog("已有连理正妻红香在堂（暂不受夫室重娶），修心宜专。")
            return
        }
        if (affection < 80) {
            addLog("【$figure】含羞带怨避之：'阁下心意妾知，然情分未深、礼制未足，实难作嫁。'（需亲密度达80以上）")
            return
        }

        spouse.value = figure
        relationships.value[figure] = 100
        charisma.value += 15

        val updatedRel = relationships.value.toMutableMap()
        relationships.value = updatedRel

        addLog("公元${currentYear.value}年（${age.value}岁）：【洞房花烛大喜！】你与名震六荒的【$figure】六礼通书，喜办大婚！执手缔盟，结百年连理！夫人辅佐你魅力顿升15点！")
        saveCurrentGameToDb()
    }

    // Beget Heir (Dyna kids)
    fun requestBegetHeir() {
        if (spouse.value == "无") {
            addLog("行形茕独，仍未婚配，何来子孙传承？")
            return
        }
        if (gold.value < 300) {
            addLog("诞养龙凤后嗣需要优养，行宫缺少金银用费（需300金）。")
            return
        }
        gold.value -= 300
        
        val genderStr = if (Random.nextBoolean()) "男" else "女"
        val boyNames = listOf("承嗣", "茂", "昭", "显", "修", "植", "瞻", "统", "睿", "尚", "策", "冲", "统", "义")
        val girlNames = listOf("姬", "乔", "宓", "尚", "容", "昭", "甄", "蔡", "璇", "瑛", "珊", "华")
        val childGivenName = if (genderStr == "男") boyNames.random() else girlNames.random()
        
        val newChild = ChildInfo(
            name = childGivenName,
            age = 0,
            gender = genderStr,
            martial = Random.nextInt(35, 50),
            intelligence = Random.nextInt(35, 50),
            command = Random.nextInt(35, 50),
            politics = Random.nextInt(35, 50),
            charisma = Random.nextInt(35, 50)
        )
        
        val upgraded = childrenList.value.toMutableList()
        upgraded.add(newChild)
        childrenList.value = upgraded

        addLog("公元${currentYear.value}年（${age.value}岁）：【天祚大吉，添丁进口！】你与爱妇【${spouse.value}】琴瑟和谐，喜得一名骨血精雕之${genderStr}婴，起名：【$surname${childGivenName}】！子孙克昌，家族继昌！")
        saveCurrentGameToDb()
    }

    // Spend gold to educate child
    fun tutorChild(childName: String, excelType: String) {
        if (gold.value < 100) {
            addLog("修堂金费短绌，请不起大儒教头行家法（学费需要100金）。")
            return
        }
        
        val updatedList = childrenList.value.map { child ->
            if (child.name == childName) {
                gold.value -= 100
                if (excelType == "武臣") {
                    child.copy(
                        martial = child.martial + Random.nextInt(6, 11),
                        command = child.command + Random.nextInt(6, 11)
                    )
                } else {
                    child.copy(
                        intelligence = child.intelligence + Random.nextInt(6, 11),
                        politics = child.politics + Random.nextInt(6, 11),
                        charisma = child.charisma + Random.nextInt(5, 10)
                    )
                }
            } else {
                child
            }
        }
        
        childrenList.value = updatedList
        val typeLog = if (excelType == "武臣") "厉马挽弓，练习六艺骑战大术" else "诵读论语，讲授古魏太行名策"
        addLog("公元${currentYear.value}年（${age.value}岁）：你出资100金作为课资，亲自辅导后嗣【$surname${childName}】-- ${typeLog}。其天之聪颖顿启，其心大悟！")
        saveCurrentGameToDb()
    }

    // Replay with child's dynasty inherits
    fun inheritLegacyWithChild(childName: String) {
        val selectedChild = childrenList.value.firstOrNull { it.name == childName } ?: return
        
        val oldFatherName = "${surname.value}${name.value}"
        this.name.value = selectedChild.name
        age.value = 15
        
        // Inherits statistics
        martial.value = selectedChild.martial
        intelligence.value = selectedChild.intelligence
        command.value = selectedChild.command
        politics.value = selectedChild.politics
        charisma.value = selectedChild.charisma
        
        // Heritage resources
        gold.value = (gold.value * 0.6f).toInt()
        reputation.value = (reputation.value * 0.6f).toInt()
        
        // Set Mother as Spouse for timeline continuity
        val mother = spouse.value
        spouse.value = "无"
        childrenList.value = emptyList() // Next gen starts children again
        
        currentFaction.value = "在野游侠"
        currentJob.value = "江湖豪侠"
        
        lifeLogs.value = mutableListOf(
            "【二世承嗣 · 帝业复兴】公元${currentYear.value}年（15岁）：先父【${oldFatherName}】壮行仙去，其子嗣【$surname${selectedChild.name}】于哀痛中毅然束发承继宗族！继承家财白银${gold.value}两、祖名声望，执三尺利剑，高歌再征烟尘！"
        )
        
        _gameState.value = GameState.Playing
        saveCurrentGameToDb()
    }

    // Study from Friend
    fun apprecticeWith(figure: String) {
        val affection = relationships.value[figure] ?: 0
        if (affection < 75) {
            addLog("名英【$figure】婉托：'在下粗鄙见，与君交往日浅，且日后设大礼再说吧。'（需75亲密度）")
            return
        }
        relationships.value[figure] = (relationships.value[figure] ?: 75) - 30

        val updatedRel = relationships.value.toMutableMap()
        relationships.value = updatedRel

        val points = Random.nextInt(8, 16)
        if (Random.nextBoolean()) {
            intelligence.value += points
            addLog("公元${currentYear.value}年（${age.value}岁）：你于香堂叩首拜【$figure】学艺！其抚你顶指点王佐纵横、诡道突袭，你智谋狂增${points}点！")
        } else {
            politics.value += points
            addLog("公元${currentYear.value}年（${age.value}岁）：你毕恭毕敬拜请【$figure】教治行政大论。其引据经典、点透田赋，你政治通达飙增${points}点！")
        }
        saveCurrentGameToDb()
    }

    // Buy/Sell business transactions
    fun purchaseItem(itemName: String, unitPrice: Int) {
        if (gold.value < unitPrice) {
            addLog("在官市看中了【$itemName】奈何口袋窘困无法支款（需功金${unitPrice}两）！")
            return
        }
        gold.value -= unitPrice
        val currentQty = inventory.value[itemName] ?: 0
        inventory.value[itemName] = currentQty + 1

        val updatedInv = inventory.value.toMutableMap()
        inventory.value = updatedInv

        addLog("公元${currentYear.value}年（${age.value}岁）：置备珍物：在官肆划去${unitPrice}两白银，买入【$itemName】一个！")
        saveCurrentGameToDb()
    }

    fun sellItem(itemName: String, unitPrice: Int) {
        val currentQty = inventory.value[itemName] ?: 0
        if (currentQty <= 0) {
            return
        }
        gold.value += unitPrice
        if (currentQty == 1) {
            inventory.value.remove(itemName)
        } else {
            inventory.value[itemName] = currentQty - 1
        }

        val updatedInv = inventory.value.toMutableMap()
        inventory.value = updatedInv

        addLog("公元${currentYear.value}年（${age.value}岁）：在集市里痛快出手：售出【$itemName】一个，变卖折金${unitPrice}两入帐。")
        saveCurrentGameToDb()
    }

    // Refresh prices for trading when travelling or new year
    fun regenerateMarketPrices() {
        val updated = marketItems.value.map { item ->
            val factor = Random.nextDouble(0.40, 2.20)
            TradeItem(item.name, item.basePrice, (item.basePrice * factor).toInt().coerceAtLeast(10))
        }
        marketItems.value = updated
    }

    // MILITARY SERVICES
    fun hireTroops(type: String, quantity: Int, costGold: Int) {
        if (gold.value < costGold) {
            addLog("招募募兵金水不够，壮丁无米不愿出征（所需金银: $costGold 两）。")
            return
        }
        gold.value -= costGold
        conscripts.value += quantity
        when (type) {
            "步兵" -> infantry.value += quantity
            "骑兵" -> cavalry.value += quantity
            "弓兵" -> archers.value += quantity
        }
        addLog("公元${currentYear.value}年（${age.value}岁）：你斥资${costGold}金在乡野振臂高呼，召集中原豪勇充丁，得新兵【$quantity】部曲补充！")
        saveCurrentGameToDb()
    }

    fun doMilitaryTraining() {
        if (gold.value < 100) {
            addLog("缺少演练军资（大演兵需100金），三军士气委顿。")
            return
        }
        gold.value -= 100
        val trGain = Random.nextInt(8, 16)
        val morGain = Random.nextInt(8, 16)
        training.value = (training.value + trGain).coerceAtMost(100)
        morale.value = (morale.value + morGain).coerceAtMost(100)
        addLog("公元${currentYear.value}年（${age.value}岁）：你出资100两白银聚演全军，于旷野演练大纛指挥、负重枪刺。全军训练、士气均大涨！")
        saveCurrentGameToDb()
    }

    fun developProvinceAdmin(cityId: String) {
        if (gold.value < 150) {
            addLog("开发钱银短绌，民力无法募修水利（需150金）。")
            return
        }
        gold.value -= 150
        politics.value += 3
        unificationProgress.value = (unificationProgress.value + 2).coerceAtMost(99)
        addLog("公元${currentYear.value}年（${age.value}岁）：你拨付民银150两于【$cityId】兴修水渠、赈济流荒商肆。内政井然向上，你的政治与政绩上升。")
        saveCurrentGameToDb()
    }

    // Capture City and map update
    fun handleCityConquest(cityId: String) {
        val updated = cityOwners.value.toMutableMap()
        val oldOwner = updated[cityId] ?: "在野"
        updated[cityId] = if (currentFaction.value == "在野游侠") "江湖好汉盟" else currentFaction.value
        cityOwners.value = updated
        
        unificationProgress.value = (unificationProgress.value + 8).coerceAtMost(100)
        reputation.value += 150
        gold.value += 400
        
        addLog("【捷报传来！城陷池降！】你的雄军在猛士带领下，一举攻取【$cityId】（原属:$oldOwner）。宣告接掌其府印库银。得赏金400两，名望大震！")
        saveCurrentGameToDb()
    }

    // Static battle Campaigns Available list
    val campaignsAvailable: List<Campaign>
        get() = listOf(
            Campaign("camp_yellow", "黄巾剿灭战", 184, "张梁", "在野游侠", 35, "大贤良师大举揭竿。幽冀遭重、四野饿兵暴乱，急待先锋帅将登楼斩杀。"),
            Campaign("camp_dong", "逆臣董卓讨伐", 190, "华雄", "在野游侠", 55, "董温侯窃权焚烧帝京洛阳。诸侯于中原设下十二路大营歃血，需百战好汉前去破关。"),
            Campaign("camp_guandu", "官渡烧乌巢", 200, "颜良", "曹魏", 65, "曹操八千残甲对恃绍数十万！若能突刺守备、奇袭乌巢得手，战线立马颠覆！"),
            Campaign("camp_chibi", "赤壁东南火龙", 208, "张辽", "蜀汉", 75, "魏武拥雄兵百万索水路而下。联军诸葛、公瑾谋于江河，亟需敢死虎贲驾火船迎冲！"),
            Campaign("camp_fan", "攻樊城淹七军", 219, "庞德", "蜀汉", 85, "关圣御前调集霖雨大水围困樊襄二地，万军受困、亟需骑督偏将斩关夺旗大溃于禁。"),
            Campaign("camp_wuzhang", "祈星五丈原", 234, "司马懿", "蜀汉", 95, "祁山之上风大沙凄。亮病在旦夕，而仲达凭深壕死不冲突。需大统领背水破敌求得一胜！")
        ).filter { currentYear.value >= it.year - 2 }

    // Start historical battle campaigns
    fun enterCampaign(campaign: Campaign) {
        _gameState.value = GameState.BattleScene(campaign) { victory, detailsLog ->
            val updatedLog = lifeLogs.value.toMutableList()
            updatedLog.add("公元${currentYear.value}年（${age.value}岁）- [战役]: $detailsLog")
            lifeLogs.value = updatedLog

            if (victory) {
                reputation.value += 120
                gold.value += 300
                conscripts.value = (conscripts.value - Random.nextInt(1000, 2000)).coerceAtLeast(0)
            } else {
                health.value = (health.value - 20).coerceAtLeast(1)
                conscripts.value = (conscripts.value / 2)
            }

            _gameState.value = GameState.Playing
            saveCurrentGameToDb()
        }
    }

    // Dueling results
    fun startSingleDuel(figureName: String, opponentMartial: Int, onComplete: (Boolean) -> Unit) {
        _gameState.value = GameState.DuelScene(figureName, opponentMartial) { victory, duelLog ->
            val updatedLog = lifeLogs.value.toMutableList()
            updatedLog.add("公元${currentYear.value}年（${age.value}岁）- [阵前单挑]: $duelLog")
            lifeLogs.value = updatedLog

            if (victory) {
                martial.value += 4
                reputation.value += 80
                gold.value += 150
            } else {
                health.value = (health.value - 25).coerceAtLeast(1)
                reputation.value = (reputation.value - 10).coerceAtLeast(0)
            }

            _gameState.value = GameState.Playing
            saveCurrentGameToDb()
            onComplete(victory)
        }
    }

    // Trigger Protagonist death transition and finalize life
    fun triggerDeath(reason: String) {
        val calculatedAge = age.value
        var scoreBonus = calculatedAge * 10 + gold.value / 15 + reputation.value / 3 + (relationships.value.size * 50) + conscripts.value / 30
        
        if (talents.value.contains("枭雄之姿")) scoreBonus += 300
        if (spouse.value != "无") scoreBonus += 250

        val finalRank = when {
            scoreBonus > 1800 -> "SS"
            scoreBonus > 1200 -> "S"
            scoreBonus > 800 -> "A"
            scoreBonus > 500 -> "B"
            scoreBonus > 300 -> "C"
            else -> "D"
        }

        val finalTitle = getHistoricalConcludingTitle(finalRank)
        val finalLogCombined = lifeLogs.value.joinToString("|")
        val finalTalentsStr = talents.value.joinToString(",")

        val finalRecord = CharacterRecord(
            name = name.value,
            surname = surname.value,
            gender = gender.value,
            birthYear = currentYear.value - calculatedAge,
            deathYear = currentYear.value,
            deathAge = calculatedAge,
            hometown = hometown.value,
            origin = origin.value,
            maxTitle = finalTitle,
            spouse = spouse.value,
            faction = currentFaction.value,
            martial = martial.value,
            intelligence = intelligence.value,
            command = command.value,
            politics = politics.value,
            charisma = charisma.value,
            reputation = reputation.value,
            gold = gold.value,
            lifeLogs = finalLogCombined,
            talents = finalTalentsStr,
            endingRank = finalRank
        )

        // Does the user have heirs alive to succeed?
        val runHeirCheck = childrenList.value.isNotEmpty()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertRecord(finalRecord)
                // We DON'T delete active session immediately if they want to succeed with child!
                // But if they return to title, we delete it.
            }
            _gameState.value = GameState.GameOver(finalRecord, runHeirCheck)
        }
    }

    fun exitGameCleanToTitle() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteActiveSession()
            }
        }
        _gameState.value = GameState.Title
    }

    private fun getHistoricalConcludingTitle(rank: String): String {
        return when (rank) {
            "SS" -> if (currentFaction.value == "蜀汉") "汉室大司马武毅侯" else if (currentFaction.value == "曹魏") "魏国开府统国丞相高陵王" else "吞汉吞吴无冕开国神威皇帝"
            "S" -> if (martial.value > intelligence.value) "万军军中都督大车骑将军" else "开府仪同三司中书尚书令"
            "A" -> "刺史监军兵曹折冲都尉"
            "B" -> "当阳都会大太守"
            "C" -> "折冲偏印门下牙将校尉"
            else -> "落魄在野之士中原荒野林中枯骨"
        }
    }

    // Quick clear records
    fun clearPastRecords() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearAllRecords()
            }
        }
    }

    // Name generators
    private fun getRandomSurname(): String {
        val surnames = listOf(
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "曹", "曾", "诸葛", "司马", "夏侯",
            "关", "张", "刘", "袁", "董", "公孙", "马", "黄", "姜", "邓", "吕", "周", "陆", "甘"
        )
        return surnames.random()
    }

    private fun getRandomGivenName(gender: String): String {
        val maleNames = listOf("飞", "云", "德", "羽", "备", "操", "权", "亮", "统", "超", "平", "维", "逊", "懿", "昭", "盖", "忠", "辽", "霸", "蒙", "魏")
        val femaleNames = listOf("月英", "尚香", "蝉", "姬", "宓", "桥", "玉", "莲", "昭", "寿", "华", "容", "风")
        return if (gender == "男") maleNames.random() else femaleNames.random()
    }
}
