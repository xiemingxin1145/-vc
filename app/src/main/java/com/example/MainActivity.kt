package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CharacterRecord
import com.example.ui.Campaign
import com.example.ui.GameState
import com.example.ui.GameViewModel
import com.example.ui.RandomEvent
import com.example.ui.theme.MyApplicationTheme

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

@Composable
fun TitleScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val activeSave by viewModel.activeSession.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("三国人生模拟", color = Color(0xFFD4AF37), fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("救急编译版：先保证 APK 能打包，再逐步恢复完整 UI。", color = Color.LightGray, textAlign = TextAlign.Center)
            Button(onClick = { viewModel.enterBirthSetup() }, modifier = Modifier.fillMaxWidth()) {
                Text("开辟新人生")
            }
            Button(
                onClick = {
                    if (activeSave != null) {
                        viewModel.loadSavedGame()
                    } else {
                        Toast.makeText(context, "暂无存档", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = activeSave != null
            ) {
                Text("继续旧人生")
            }
        }
    }
}

@Composable
fun BirthSetupScreen(viewModel: GameViewModel) {
    val surname by viewModel.surname.collectAsState()
    val name by viewModel.name.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val hometown by viewModel.hometown.collectAsState()
    val origin by viewModel.origin.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("投生乾坤", color = Color(0xFFD4AF37), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        InfoCard("姓名", "$surname$name  性别：$gender")
        InfoCard("祖籍 / 出身", "$hometown · $origin")

        Button(onClick = { viewModel.randomizeIdentity() }, modifier = Modifier.fillMaxWidth()) {
            Text("随机姓名")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.gender.value = "男"; viewModel.randomizeIdentity() }, modifier = Modifier.weight(1f)) { Text("男") }
            Button(onClick = { viewModel.gender.value = "女"; viewModel.randomizeIdentity() }, modifier = Modifier.weight(1f)) { Text("女") }
        }
        Button(
            onClick = {
                viewModel.startNewGame(
                    allocatedMartial = 55,
                    allocatedIntel = 55,
                    allocatedCommand = 55,
                    allocatedPol = 55,
                    allocatedCharisma = 55,
                    selectedTalents = viewModel.rollSetupTalents().take(2)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始乱世人生")
        }
        TextButton(onClick = { viewModel.exitToTitle() }) { Text("返回标题") }
    }
}

@Composable
fun PlayingScreen(viewModel: GameViewModel) {
    val surname by viewModel.surname.collectAsState()
    val name by viewModel.name.collectAsState()
    val age by viewModel.age.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val gold by viewModel.gold.collectAsState()
    val rep by viewModel.reputation.collectAsState()
    val health by viewModel.health.collectAsState()
    val faction by viewModel.currentFaction.collectAsState()
    val job by viewModel.currentJob.collectAsState()
    val logs by viewModel.lifeLogs.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Text("$surname$name", color = Color(0xFFD4AF37), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("公元 $year 年 · $age 岁 · $faction · $job", color = Color.LightGray)
        Text("金钱：$gold   声望：$rep   健康：$health", color = Color.White)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.advanceAge() }, modifier = Modifier.weight(1f)) { Text("过一年") }
            Button(
                onClick = {
                    viewModel.saveCurrentGameToDb()
                    Toast.makeText(context, "已存档", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) { Text("存档") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.trainSkill("武力") }, modifier = Modifier.weight(1f)) { Text("练武") }
            Button(onClick = { viewModel.trainSkill("智谋") }, modifier = Modifier.weight(1f)) { Text("读书") }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)), modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                logs.forEach { log ->
                    Text(log, color = Color(0xFFE5E0D8), fontSize = 13.sp, lineHeight = 19.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EventChoiceDialog(event: RandomEvent, onOptionSelected: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(event.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(event.description)
                Button(onClick = { onOptionSelected(1) }, modifier = Modifier.fillMaxWidth()) { Text(event.option1) }
                Button(onClick = { onOptionSelected(2) }, modifier = Modifier.fillMaxWidth()) { Text(event.option2) }
                Button(onClick = { onOptionSelected(3) }, modifier = Modifier.fillMaxWidth()) { Text(event.option3) }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DuelView(opponentName: String, opponentMartial: Int, onDuelComplete: (Boolean, String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141416)).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("阵前单挑", color = Color(0xFFD4AF37), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("敌将：$opponentName  武力：$opponentMartial", color = Color.White)
            Button(onClick = { onDuelComplete(true, "你阵前奋勇，击退$opponentName。") }) { Text("结束单挑") }
        }
    }
}

@Composable
fun BattleView(campaign: Campaign, viewModel: GameViewModel, onBattleComplete: (Boolean, String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141416)).padding(20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(campaign.name, color = Color(0xFFD4AF37), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(campaign.description, color = Color.LightGray, textAlign = TextAlign.Center)
            Button(onClick = { onBattleComplete(true, "你率军出征，取得阶段性胜利。") }) { Text("结束战役") }
        }
    }
}

@Composable
fun GameOverScreen(viewModel: GameViewModel, record: CharacterRecord, hasHeir: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("魂归太虚", color = Color(0xFFD4AF37), fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("${record.surname}${record.name}，享年 ${record.deathAge} 岁", color = Color.White)
        Text("青史评级：${record.endingRank} 等", color = Color.LightGray)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { viewModel.exitGameCleanToTitle() }) { Text("返回标题") }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, color = Color.White)
        }
    }
}
