package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CharacterRecord
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameApp()
                }
            }
        }
    }
}

@Composable
fun GameApp(viewModel: GameViewModel = viewModel()) {
    val playState by viewModel.gameState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = playState) {
            is GameState.Title -> TitleScreen(viewModel)
            is GameState.BirthSetup -> BirthSetupScreen(viewModel)
            is GameState.Playing -> PlayingScreen(viewModel)
            is GameState.EventChoice -> EventChoiceDialog(state.event, state.onOptionSelected)
            is GameState.DuelScene -> DuelView(state.opponentName, state.opponentMartial, state.onDuelComplete)
            is GameState.BattleScene -> BattleView(state.campaign, viewModel, state.onBattleComplete)
            is GameState.GameOver -> GameOverScreen(viewModel, state.finalRecord, state.hasHeir)
        }
    }
}

// -------------------------------------------------------------
// SCREEN: TitleScreen (Sanguo Theme Title Layout)
// -------------------------------------------------------------
@Composable
fun TitleScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    var showRecordsHall by remember { mutableStateOf(false) }
    val activeSaveSession by viewModel.activeSession.collectAsState()

    // Title aesthetic Canvas Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
            .drawBehind {
                drawCircle(
                    color = Color(0xFFD4AF37).copy(alpha = 0.03f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF8B1A1A).copy(alpha = 0.04f),
                    style = Stroke(width = 6.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Ink Silhouette Banner
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFF1E1010).copy(alpha = 0.5f), RoundedCornerShape(55.dp))
                    .border(1.5.dp, Color(0xFFD32F2F), RoundedCornerShape(55.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "三国\n人生",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "—— 乱世宏图演义录 · 承嗣继承版 ——",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(35.dp))

            // Options Buttons Column
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.width(280.dp)
            ) {
                // Button 1: Start New Journey
                Button(
                    onClick = { viewModel.enterBirthSetup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_game_button")
                        .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "起兵征伐 (开辟新人生)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 2: Resume Saved Journey
                Button(
                    onClick = {
                        if (activeSaveSession != null) {
                            viewModel.loadSavedGame()
                            Toast.makeText(context, "往世人生魂归归位！", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "三生石中暂未寻到您的旧岁留墨。", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("resume_game_button")
                        .border(
                            1.dp,
                            if (activeSaveSession != null) Color(0xFFD4AF37).copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSaveSession != null) Color(0xFF242220) else Color(0xFF161514)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = activeSaveSession != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Resume",
                            tint = if (activeSaveSession != null) Color(0xFFD4AF37) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSaveSession != null) "红尘继续 (流转旧功业)" else "红尘孤客 (无旧归档)",
                            color = if (activeSaveSession != null) Color.White else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button 3: Hall of Fame Records
                Button(
                    onClick = { showRecordsHall = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("records_hall_button")
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF19191C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = "History", tint = Color(0xFFE5E0D8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "青史留凭 (三生回廊)",
                            color = Color(0xFFE5E0D8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(45.dp))

            // Game brief tips
            Text(
                text = "游玩提示: 战阵兵种八卦克鱼鳞、鱼鳞克鹤翼、鹤翼克八卦。\n成家后求取诞下子嗣，悉心栽培，卒岁后可继承二代功勋！",
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }

    if (showRecordsHall) {
        RecordsHallDialog(viewModel) { showRecordsHall = false }
    }
}

// -------------------------------------------------------------
// DIALOG: RecordsHallDialog (Playthrough Hall of Fame)
// -------------------------------------------------------------
@Composable
fun RecordsHallDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    val records by viewModel.allPastRecords.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("青史回廊 (列传堂)", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = {
                    viewModel.clearPastRecords()
                    Toast.makeText(context, "青史尘卷已被焚灰。", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Records", tint = Color.Gray)
                }
            }
        },
        text = {
            Box(modifier = Modifier.size(width = 320.dp, height = 400.dp)) {
                if (records.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "天地寂寥，尚无功业载档。\n去游戏中谱写您的家业荣辱吧！",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(records) { record ->
                            RecordItemCard(record)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("敛迹退出", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF1E1E22),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun RecordItemCard(record: CharacterRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.surname}${record.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            when(record.endingRank) {
                                "SS" -> Color(0xFFE5C158)
                                "S" -> Color(0xFFE5A93C)
                                "A" -> Color(0xFFC04B4B)
                                else -> Color(0xFF2E6F40)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(record.endingRank + " 等爵", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "最高称号: ${record.maxTitle}",
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${record.deathAge} 岁于 ${record.hometown} （主君: ${record.faction}）羽化。",
                fontSize = 12.sp,
                color = Color.Gray
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Gray.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("武:${record.martial}", fontSize = 10.sp, color = Color.White)
                Text("智:${record.intelligence}", fontSize = 10.sp, color = Color.White)
                Text("统:${record.command}", fontSize = 10.sp, color = Color.White)
                Text("政:${record.politics}", fontSize = 10.sp, color = Color.White)
                Text("余金:${record.gold}两", fontSize = 10.sp, color = Color(0xFFD4AF37))
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN: BirthSetupScreen (Character Creation Designer)
// -------------------------------------------------------------
@Composable
fun BirthSetupScreen(viewModel: GameViewModel) {
    val sur by viewModel.surname.collectAsState()
    val nm by viewModel.name.collectAsState()
    val gend by viewModel.gender.collectAsState()
    val home by viewModel.hometown.collectAsState()
    val orig by viewModel.origin.collectAsState()

    var martialPoints by remember { mutableStateOf(50) }
    var intelPoints by remember { mutableStateOf(50) }
    var commandPoints by remember { mutableStateOf(50) }
    var polPoints by remember { mutableStateOf(50) }
    var charismaPoints by remember { mutableStateOf(50) }

    var spareAllocUnits by remember { mutableStateOf(50) }

    var rollTalents by remember { mutableStateOf(viewModel.rollSetupTalents()) }
    val chosenTalents = remember { mutableStateListOf<String>() }

    // Scroll container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "投生乾坤 (乱世名器重塑)",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Panel 1: Direct Identity Detail Customization
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "名讳: $sur $nm",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { viewModel.randomizeIdentity() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reroll Name", tint = Color(0xFFD4AF37))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gender Switch Select Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("性别:  ", color = Color.Gray, fontSize = 14.sp)
                        listOf("男", "女").forEach { targetGender ->
                            Button(
                                onClick = {
                                    viewModel.gender.value = targetGender
                                    viewModel.randomizeIdentity()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gend == targetGender) Color(0xFF8B1A1A) else Color(0xFF2E2E34)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(34.dp)
                            ) {
                                Text(targetGender, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hometown Select Tag
                    Text("投生祖籍: ", color = Color.Gray, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("幽州", "冀州", "徐州", "荆州", "凉州").forEach { prov ->
                            FilterChip(
                                selected = home == prov,
                                onClick = { viewModel.hometown.value = prov },
                                label = { Text(prov) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF8B1A1A),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Class Origins Select Tag
                    Text("行伍出身: ", color = Color.Gray, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("寒门庶民", "商贾大富", "世家子弟").forEach { clss ->
                            ElevatedCard(
                                onClick = { viewModel.origin.value = clss },
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (orig == clss) Color(0xFF8B1A1A) else Color(0xFF2C2C30)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(clss, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Panel 2: Points Allocator Designer UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("天资五维属性点数分配", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text("余积分配点: $spareAllocUnits", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val attributes = listOf(
                        Triple("武力", martialPoints) { delta: Int -> martialPoints += delta },
                        Triple("智谋", intelPoints) { delta: Int -> intelPoints += delta },
                        Triple("统率", commandPoints) { delta: Int -> commandPoints += delta },
                        Triple("政治", polPoints) { delta: Int -> polPoints += delta },
                        Triple("魅力", charismaPoints) { delta: Int -> charismaPoints += delta }
                    )

                    attributes.forEach { (label, value, updater) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.width(50.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (value > 30) {
                                            updater(-5)
                                            spareAllocUnits += 5
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "$value",
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .width(40.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )

                                IconButton(
                                    onClick = {
                                        if (spareAllocUnits >= 5) {
                                            updater(5)
                                            spareAllocUnits -= 5
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Panel 3: Talents Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("宿慧天赋选择 (选择上限3个)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        IconButton(onClick = { rollTalents = viewModel.rollSetupTalents() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Roll Talents", tint = Color(0xFFD4AF37))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rollTalents.forEach { tal ->
                            val isSelected = chosenTalents.contains(tal)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFF381212) else Color(0xFF141416),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            chosenTalents.remove(tal)
                                        } else {
                                            if (chosenTalents.size < 3) chosenTalents.add(tal)
                                        }
                                    }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tal, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFFD4AF37))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Main launch validation button
            Button(
                onClick = {
                    viewModel.startNewGame(
                        allocatedMartial = martialPoints,
                        allocatedIntel = intelPoints,
                        allocatedCommand = commandPoints,
                        allocatedPol = polPoints,
                        allocatedCharisma = charismaPoints,
                        selectedTalents = chosenTalents.toList()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("掷定乾坤，投胎入世！", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN: PlayingScreen (Core Game Layout Framework with dynamic map)
// -------------------------------------------------------------
@Composable
fun PlayingScreen(viewModel: GameViewModel) {
    val currentSur by viewModel.surname.collectAsState()
    val currentN by viewModel.name.collectAsState()
    val characterAge by viewModel.age.collectAsState()
    val characterYear by viewModel.currentYear.collectAsState()
    val currentF by viewModel.currentFaction.collectAsState()
    val currentJ by viewModel.currentJob.collectAsState()
    val currentHP by viewModel.health.collectAsState()
    val currentM by viewModel.gold.collectAsState()
    val currentRep by viewModel.reputation.collectAsState()
    val curUnifProgress by viewModel.unificationProgress.collectAsState()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF19191C))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .statusBarsPadding()
            ) {
                // Header Row 1: Profile and Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF8B1A1A), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentSur.take(1),
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$currentSur$currentN", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                // Manual Save Anchor
                                IconButton(
                                    onClick = { 
                                        viewModel.saveCurrentGameToDb()
                                        Toast.makeText(context, "归档已存入三生石 (已存档)！", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Manual Save", tint = Color(0xFFD4AF37).copy(alpha = 0.8f), modifier = Modifier.size(15.dp))
                                }
                            }
                            Text("$currentF · $currentJ", fontSize = 12.sp, color = Color(0xFFD4AF37))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "公元 $characterYear 年",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$characterAge 岁",
                            color = Color(0xFFE5E0D8).copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Dashboard Bar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF141416), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙 $currentM 金", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF141416), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎖️ $currentRep 声望", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(Color(0xFF141416), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("HP:", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("$currentHP%", fontSize = 12.sp, color = if (currentHP < 35) Color(0xFFA62B2B) else Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            LinearProgressIndicator(
                                progress = { currentHP / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (currentHP < 35) Color(0xFFA62B2B) else Color(0xFF2E6F40),
                                trackColor = Color(0xFF2C2C30)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TAB Selectors
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFD4AF37),
                    indicator = { tabPositions ->
                        if (activeTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = Color(0xFFD4AF37)
                            )
                        }
                    }
                ) {
                    val titles = listOf("岁月年表", "修聚仕途", "贸易集市", "宗族家族", "金戈伐图")
                    titles.forEachIndexed { sIndex, sTitle ->
                        Tab(
                            selected = activeTab == sIndex,
                            onClick = { activeTab = sIndex },
                            text = {
                                Text(
                                    text = sTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF141416)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> ChronologyTab(viewModel)
                1 -> CareersAndTrainingTab(viewModel)
                2 -> TradingShopTab(viewModel)
                3 -> HeirsTab(viewModel)
                4 -> MapTerritoryTab(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: CHRONOLOGY / YEAR ADVANCE LOGS
// -------------------------------------------------------------
@Composable
fun ChronologyTab(viewModel: GameViewModel) {
    val logs by viewModel.lifeLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val scrollStateLogs = rememberScrollState()

                LaunchedEffect(logs.size) {
                    scrollStateLogs.animateScrollTo(scrollStateLogs.maxValue)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollStateLogs)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    logs.forEachIndexed { index, log ->
                        val isCurrentNewest = index == logs.lastIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrentNewest) Color(0xFF2E1212) else Color(0xFF141416),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (isCurrentNewest) Color(0xFFD4AF37) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = log,
                                color = if (isCurrentNewest) Color(0xFFFCF8F2) else Color(0xFFE5E0D8).copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = if (isCurrentNewest) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.advanceAge() },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Next Year", tint = Color(0xFFD4AF37), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "时光飞逝 (演进一载)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "流岁增 1 · 必将触发年度世事际遇选择", fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: CAREERS TREE & ATTR TRAINING STUDY
// -------------------------------------------------------------
@Composable
fun CareersAndTrainingTab(viewModel: GameViewModel) {
    val mValue by viewModel.martial.collectAsState()
    val iValue by viewModel.intelligence.collectAsState()
    val cValue by viewModel.command.collectAsState()
    val pValue by viewModel.politics.collectAsState()
    val chValue by viewModel.charisma.collectAsState()
    val gold by viewModel.gold.collectAsState()

    val currentFaction by viewModel.currentFaction.collectAsState()
    val currentJob by viewModel.currentJob.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Stats display card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "「乾坤五维功修」", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val stats = listOf(
                    "武力" to mValue,
                    "智谋" to iValue,
                    "统率" to cValue,
                    "政治" to pValue,
                    "魅力" to chValue
                )

                stats.forEach { (name, score) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, color = Color.LightGray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$score 点", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.trainSkill(name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1212)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("进研 (100金)", fontSize = 11.sp, color = Color(0xFFD4AF37))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Career Promotion Tree Scheme Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "「宗族仕途等爵之梯」", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("当前供职势力: $currentFaction · 当前阶级: $currentJob", color = Color.Gray, fontSize = 12.sp)

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.15f))

                // Selector for seeking patron split factions
                Text("投靠各路权雄诸侯:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("曹魏", "蜀汉", "东吴", "在野游侠").forEach { targetFaction ->
                        Button(
                            onClick = { 
                                if (targetFaction == "在野游侠") {
                                    viewModel.currentFaction.value = "在野游侠"
                                    viewModel.currentJob.value = "江湖豪侠"
                                } else {
                                    viewModel.applyForJobDirectly("偏将军", targetFaction, 100, 300)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentFaction == targetFaction) Color(0xFF8B1A1A) else Color(0xFF2C2C30)
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text(targetFaction, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Military Ranks Nodes Row Flow list
                Text("武职路线晋升梯度一览:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val militaryJobs = listOf(
                    Triple("牙将校尉", 80, 50),
                    Triple("偏将军", 150, 70),
                    Triple("都督牙将", 220, 80),
                    Triple("天下大将军", 450, 90)
                )

                militaryJobs.forEach { (jobTitle, reqRep, reqMart) ->
                    val isActive = currentJob == jobTitle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                0.5.dp,
                                if (isActive) Color(0xFFD4AF37) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFF381212) else Color(0xFF141416)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(jobTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("要求: 声望 $reqRep | 武力 $reqMart （年俸涨禄）", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.applyForJobDirectly(jobTitle, currentFaction, reqRep, 500) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("叩请", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Civil Ranks Nodes Row Flow list
                Text("文职路线晋升梯度一览:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val civilJobs = listOf(
                    Triple("主簿县丞", 80, 50),
                    Triple("郡首都尉", 150, 65),
                    Triple("郡太守", 220, 80),
                    Triple("汉中丞相", 450, 90)
                )

                civilJobs.forEach { (jobTitle, reqRep, reqPol) ->
                    val isActive = currentJob == jobTitle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                0.5.dp,
                                if (isActive) Color(0xFFD4AF37) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFF122C38) else Color(0xFF141416)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(jobTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("要求: 声望 $reqRep | 政治 $reqPol （主政城池内政加成）", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.applyForJobDirectly(jobTitle, currentFaction, reqRep, 500) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6080)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("叩请", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: TRADING COMMODITY SHOP MARKET
// -------------------------------------------------------------
@Composable
fun TradingShopTab(viewModel: GameViewModel) {
    val itemsAvailable by viewModel.marketItems.collectAsState()
    val gold by viewModel.gold.collectAsState()
    val inventoryMap by viewModel.inventory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "官设漕银重镇集市",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    fontSize = 15.sp
                )
                Text(text = "手头流动资金: $gold 两", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(itemsAvailable) { tradeItem ->
                val qtyVal = inventoryMap[tradeItem.name] ?: 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tradeItem.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("买卖指导价: ${tradeItem.currentPrice}金", color = Color(0xFFD4AF37), fontSize = 12.sp)
                            Text("拥有数: ${qtyVal}件", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Buy
                            Button(
                                onClick = { viewModel.purchaseItem(tradeItem.name, tradeItem.currentPrice) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1212)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("补购", fontSize = 12.sp, color = Color.White)
                            }
                            // Sell
                            Button(
                                onClick = { viewModel.sellItem(tradeItem.name, tradeItem.currentPrice) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F40)),
                                shape = RoundedCornerShape(6.dp),
                                enabled = qtyVal > 0
                            ) {
                                Text("出手", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: CONNECTIONS, MARRIAGE, FAMILY & HEIRS
// -------------------------------------------------------------
@Composable
fun HeirsTab(viewModel: GameViewModel) {
    val currentSpouse by viewModel.spouse.collectAsState()
    val relationMap by viewModel.relationships.collectAsState()
    val children by viewModel.childrenList.collectAsState()
    val gold by viewModel.gold.collectAsState()

    var showRelationsTab by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Switch between Relations or Heirs children
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showRelationsTab = true },
                colors = ButtonDefaults.buttonColors(containerColor = if (showRelationsTab) Color(0xFF8B1A1A) else Color(0xFF2C2C30)),
                modifier = Modifier.weight(1f)
            ) {
                Text("名贤游仙拜请", fontSize = 13.sp)
            }
            Button(
                onClick = { showRelationsTab = false },
                colors = ButtonDefaults.buttonColors(containerColor = if (!showRelationsTab) Color(0xFF8B1A1A) else Color(0xFF2C2C30)),
                modifier = Modifier.weight(1f)
            ) {
                Text("宗祠家族后裔", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (showRelationsTab) {
            // RELATIONS PANEL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "「宴席大邀与名士拜谒」", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("与天下名豪增进羁绊。亲密达80以上可纳聘为配偶，亲密达75可求师。", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.holdBanquetAndMeet() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1212))
                    ) {
                        Text("举办百官宴席 结识新人 (花费200金)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Listed companions
            if (relationMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("当前尚无亲厚之契合名贤。快设酒宴拜谒吧！", color = Color.Gray)
                }
            } else {
                relationMap.forEach { (name, aff) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Silhouette styled Canvas vector icon for general avatar
                                    Canvas(modifier = Modifier.size(36.dp).padding(4.dp)) {
                                        drawCircle(color = Color(0xFFD4AF37), style = Stroke(width = 1.dp.toPx()))
                                        drawLine(Color(0xFF8B1A1A), Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = 3f)
                                        drawCircle(Color(0xFF8B1A1A), radius = 6f, center = Offset(size.width * 0.5f, size.height * 0.35f))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                }
                                Text("修好契约: $aff 点", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.giveGiftTo(name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C30)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("赠礼古金 (150金)", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.apprecticeWith(name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C30)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp),
                                    enabled = aff >= 75
                                ) {
                                    Text("师授武经", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.proposeMarriageTo(name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp),
                                    enabled = aff >= 80 && currentSpouse == "无"
                                ) {
                                    Text("提亲迎娶", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // HEIRS & FAMILY PANEL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "「宗亲正妻与诞衍育孤」", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("夫人: $currentSpouse", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.requestBegetHeir() },
                        enabled = currentSpouse != "无",
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                    ) {
                        Text("求签诞子 喜事盈门 (300金)", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (currentSpouse == "无") {
                        Text(
                            "提示: 您尚未婚配正妻。请先去「名贤拜请」给倾慕美人连环赠礼升温亲密，八抬大轿抬入内府以添嗣！",
                            color = Color(0xFFC04B4B),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("膝下后裔一览 (悉心培养):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (children.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("中原香火凋零，暂无子嗣。快娶妻得子承继宗图！", color = Color.Gray)
                }
            } else {
                children.forEach { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (child.gender == "男") Color(0xFF1A384F) else Color(0xFF4F1A30),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(child.gender, fontSize = 11.sp, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${viewModel.surname.value}${child.name}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text("${child.age} 岁", color = Color.LightGray, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "属性: ⚔️武:${child.martial} | 🧠智:${child.intelligence} | 🎖️统:${child.command} | 📜政:${child.politics}",
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.tutorChild(child.name, "武臣") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1212)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("讲武传策 (100金)", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.tutorChild(child.name, "文臣") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12345A)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("诗书论政 (100金)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: MAP & TERRITORY MILITARY SERVICES
// -------------------------------------------------------------
@Composable
fun MapTerritoryTab(viewModel: GameViewModel) {
    val cityOwners by viewModel.cityOwners.collectAsState()
    val conscriptsVal by viewModel.conscripts.collectAsState()
    val infVal by viewModel.infantry.collectAsState()
    val cavVal by viewModel.cavalry.collectAsState()
    val arcVal by viewModel.archers.collectAsState()
    val moraleVal by viewModel.morale.collectAsState()
    val trainingVal by viewModel.training.collectAsState()
    val goldVal by viewModel.gold.collectAsState()

    var selectedCityForAction by remember { mutableStateOf<CityInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Upper stats bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "「天下总掌兵戈大营」",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("总兵员数: $conscriptsVal ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("步兵:$infVal | 骑兵:$cavVal | 弓兵:$arcVal", color = Color.Gray, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("训练度: $trainingVal% | 士气: $moraleVal%", color = Color(0xFFD4AF37), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.doMilitaryTraining() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                            modifier = Modifier.height(26.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("大集训 (100金)", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interative Canvas Territory Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F11))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val mapW = constraints.maxWidth.toFloat()
                val mapH = constraints.maxHeight.toFloat()

                // Draw map paths and rivers
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Back roads paths connections representing supply lines
                    val connections = listOf(
                        "tianshui" to "changan", "changan" to "luoyang", "luoyang" to "yecheng",
                        "yecheng" to "beiping", "xiangyang" to "luoyang", "xiangyang" to "xuchang",
                        "xuchang" to "xiapi", "xiapi" to "jianye", "chengdu" to "xiangyang"
                    )

                    connections.forEach { (c1, c2) ->
                        val node1 = viewModel.citiesList.firstOrNull { it.id == c1 }
                        val node2 = viewModel.citiesList.firstOrNull { it.id == c2 }
                        if (node1 != null && node2 != null) {
                            drawLine(
                                color = Color(0xFFD4AF37).copy(alpha = 0.15f),
                                start = Offset(node1.x * mapW, node1.y * mapH),
                                end = Offset(node2.x * mapW, node2.y * mapH),
                                strokeWidth = 3f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }

                // Render city buttons on coordinates
                viewModel.citiesList.forEach { city ->
                    val ownFaction = cityOwners[city.id] ?: "在野"
                    val factionColor = when(ownFaction) {
                        "曹魏" -> Color(0xFFA62B2B)
                        "蜀汉" -> Color(0xFFD4AF37)
                        "东吴" -> Color(0xFF2B8BA6)
                        "袁绍" -> Color(0xFF702BA6)
                        "董卓" -> Color(0xFF3F3F42)
                        "黄巾贼" -> Color(0xFFB28F10)
                        else -> Color(0xFF2B863C)
                    }

                    // Scaled positions
                    val density = LocalDensity.current
                    val xPosDp = with(density) { (city.x * mapW).toDp() }
                    val yPosDp = with(density) { (city.y * mapH).toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xPosDp - 22.dp, y = yPosDp - 22.dp)
                            .size(44.dp)
                            .background(factionColor, RoundedCornerShape(22.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                            .clickable { selectedCityForAction = city },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = city.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Instructions Overlay Text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        "「天下十三州沙盘」· 点击上方闪烁城池即可派遣督征、征兵、攻取",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions panel for selected city
        val tgtCity = selectedCityForAction
        if (tgtCity != null) {
            val ownFaction = cityOwners[tgtCity.id] ?: "在野"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("城池: 【${tgtCity.name}】", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("执掌主公: $ownFaction | 基础漕漕贡税: ${tgtCity.baseIncome}金", color = Color.LightGray, fontSize = 11.sp)
                        }
                        IconButton(onClick = { selectedCityForAction = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(tgtCity.description, color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.15f))

                    // Buttons Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Action 1: Recruit troops (only allowed if own or home)
                        Button(
                            onClick = { viewModel.hireTroops("步兵", 400, 100) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F42)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("招募步勇\n(100金)", fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                        Button(
                            onClick = { viewModel.hireTroops("骑兵", 300, 150) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F42)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("征猎精骑\n(150金)", fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Action 2: Develop (if owned)
                        Button(
                            onClick = { viewModel.developProvinceAdmin(tgtCity.name) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3C40)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("督察开发 (150金)", fontSize = 11.sp)
                        }

                        // Action 3: Launch campaign Battle crusade
                        val canCrusade = ownFaction != viewModel.currentFaction.value
                        Button(
                            onClick = {
                                val camp = Campaign(
                                    id = "siege_" + tgtCity.id,
                                    name = "收官取城 -- 【${tgtCity.name}】夺玺役",
                                    year = viewModel.currentYear.value,
                                    enemyName = ownFaction,
                                    factionRequired = "在野游侠",
                                    enemyForceStrength = 55,
                                    description = "统领部曲大举叩关夺下【${tgtCity.name}】府印，宣布接管其漕库土地！"
                                )
                                viewModel.selectedCampaignTarget.value = tgtCity
                                viewModel.enterCampaign(camp)
                                selectedCityForAction = null
                            },
                            enabled = canCrusade,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("攻城掠地 (鸣鼓发兵)", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Historical Campaigns selector list scroll
        Text("可投效大略会战 (历代演籍):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        viewModel.campaignsAvailable.forEach { camp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(camp.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("限岁: ${camp.year} 年", color = Color(0xFFD4AF37), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(camp.description, color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.enterCampaign(camp) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("承运挂帅 奔赴大会战", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UI COMPONENT: EventChoiceDialog (CYOA Events Dialog Popup)
// -------------------------------------------------------------
@Composable
fun EventChoiceDialog(
    event: RandomEvent,
    onOptionSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force choose to resolve */ },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Event", tint = Color(0xFFD4AF37), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(event.description, color = Color(0xFFE5E0D8), fontSize = 14.sp, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Options list list
                val options = listOf(
                    event.option1 to 1,
                    event.option2 to 2,
                    event.option3 to 3
                )

                options.forEach { (optionText, optIndex) ->
                    Button(
                        onClick = { onOptionSelected(optIndex) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C30)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(11.dp)
                    ) {
                        Text(
                            text = optionText,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF1E1E22),
        shape = RoundedCornerShape(16.dp)
    )
}

// -------------------------------------------------------------
// VIEW SCREEN: DuelView (Dueling Animation Arena Scene)
// -------------------------------------------------------------
@Composable
fun DuelView(
    opponentName: String,
    opponentMartial: Int,
    onDuelComplete: (Boolean, String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var duelProgressLog by remember { mutableStateOf(listOf<String>()) }
    var userRemLifeHP by remember { mutableStateOf(100) }
    var oppRemLifeHP by remember { mutableStateOf(100) }
    var isDuelOngoing by remember { mutableStateOf(true) }
    var gameFinalResult by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val welcomeMsg = "两马相交，兵戈闪烁！你策马挺身与敌军枭将【$opponentName】（武力:$opponentMartial）对决盘马！"
        duelProgressLog = listOf(welcomeMsg)

        var userM = 65 // Assume average base
        var isVictor = false

        delay(1300)

        // Run clash loops
        var turn = 1
        while (userRemLifeHP > 0 && oppRemLifeHP > 0 && turn <= 4) {
            val userClashHit = Random.nextInt(15, 30)
            val oppClashHit = Random.nextInt(15, 30)

            val turnText = when(turn) {
                1 -> "第一回相遇，两戈骤击，马蹄震雪。你在飞身闪刀间"
                2 -> "第二回盘绕，敌将刀法刁钻斜劈中盘。你咬牙翻身侧躲"
                3 -> "第三回冲掠，你虚晃长枪一记金鸡点头！"
                else -> "终局狂攻，两方皆弃马步下博杀互劈！"
            }

            if (Random.nextBoolean()) {
                oppRemLifeHP = (oppRemLifeHP - userClashHit).coerceAtLeast(0)
                duelProgressLog = duelProgressLog + "$turnText：刺入破绽重创敌将，其闪手见红，体力丧失${userClashHit}%！"
            } else {
                userRemLifeHP = (userRemLifeHP - oppClashHit).coerceAtLeast(0)
                duelProgressLog = duelProgressLog + "$turnText：不察中了敌将回马流星锤，险些坠马，周身剧烈气血飞损${oppClashHit}%！"
            }

            delay(1200)
            turn++
        }

        isVictor = oppRemLifeHP <= userRemLifeHP
        isDuelOngoing = false
        gameFinalResult = isVictor

        val concludingDesc = if (isVictor) {
            "【悍将大胜！】你拍马狂啸，刺落【$opponentName】钢盔，敌军惨败狼狈遁逃，纳金受功！"
        } else {
            "【单挑折重！】你不敌敌将长枪被挑落马下，幸得部曲盾刀死战护驾退回，周身负伤惨重。"
        }
        duelProgressLog = duelProgressLog + concludingDesc
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HP Battle gauges
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(
                    "—— 猛将阵前搦战对杀演义 ——",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Left: Player
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("自驾部勇 HP: $userRemLifeHP%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LinearProgressIndicator(
                            progress = { userRemLifeHP / 100f },
                            modifier = Modifier.width(120.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2E6F40),
                            trackColor = Color(0xFF2C2C30)
                        )
                    }

                    Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF8B1A1A))

                    // Right: Opponent
                    Column(horizontalAlignment = Alignment.End) {
                        Text("【$opponentName】 HP: $oppRemLifeHP%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LinearProgressIndicator(
                            progress = { oppRemLifeHP / 100f },
                            modifier = Modifier.width(120.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFA62B2B),
                            trackColor = Color(0xFF2C2C30)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scroll logs showcase
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val scLog = rememberScrollState()
                    LaunchedEffect(duelProgressLog.size) {
                        scLog.animateScrollTo(scLog.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scLog)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        duelProgressLog.forEach { line ->
                            Text(line, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }

            // Exits anchor
            Button(
                onClick = { 
                    val endRes = gameFinalResult ?: false
                    val finalL = if (endRes) "枪出蛟龙，斩下敌将将玺！" else "落败遭枪，负重伤遁阵。"
                    onDuelComplete(endRes, finalL)
                },
                enabled = !isDuelOngoing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
            ) {
                Text(if (isDuelOngoing) "交锋厮杀轰鸣中..." else "鸣金收兵 回朝归位", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// VIEW SCREEN: BattleView (Upgraded Tactical clashing Battle card Arena)
// -------------------------------------------------------------
@Composable
fun BattleView(
    campaign: Campaign,
    viewModel: GameViewModel,
    onBattleComplete: (Boolean, String) -> Unit
) {
    var logsList by remember { mutableStateOf(listOf<String>()) }
    var currentRound by remember { mutableStateOf(1) }
    var playerTroopsHP by remember { mutableStateOf(8000) }
    var enemyTroopsHP by remember { mutableStateOf(8000) }
    var isCampaignFinished by remember { mutableStateOf(false) }
    var userWonState by remember { mutableStateOf(false) }

    var selectedFormation by remember { mutableStateOf("未定") }

    LaunchedEffect(Unit) {
        val initialLog = "两军对峙，枪橹如林！你率本部校骑迎头接战敌督枭帅【${campaign.enemyName}】的大批镇军（难度:${campaign.enemyForceStrength}）！请部署您先锋战阵。"
        logsList = listOf(initialLog)
    }

    val runBattleRound = {
        if (selectedFormation == "未定") {
            // Must choose formation
        } else {
            val playerLoss: Int
            val enemyLoss: Int
            val roundLog: String

            // Matchups and Counters logic: 八卦 (Eight) counters 鱼鳞 (Fish) | 鱼鳞 (Fish) counters 鹤翼 (Crane) | 鹤翼 (Crane) counters 八卦 (Eight)
            val baseRnd = Random.nextInt(0, 3)
            val enemyFormationPool = listOf("鱼鳞阵", "鹤翼阵", "八卦阵")
            val enemySelectedForm = enemyFormationPool[baseRnd]

            val isCountered = (selectedFormation == "八卦阵" && enemySelectedForm == "鱼鳞阵") ||
                             (selectedFormation == "鱼鳞阵" && enemySelectedForm == "鹤翼阵") ||
                             (selectedFormation == "鹤翼阵" && enemySelectedForm == "八卦阵")

            val counterLossMod = if (isCountered) 0.55f else 1.25f
            val counterEnemyMod = if (isCountered) 1.6f else 0.85f

            playerLoss = (Random.nextInt(1200, 2400) * counterLossMod).toInt()
            enemyLoss = (Random.nextInt(1200, 2400) * counterEnemyMod).toInt()

            playerTroopsHP = (playerTroopsHP - playerLoss).coerceAtLeast(0)
            enemyTroopsHP = (enemyTroopsHP - enemyLoss).coerceAtLeast(0)

            roundLog = "【锋矢对击 - 第 $currentRound 阵】：你布下【$selectedFormation】对开阵斩敌方的【$enemySelectedForm】。交激之下，大将斩戟飞红！我军伤兵车部曲 ${playerLoss} 员，重创敌兵 ${enemyLoss} 骑！"
            logsList = logsList + roundLog

            if (playerTroopsHP <= 0 || enemyTroopsHP <= 0 || currentRound >= 3) {
                isCampaignFinished = true
                userWonState = enemyTroopsHP <= playerTroopsHP
                val finConclusion = if (userWonState) {
                    "【三军欢腾，大捷夺城！】敌军全线大崩溃丢盔曳兵，你督精骑大肆追捕。斩获捷赏、接掌该部印玺库银，威镇华夏一海！"
                } else {
                    "【损兵折将，战阵惜败！】我军后应粮草不济大部断裂。你被困阵中负重伤，在亲随刀卫护送下凄然溃回宿营。"
                }
                logsList = logsList + finConclusion
            } else {
                currentRound++
                selectedFormation = "未定" // Reset for next deployment
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heading banners stats
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Text(
                    text = "—— 阵图军法 · 金戈大合役战 ——",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Player army
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("我军大营步骑: ${playerTroopsHP}部", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { playerTroopsHP / 8000f },
                            modifier = Modifier.width(130.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFD4AF37),
                            trackColor = Color(0xFF2C2C30)
                        )
                    }

                    Text("⚔️", fontSize = 18.sp)

                    // Enemy army
                    Column(horizontalAlignment = Alignment.End) {
                        Text("敌军【${campaign.enemyName}】: ${enemyTroopsHP}部", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { enemyTroopsHP / 8000f },
                            modifier = Modifier.width(130.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFA62B2B),
                            trackColor = Color(0xFF2C2C30)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action logs box
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val scBLog = rememberScrollState()
                    LaunchedEffect(logsList.size) {
                        scBLog.animateScrollTo(scBLog.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scBLog)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        logsList.forEach { line ->
                            Text(line, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }

            // Tactical Placement card choices
            if (!isCampaignFinished) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("第 $currentRound 锋先锋排位阵形部署: ", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "鱼鳞阵" to "高猛突刺 (突锋)",
                                "鹤翼阵" to "包抄御守 (御翼)",
                                "八卦阵" to "奇变玄门 (玄变)"
                            ).forEach { (formName, descTip) ->
                                ElevatedButton(
                                    onClick = { selectedFormation = formName },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = if (selectedFormation == formName) Color(0xFF8B1A1A) else Color(0xFF2E2E34)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(formName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(descTip, fontSize = 8.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Button(
                            onClick = runBattleRound,
                            enabled = selectedFormation != "未定",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                        ) {
                            Text("鸣响战鼓 · 擂鼓冲锋战阵对攻！", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Completed exit buttons
                Button(
                    onClick = {
                        val outcomeMessageLogCombined = if (userWonState) {
                            // Trigger dynamic land update in map if this is a city siege campaign
                            if (campaign.id.startsWith("siege_")) {
                                val cleanTargetId = campaign.id.replace("siege_", "")
                                viewModel.handleCityConquest(cleanTargetId)
                            }
                            "一举击陷【${campaign.enemyName}】大寨，扫清贼寇，接掌库房印绶！"
                        } else {
                            "惨遭埋伏突袭，折损主力，伤及肺腑宿疾。"
                        }
                        onBattleComplete(userWonState, outcomeMessageLogCombined)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                ) {
                    Text("缴印斩旗，鸣金收兵回朝！", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN: GameOver (Funeral details, record hall saving, heirs inherits)
// -------------------------------------------------------------
@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    record: CharacterRecord,
    hasHeir: Boolean
) {
    val heirs by viewModel.childrenList.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0A0A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Funeral White standard text label
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF1E1414), RoundedCornerShape(45.dp))
                    .border(2.dp, Color.LightGray, RoundedCornerShape(45.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "卒",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "—— 三国一梦 · 魂归太虚 ——",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Full life review panel board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1616))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "列传宿主: ${record.surname}${record.name}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD4AF37), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "青史评: " + record.endingRank + " 等",
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "最后封爵谥号: 【${record.maxTitle}】",
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "生于乱世之野，卒于公元 ${record.deathYear} 年（寿享 ${record.deathAge} 岁），最后投靠在【${record.faction}】门下。身留残金 ${record.gold} 银两，清溢英名达 ${record.reputation} 声望。",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // If heirs exist: Display heirs succession controls dynasty!
            if (hasHeir && heirs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF225577), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111E26))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "「命定不绝 · 宗祠家族香火继承」",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "您在世间留有膝下血脉后代。您可以选择一个后裔承袭先祖余荫与累积家业（继承60%金与名望、继承年龄15岁、继承对方培养之维），二代复兴中原！",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        heirs.forEach { heir ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A36))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "${record.surname}${heir.name}（${heir.gender} / 培养${heir.age}岁）",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text("⚔️武:${heir.martial} | 🧠智:${heir.intelligence} | 🎖️统:${heir.command} | 📜政:${heir.politics}", fontSize = 11.sp, color = Color(0xFFD4AF37))
                                    }

                                    Button(
                                        onClick = { viewModel.inheritLegacyWithChild(heir.name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF225577)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("袭爵承续", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Button: Exit back cleanly to title screen
            Button(
                onClick = { 
                    viewModel.exitGameCleanToTitle()
                    viewModel.exitToTitle() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "魂归地府 (退出到廊门)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
