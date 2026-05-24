# Sanguo content patch

Added files:

- app/src/main/java/com/example/ui/SanguoContentPack.kt
- app/src/main/res/drawable/illust_yellow_turban_oath.xml
- app/src/main/res/drawable/illust_luoyang_night.xml
- app/src/main/res/drawable/illust_longzhong_strategy.xml
- app/src/main/res/drawable/illust_red_cliff_wind.xml

What it does:

- Adds a premium story event pack.
- Adds simple vector illustrations for major events.
- Adds ending title ideas.

Next integration step:

- Wire SanguoContentPack.premiumEvents into GameViewModel.triggerRandomEvent().
- Show the matching illustration in EventChoiceDialog.
