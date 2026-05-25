from pathlib import Path

# Rerun marker: event-images-v1
path = Path('app/src/main/java/com/example/MainActivity.kt')
text = path.read_text(encoding='utf-8')

marker = '                Text(event.description, color = Color(0xFFE5E0D8), fontSize = 14.sp, lineHeight = 20.sp)\n\n                Spacer(modifier = Modifier.height(12.dp))\n'
insert = '''                val eventImageRes = GameImageAssets.imageForEvent(event.id)
                if (eventImageRes != null) {
                    Image(
                        painter = painterResource(id = eventImageRes),
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

'''
if insert.strip() not in text:
    if marker not in text:
        raise SystemExit('event dialog marker not found')
    text = text.replace(marker, insert + marker, 1)

path.write_text(text, encoding='utf-8')
print('patched event dialog images')
