from pathlib import Path

# Rerun marker: title-visuals-v1
path = Path('app/src/main/java/com/example/MainActivity.kt')
text = path.read_text(encoding='utf-8')

box_marker = '''    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {'''
box_insert = '''    ) {
        Image(
            painter = painterResource(id = GameImageAssets.Maps.SANGUO_OVERVIEW),
            contentDescription = "三国开场地图背景",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.28f
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {'''

if 'contentDescription = "三国开场地图背景"' not in text:
    if box_marker not in text:
        raise SystemExit('title box marker not found')
    text = text.replace(box_marker, box_insert, 1)

portrait_marker = '''            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "—— 乱世宏图演义录 · 承嗣继承版 ——",'''
portrait_insert = '''            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    GameImageAssets.Portraits.LIU_BEI,
                    GameImageAssets.Portraits.CAO_CAO,
                    GameImageAssets.Portraits.SUN_QUAN,
                    GameImageAssets.Portraits.GUAN_YU,
                    GameImageAssets.Portraits.ZHUGE_LIANG,
                    GameImageAssets.Portraits.ZHAO_YUN
                ).forEach { portraitRes ->
                    Image(
                        painter = painterResource(id = portraitRes),
                        contentDescription = "三国人物头像",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.65f), RoundedCornerShape(21.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "—— 乱世宏图演义录 · 承嗣继承版 ——",'''

if 'GameImageAssets.Portraits.LIU_BEI' not in text:
    if portrait_marker not in text:
        raise SystemExit('title portrait marker not found')
    text = text.replace(portrait_marker, portrait_insert, 1)

path.write_text(text, encoding='utf-8')
print('patched title visuals')
