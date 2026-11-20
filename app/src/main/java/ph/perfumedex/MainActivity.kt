package ph.perfumedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val Ink = Color(0xFF243C32)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Ink, background = Color(0xFFFAF8F1), surface = Color(0xFFFAF8F1), secondaryContainer = Color(0xFFE0E8D5))) { DexApp(store) } }
    }
}

@Composable private fun DexApp(store: Store) {
    val initial = remember { store.load() }
    var scents by remember { mutableStateOf(initial.first) }
    var wears by remember { mutableStateOf(initial.second) }
    var saved by remember { mutableStateOf(initial.third) }
    var votes by remember { mutableStateOf(store.votes()) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var section by rememberSaveable { mutableStateOf("Cards") }
    var filter by rememberSaveable { mutableStateOf("Hot commute") }
    var selected by remember { mutableStateOf<Scent?>(null) }
    var logging by remember { mutableStateOf<Scent?>(null) }
    var adding by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    fun persist() = store.save(scents, wears, saved, votes)
    fun toggleSave(s: Scent) { saved = if (s.id in saved) saved - s.id else saved + s.id; persist() }
    Scaffold(bottomBar = {
        NavigationBar(containerColor = Color(0xFFF0F0E7)) {
            listOf("Discover", "My Dex", "Community").forEachIndexed { index, label -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(listOf(Icons.Outlined.Explore, Icons.Outlined.Style, Icons.Outlined.People)[index], label) }, label = { Text(label) }) }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("PERFUMEDEX  /  SCENT FIELD NOTES", color = Ink, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Text(listOf("Find your next\nscent story.", "A little more you,\none scent at a time.", "Good scents.\nShared experiences.")[tab], fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 39.sp, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(listOf("Discover â€¢ Wear â€¢ Collect", "${wears.map { it.scent }.distinct().size} cards unlocked Â· ${wears.size} wears logged", "Recommendations for real life in the tropics")[tab], color = Color(0xFF657367))
            }
            if (tab == 0) {
                item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Name, house, or note") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp)) }
                item { Banner("YOUR NEXT DISCOVERY", "Fictional scents. Familiar inspirations.\nExplore a small catalog rooted in Filipino notes.") }
                item { Text("THE SCENT SHELF", fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold) }
                val results = scents.filter { "$it".contains(query.trim(), ignoreCase = true) }
                if (results.isEmpty()) item { Text("No scents found. Add your discovery in My Dex.") }
                items(results, key = { it.id }) { s -> ScentCard(s, s.id in saved, wears.any { it.scent == s.id }, { selected = s }, { logging = s }, { toggleSave(s) }) }
                item { Text("Occasion rankings will appear when a connected community has real votes. Demo activity never enters live rankings.", style = MaterialTheme.typography.bodySmall) }
            } else if (tab == 1) {
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Cards", "Wishlist", "History").forEach { FilterChip(section == it, { section = it }, { Text(it) }) } } }
                item { OutlinedButton({ adding = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text("Add a missing fragrance") } }
                if (section == "History") {
                    if (wears.isEmpty()) item { Banner("YOUR FIRST WEAR", "Log a tester, decant, or bottle to begin your scent story.") }
                    items(wears, key = { it.id }) { w ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(scents.first { it.id == w.scent }.name, fontWeight = FontWeight.Bold)
                            Text("${w.occasion} Â· ${w.format} Â· ${w.date}", style = MaterialTheme.typography.bodySmall)
                            if (w.note.isNotBlank()) Text(w.note)
                            Text(if (w.shared) "Shared in local demo only" else "Private wear", style = MaterialTheme.typography.labelSmall)
                            TextButton({ wears = wears.map { if (it.id == w.id) it.copy(shared = !it.shared) else it }; persist() }) { Text(if (w.shared) "Make private" else "Recommend in demo") }
                        } }
                    }
                } else {
                    val collection = scents.filter { if (section == "Cards") wears.any { w -> w.scent == it.id } else it.id in saved }
                    if (collection.isEmpty()) item { Banner(if (section == "Cards") "A DEX WAITING TO BLOOM" else "YOUR TRY-NEXT LIST", if (section == "Cards") "Every first wear unlocks a card. Start with a scent that catches your curiosity." else "Tap Save to try on any fragrance. Your wishlist stays on this device.") }
                    items(collection, key = { it.id }) { s -> ScentCard(s, s.id in saved, wears.any { it.scent == s.id }, { selected = s }, { logging = s }, { toggleSave(s) }) }
                }
                item { Text("Private by default. Stored on this device; no account needed. Clearing app data removes your Dex.", style = MaterialTheme.typography.bodySmall) }
            } else {
                item { Banner("LOCAL COMMUNITY DEMO", "No account or shared backend connected. Posts and vouches below stay on this device. No live votes or rankings.") }
                item { Column { occasions.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { FilterChip(filter == it, { filter = it }, { Text(it) }) } } } } }
                val demo = listOf(Wear("demo1", "001", "Hot commute", "Fresh on the jeepney ride and still noticeable by lunch.", "Tester", "Sample", true), Wear("demo2", "003", "Office", "Soft and green at my desk. A comfortable afternoon companion.", "Decant", "Sample", true), Wear("demo3", "006", "Date night", "Warm petals for a slow evening out.", "Bottle", "Sample", true), Wear("demo4", "005", "Rainy day", "Earthy and comforting while the rain rolled in.", "Tester", "Sample", true))
                val posts = (wears.filter { it.shared } + demo).filter { it.occasion == filter }
                items(posts, key = { it.id }) { w ->
                    val s = scents.first { it.id == w.scent }
                    val key = VouchRules.key(s.id, w.occasion)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (w.id.startsWith("demo")) "SAMPLE FIELD NOTE Â· DEMO" else "YOUR LOCAL RECOMMENDATION", fontSize = 10.sp, letterSpacing = 1.sp, color = Ink)
                        Text(s.name, fontFamily = FontFamily.Serif, fontSize = 25.sp)
                        Text(w.occasion, style = MaterialTheme.typography.labelMedium)
                        Text(w.note.ifBlank { "A scent worth trying for ${w.occasion.lowercase()}." })
                        Text("${if (key in votes) 1 else 0} local demo vouches Â· 0 live votes", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ if (!VouchRules.eligible(wears, s.id)) { notice = "Log a wear of ${s.name} first. A vouch means you have tried it and recommend it for ${w.occasion.lowercase()}." } else { votes = if (key in votes) votes - key else votes + key; persist() } }) { Text(if (key in votes) "Undo vouch" else "I vouch") }
                            TextButton({ toggleSave(s) }) { Text(if (s.id in saved) "Saved âœ“" else "Save to try") }
                        }
                    } }
                }
                item { Text("A vouch belongs to a fragrance + occasion, so it is reflected across matching posts. This demo represents one local participant only.", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
    selected?.let { s -> AlertDialog(onDismissRequest = { selected = null }, title = { Text(s.name) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(s.house); Text(s.notes); Text(s.story); Text("Fictional catalog profile Â· explore on your own skin.") } }, confirmButton = { TextButton({ selected = null; logging = s }) { Text("Log wear") } }, dismissButton = { TextButton({ toggleSave(s) }) { Text(if (s.id in saved) "Remove from wishlist" else "Save to try") } }) }
    logging?.let { s -> WearDialog(s, { logging = null }) { occasion, note, format, shared ->
        val first = wears.none { it.scent == s.id }
        wears = listOf(Wear(UUID.randomUUID().toString(), s.id, occasion, note.trim(), format, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy Â· HH:mm")), shared)) + wears
        saved = saved - s.id; persist(); logging = null
        notice = if (first) "Card unlocked: ${s.name}! Find it in My Dex." else "Wear saved to your history."
    } }
    if (adding) AddDialog({ adding = false }) { name, house, notes ->
        val s = Scent(UUID.randomUUID().toString(), name.trim(), house.trim().ifBlank { "Personal discovery" }, notes.trim(), "Your own discovery.", 0xFFDED1EA)
        scents = scents + s; persist(); adding = false; logging = s
    }
    notice?.let { AlertDialog(onDismissRequest = { notice = null }, title = { Text("Your scent story") }, text = { Text(it) }, confirmButton = { TextButton({ notice = null }) { Text("Got it") } }) }
}

@Composable private fun Banner(eyebrow: String, body: String) {
    Column(Modifier.fillMaxWidth().background(Color(0xFFE7EBDD), RoundedCornerShape(18.dp)).padding(18.dp)) { Text(eyebrow, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = Ink); Spacer(Modifier.height(8.dp)); Text(body, color = Ink, style = MaterialTheme.typography.bodyMedium) }
}

@Composable private fun ScentCard(s: Scent, saved: Boolean, unlocked: Boolean, open: () -> Unit, log: () -> Unit, save: () -> Unit) {
    Card(onClick = open, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(68.dp, 92.dp).background(Color(s.color), RoundedCornerShape(30.dp, 30.dp, 12.dp, 12.dp)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.WaterDrop, null, tint = Ink, modifier = Modifier.size(30.dp)); Text(s.id.takeLast(3).uppercase(), fontSize = 10.sp, color = Ink) } }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(if (unlocked) "UNLOCKED  âœ¦" else s.house.uppercase(), fontSize = 10.sp, letterSpacing = 1.sp, color = Ink); Text(s.name, fontFamily = FontFamily.Serif, fontSize = 23.sp); Text(s.notes, style = MaterialTheme.typography.bodySmall, color = Color(0xFF657367)) }
        }
        Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilledTonalButton(log) { Text("Log wear") }; TextButton(save) { Text(if (saved) "Saved âœ“" else "Save to try") } }
    }
}

