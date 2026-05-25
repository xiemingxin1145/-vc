from pathlib import Path

# Rerun marker: visual-map-ui-v1
path = Path('app/src/main/java/com/example/MainActivity.kt')
text = path.read_text(encoding='utf-8')

if 'import androidx.compose.ui.res.painterResource' not in text:
    text = text.replace(
        'import androidx.compose.ui.platform.testTag\n',
        'import androidx.compose.ui.platform.testTag\nimport androidx.compose.ui.res.painterResource\nimport androidx.compose.ui.layout.ContentScale\n',
        1,
    )

marker = '                // Draw map paths and rivers\n'
insert = '''                Image(
                    painter = painterResource(id = GameImageAssets.Maps.SANGUO_OVERVIEW),
                    contentDescription = "三国天下地图",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.82f
                )

'''
if insert.strip() not in text:
    if marker not in text:
        raise SystemExit('map canvas marker not found')
    text = text.replace(marker, insert + marker, 1)

path.write_text(text, encoding='utf-8')
print('patched MainActivity visual map UI')
