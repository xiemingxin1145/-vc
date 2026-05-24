from pathlib import Path

path = Path("app/src/main/java/com/example/ui/GameViewModel.kt")
text = path.read_text(encoding="utf-8")

old_trigger = '''    private fun triggerRandomEvent() {
        val num = Random.nextInt(8)
        val ev = when (num) {'''

new_trigger = '''    private fun triggerRandomEvent() {
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
        } else when (Random.nextInt(8)) {'''

if old_trigger not in text:
    if "SanguoAnnualEventPicker.shouldUseBattleStory" not in text:
        raise SystemExit("triggerRandomEvent anchor not found; file may have diverged")
else:
    text = text.replace(old_trigger, new_trigger, 1)

old_handler = '''    private fun handleEventOutcome(ev: RandomEvent, option: Int) {
        val logPrefix = "公元${currentYear.value}年（${age.value}岁）- [${ev.title}]: "
        var logSuffix = ""

        when (ev.id) {'''

new_handler = '''    private fun handleEventOutcome(ev: RandomEvent, option: Int) {
        val logPrefix = "公元${currentYear.value}年（${age.value}岁）- [${ev.title}]: "
        var logSuffix = ""

        if (ev.id.startsWith("battle_")) {
            val battleLog = applySanguoBattleOutcome(ev, option)
            addLog(logPrefix + battleLog)
            _gameState.value = GameState.Playing
            saveCurrentGameToDb()
            return
        }

        when (ev.id) {'''

if old_handler not in text:
    if "ev.id.startsWith(\"battle_\")" not in text:
        raise SystemExit("handleEventOutcome anchor not found; file may have diverged")
else:
    text = text.replace(old_handler, new_handler, 1)

path.write_text(text, encoding="utf-8")
print("Sanguo battle expansion integrated into GameViewModel.kt")