@Composable private fun WearDialog(s: Scent, dismiss: () -> Unit, save: (String, String, String, Boolean) -> Unit) {
    var occasion by rememberSaveable { mutableStateOf(occasions.first()) }
    var format by rememberSaveable { mutableStateOf("Tester") }
    var note by rememberSaveable { mutableStateOf("") }
    var share by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Log ${s.name}") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("What was the occasion?")
        occasions.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { FilterChip(occasion == it, { occasion = it }, { Text(it) }) } } }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Tester", "Decant", "Bottle").forEach { FilterChip(format == it, { format = it }, { Text(it) }) } }
        OutlinedTextField(note, { note = it.take(280) }, label = { Text("A quick note (optional)") }, supportingText = { Text("${note.length}/280") }, maxLines = 3)
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(share, { share = it }); Text("Recommend in local demo", style = MaterialTheme.typography.bodySmall) }
        Text("Private unless selected. Demo sharing stays on this device.", style = MaterialTheme.typography.bodySmall)
    } }, confirmButton = { Button({ save(occasion, note, format, share) }) { Text("Save wear") } }, dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}

@Composable private fun AddDialog(dismiss: () -> Unit, save: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }; var house by rememberSaveable { mutableStateOf("") }; var notes by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Add your discovery") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Your addition stays in your personal catalog.")
        OutlinedTextField(name, { name = it.take(80) }, label = { Text("Fragrance name *") }, singleLine = true)
        OutlinedTextField(house, { house = it.take(80) }, label = { Text("House") }, singleLine = true)
        OutlinedTextField(notes, { notes = it.take(180) }, label = { Text("Notes") }, maxLines = 2)
    } }, confirmButton = { Button({ save(name, house, notes) }, enabled = name.isNotBlank()) { Text("Add & log") } }, dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}

