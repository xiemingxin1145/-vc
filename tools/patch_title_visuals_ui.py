from pathlib import Path

path = Path('app/src/main/java/com/example/MainActivity.kt')
text = path.read_text(encoding='utf-8')

old_bg = '''        Image(
            painter = painterResource(id = GameImageAssets.Maps.SANGUO_OVERVIEW),
            contentDescription = "三国开场地图背景",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.28f
        )'''
new_bg = '''        Image(
            painter = painterResource(id = GameImageAssets.Maps.SANGUO_OVERVIEW),
            contentDescription = "三国开场地图背景",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.55f
        )'''
if old_bg in text:
    text = text.replace(old_bg, new_bg, 1)

old_portraits = '''            Row(
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

            Spacer(modifier = Modifier.height(16.dp))'''
new_portraits = '''            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F11))
            ) {
                Image(
                    painter = painterResource(id = GameImageAssets.Battles.RED_CLIFF_FIRE),
                    contentDescription = "赤壁火攻开场海报",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            .size(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF101010), RoundedCornerShape(32.dp))
                            .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(32.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))'''
if old_portraits in text:
    text = text.replace(old_portraits, new_portraits, 1)
elif 'contentDescription = "赤壁火攻开场海报"' not in text:
    raise SystemExit('portrait block not found')

path.write_text(text, encoding='utf-8')
print('patched title visuals v2')
