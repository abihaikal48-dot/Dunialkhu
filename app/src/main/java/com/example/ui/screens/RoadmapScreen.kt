package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavingsGoal
import com.example.data.JournalEntry
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

@Composable
fun RoadmapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val journals by viewModel.allJournalEntries.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Milestones & Proyeksi, 1: Jurnal Harian

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddJournalDialog by remember { mutableStateOf(false) }

    // Aggregate savings
    val haikalSavings = transactions.filter { it.person == "Haikal" && it.type == "SAVING" }.sumOf { it.amount }
    val ummuSavings = transactions.filter { it.person == "Ummu" && it.type == "SAVING" }.sumOf { it.amount }
    val combinedSavings = haikalSavings + ummuSavings
    val totalGoalAmount = goals.sumOf { it.targetAmount }

    // User-adjustable projection parameter (joint savings per month)
    var jointMonthlySavingEstimate by remember { mutableStateOf(1200000.0) } // Default: 1.2jt (based on Rp20k each daily)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Milestone")
                }
            } else {
                FloatingActionButton(
                    onClick = { showAddJournalDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Tulis Jurnal")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant M3 TabRow to switch between Milestones and Journal
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Milestones Keuangan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Milestones") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Jurnal Berdua & Gratitude", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Jurnal") }
                )
            }

            if (activeTab == 0) {
                // Milestones & Proyeksi View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    // Interactive Projections Simulator Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Simulator Proyeksi Hidup Bersama ⏳",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mengalkulasi waktu pencapaian target berdasarkan komitmen menabung bulanan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Estimate Input Slider
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Nabung Bersama:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(130.dp)
                                    )
                                    Text(
                                        text = "Rp${"%,.0f".format(jointMonthlySavingEstimate)} / bulan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = jointMonthlySavingEstimate.toFloat(),
                                    onValueChange = { jointMonthlySavingEstimate = it.toDouble().coerceIn(300000.0, 5000000.0) },
                                    valueRange = 300000f..5000000f,
                                    steps = 47,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                // Calculations output
                                val remainingToSave = (totalGoalAmount - combinedSavings).coerceAtLeast(0.0)
                                val monthsEstimated = if (jointMonthlySavingEstimate > 0) ceil(remainingToSave / jointMonthlySavingEstimate).toInt() else 0

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "Kekurangan Sisa",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Rp${"%,.0f".format(remainingToSave)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Estimasi Waktu",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "$monthsEstimated Bulan Lagi",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "💡 Haikal memiliki cicilan motor Rp550.000 selama 30 bulan. Ini berarti Haikal perlu berjuang memperbesar pemasukan agar target menabung Rp${"%,.0f".format(jointMonthlySavingEstimate / 2)} tetap terpenuhi secara sehat!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // List of Roadmap Steps / Milestones
                    item {
                        Text(
                            text = "Tahapan Hidup Bersama (Roadmap)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (goals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada milestone tahapan.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    } else {
                        items(goals) { goal ->
                            var isExpanded by remember { mutableStateOf(false) }
                            
                            var cumulativeRequired = 0.0
                            for (g in goals) {
                                if (g.priority < goal.priority || (g.priority == goal.priority && g.id < goal.id)) {
                                    cumulativeRequired += g.targetAmount
                                }
                            }
                            val allocatedSavings = (combinedSavings - cumulativeRequired).coerceIn(0.0, goal.targetAmount)
                            val percentAllocated = if (goal.targetAmount > 0) (allocatedSavings / goal.targetAmount * 100).coerceAtLeast(0.0) else 0.0

                            RoadmapMilestoneCard(
                                goal = goal,
                                allocatedAmount = allocatedSavings,
                                progressPercent = percentAllocated,
                                jointMonthlySavingEstimate = jointMonthlySavingEstimate,
                                isExpanded = isExpanded,
                                onCardClicked = { isExpanded = !isExpanded },
                                onDeleteGoal = { viewModel.deleteGoal(goal) }
                            )
                        }
                    }
                }
            } else {
                // Journal & Gratitude Section
                JournalSection(
                    journals = journals,
                    onDeleteEntry = { viewModel.deleteJournalEntry(it) }
                )
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, desc, targetAmt, targetDate, cat, priority ->
                viewModel.addGoal(title, desc, targetAmt, targetDate, cat, priority)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddJournalDialog) {
        AddJournalDialog(
            onDismiss = { showAddJournalDialog = false },
            onConfirm = { author, content, gratitude, mood ->
                viewModel.addJournalEntry(author, content, gratitude, mood)
                showAddJournalDialog = false
            }
        )
    }
}

@Composable
fun RoadmapMilestoneCard(
    goal: SavingsGoal,
    allocatedAmount: Double,
    progressPercent: Double,
    jointMonthlySavingEstimate: Double,
    isExpanded: Boolean,
    onCardClicked: () -> Unit,
    onDeleteGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClicked() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon circle wrapper
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when (goal.category) {
                                "Pernikahan" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                "Tempat Tinggal" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (goal.category) {
                            "Pernikahan" -> Icons.Default.Favorite
                            "Tempat Tinggal" -> Icons.Default.Home
                            else -> Icons.Default.Star
                        },
                        contentDescription = goal.category,
                        tint = when (goal.category) {
                            "Pernikahan" -> MaterialTheme.colorScheme.primary
                            "Tempat Tinggal" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (goal.priority) {
                                        1 -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (goal.priority) {
                                    1 -> "Utama"
                                    2 -> "Penting"
                                    else -> "Opsional"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (goal.priority) {
                                    1 -> MaterialTheme.colorScheme.error
                                    2 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Target: Rp${"%,.0f".format(goal.targetAmount)} • Selesai: ${goal.targetDateString}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (progressPercent / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (goal.category) {
                    "Pernikahan" -> MaterialTheme.colorScheme.primary
                    "Tempat Tinggal" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Teralokasi: Rp${"%,.0f".format(allocatedAmount)} dari Rp${"%,.0f".format(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    // Display Time Remaining Calculation!
                    val neededAmount = (goal.targetAmount - allocatedAmount).coerceAtLeast(0.0)
                    if (neededAmount <= 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tercapai",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lunas / Tercapai! 🎉",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        val estMonthsLeft = if (jointMonthlySavingEstimate > 0) ceil(neededAmount / jointMonthlySavingEstimate).toInt() else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Sisa Waktu",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kurang Rp${"%,.0f".format(neededAmount)} • ⏳ Sisa: ${estMonthsLeft} bln lagi",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                
                Text(
                    text = "${"%.1f".format(progressPercent)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = when (goal.category) {
                        "Pernikahan" -> MaterialTheme.colorScheme.primary
                        "Tempat Tinggal" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDeleteGoal() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Milestone")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, targetAmount: Double, dateString: String, category: String, priority: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetAmtStr by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("Desember 2027") }
    var category by remember { mutableStateOf("Pernikahan") }
    var priority by remember { mutableStateOf(2) } // 1, 2, 3

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Tambah Tahapan Impian", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Impian (misal: Sewa Rumah)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Deskripsi/Spesifikasi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetAmtStr,
                        onValueChange = { targetAmtStr = it },
                        label = { Text("Target Biaya (Rupiah)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Target Selesai (misal: Agustus 2027)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Pernikahan", "Tempat Tinggal", "Dana Darurat", "Perangkat Rumah", "Lainnya").forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = {
                                    category = cat
                                    expanded = false
                                })
                            }
                        }
                    }
                }

                item {
                    Text("Skala Prioritas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1 to "Utama", 2 to "Penting", 3 to "Opsional").forEach { (level, lbl) ->
                            val isSel = priority == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { priority = level }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(lbl, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = targetAmtStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, desc, amt, dateString, category, priority)
                    }
                },
                enabled = title.isNotBlank() && targetAmtStr.toDoubleOrNull() != null
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun JournalSection(
    journals: List<JournalEntry>,
    onDeleteEntry: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Journal Summary Stats Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love Journal",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Langkah Bersama Diary 📖",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Log refleksi harian, ungkapan terima kasih, dan mimpi kecil Haikal & Ummu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Mood & Gratitude Analytics Card (NEW FEATURE!)
        item {
            val totalNotes = journals.size
            if (totalNotes > 0) {
                val moodCounts = remember(journals) {
                    journals.groupBy { it.mood }.mapValues { it.value.size }
                }
                
                val senangCount = moodCounts["Senang"] ?: 0
                val syukurCount = moodCounts["Syukur"] ?: 0
                val optimisCount = moodCounts["Optimis"] ?: 0
                val tantanganCount = moodCounts["Tantangan"] ?: 0
                
                val totalWithMoods = (senangCount + syukurCount + optimisCount + tantanganCount).coerceAtLeast(1)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Mood Tracking",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Mood & Rasa Syukur Tracker 💞",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Mood grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("😊 Senang", senangCount, Color(0xFF10B981)),
                                Triple("🙏 Syukur", syukurCount, Color(0xFFF97316)),
                                Triple("🚀 Optimis", optimisCount, Color(0xFF3B82F6)),
                                Triple("💪 Tantangan", tantanganCount, Color(0xFFEC4899))
                            ).forEach { (label, count, color) ->
                                val pct = (count.toFloat() / totalWithMoods.toFloat()) * 100f
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$count kali",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = color
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Mini progress line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct / 100f)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (journals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada tulisan jurnal. Mari catat momen indah pertamamu hari ini!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(journals) { journal ->
                val dateFormatted = remember(journal.dateMillis) {
                    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
                    sdf.format(Date(journal.dateMillis))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Header: Date, Author & Mood
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )

                            // Delete button
                            IconButton(
                                onClick = { onDeleteEntry(journal) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadata Row (Author Badge & Mood Badge)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Author Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (journal.author) {
                                            "Haikal" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            "Ummu" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Ditulis: ${journal.author}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (journal.author) {
                                        "Haikal" -> MaterialTheme.colorScheme.primary
                                        "Ummu" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                            }

                            // Mood Badge
                            val moodEmoji = when (journal.mood) {
                                "Senang" -> "😊"
                                "Syukur" -> "🙏"
                                "Optimis" -> "🚀"
                                "Tantangan" -> "💪"
                                else -> "✨"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$moodEmoji ${journal.mood}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Main Content
                        Text(
                            text = "Pengalaman & Refleksi:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = journal.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        if (journal.gratitude.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            // Gratitude box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Gratitude",
                                            tint = Color(0xFFE5A93B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rasa Syukur Hari Ini:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB57A1B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "“${journal.gratitude}”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddJournalDialog(
    onDismiss: () -> Unit,
    onConfirm: (author: String, content: String, gratitude: String, mood: String) -> Unit
) {
    var author by remember { mutableStateOf("Haikal") } // Haikal, Ummu, Bersama
    var content by remember { mutableStateOf("") }
    var gratitude by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Senang") } // Senang, Syukur, Optimis, Tantangan

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Tulis Catatan Harian 📝",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Author Selection
                item {
                    Text(
                        text = "Ditulis Oleh",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Haikal", "Ummu", "Bersama").forEach { nominee ->
                            val isSel = author == nominee
                            val chipBg = if (isSel) {
                                when (nominee) {
                                    "Haikal" -> MaterialTheme.colorScheme.primary
                                    "Ummu" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                            val chipText = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .clickable { author = nominee }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nominee,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipText
                                )
                            }
                        }
                    }
                }

                // Mood Selector
                item {
                    Text(
                        text = "Bagaimana perasaanmu? (Mood)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Senang" to "😊",
                            "Syukur" to "🙏",
                            "Optimis" to "🚀",
                            "Tantangan" to "💪"
                        ).forEach { (lbl, emoji) ->
                            val isSel = mood == lbl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { mood = lbl }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(emoji, fontSize = 18.sp)
                                    Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Content reflection Text Area
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Pengalaman & Refleksi Berdua") },
                        placeholder = { Text("Misal: Tulis momen diskusi santai, survei kontrakan, atau hal indah hari ini...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Gratitude list / Notes Text Area
                item {
                    OutlinedTextField(
                        value = gratitude,
                        onValueChange = { gratitude = it },
                        label = { Text("Apa yang paling kamu syukuri?") },
                        placeholder = { Text("Misal: Memiliki pasangan yang sabar, rezeki usaha katering Ummu lancar...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(author, content, gratitude, mood)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Batal")
            }
        }
    )
}
