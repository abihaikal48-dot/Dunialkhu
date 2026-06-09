package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionRecord
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()

    var currentSubTab by remember { mutableStateOf("Riwayat") } // "Riwayat" or "Analisis"
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Haikal", "Ummu"
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (currentSubTab == "Riwayat") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Catatan")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual Sub-Tabs Switch (Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .padding(4.dp)
            ) {
                listOf("Riwayat", "Analisis").forEach { tab ->
                    val isSelected = currentSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { currentSubTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (tab == "Riwayat") Icons.Default.List else Icons.Default.Favorite,
                                contentDescription = tab,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (tab == "Riwayat") "Catatan Transaksi" else "Analisis Cash Flow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (currentSubTab == "Riwayat") {
                // Filter Tabs (Haikal, Ummu, Combined)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Semua", "Haikal", "Ummu").forEach { person ->
                        val isSelected = selectedFilter == person
                        val modifierWeight = Modifier.weight(1f)
                        Box(
                            modifier = modifierWeight
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedFilter = person }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = person,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Aggregate values based on filter
                val filteredTransactions = when (selectedFilter) {
                    "Haikal" -> transactions.filter { it.person == "Haikal" }
                    "Ummu" -> transactions.filter { it.person == "Ummu" }
                    else -> transactions
                }

                val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val totalSavings = filteredTransactions.filter { it.type == "SAVING" }.sumOf { it.amount }

                // Summary Financial Box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ringkasan Finansial (${selectedFilter})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FinancialMetric(
                                title = "Pendapatan",
                                amount = totalIncome,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            FinancialMetric(
                                title = "Tabungan",
                                amount = totalSavings,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            FinancialMetric(
                                title = "Pengeluaran",
                                amount = totalExpense,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // List of Transactions with special header
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Riwayat Catatan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (filteredTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Bank",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada catatan transaksi keuangan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredTransactions, key = { it.id }) { tx ->
                                TransactionItemRow(
                                    record = tx,
                                    onDelete = { viewModel.deleteTransaction(tx) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Brand-new Cash Flow Analytics panel with Canvas Charts & category Donut breakdowns
                VisualAnalysisSection(
                    viewModel = viewModel,
                    transactions = transactions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Modal Dialog to Add Transaction
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { person, type, amount, category, desc ->
                viewModel.addTransaction(person, type, amount, category, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FinancialMetric(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Rp${"%,.0f".format(amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TransactionItemRow(
    record: TransactionRecord,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (record.type) {
                            "INCOME" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            "SAVING" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (record.type) {
                        "INCOME" -> Icons.Default.Star
                        "SAVING" -> Icons.Default.Favorite
                        else -> Icons.Default.Warning
                    },
                    contentDescription = record.type,
                    tint = when (record.type) {
                        "INCOME" -> MaterialTheme.colorScheme.primary
                        "SAVING" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (record.type == "INCOME" || record.type == "SAVING") "+" else "-"} Rp${"%,.0f".format(record.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (record.type) {
                            "INCOME" -> MaterialTheme.colorScheme.primary
                            "SAVING" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (record.person == "Haikal") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.person,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (record.person == "Haikal") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (person: String, type: String, amount: Double, category: String, desc: String) -> Unit
) {
    var person by remember { mutableStateOf("Haikal") } // "Haikal", "Ummu"
    var type by remember { mutableStateOf("SAVING") } // INCOME, EXPENSE, SAVING
    var amountString by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tabungan Bersama") }
    var description by remember { mutableStateOf("") }

    val categories = when (type) {
        "INCOME" -> listOf("Gaji", "Pekerjaan Sampingan", "Hasil Usaha", "Lainnya")
        "SAVING" -> listOf("Tabungan Bersama", "Uang Kopi Haikal", "Sisa Belanja Ummu", "Tabungan Emas")
        else -> listOf(
            "Persiapan Pernikahan",
            "DP & Kontrakan",
            "Kebutuhan Rumah Tangga",
            "Makan & Kebutuhan",
            "Transportasi",
            "Komunikasi & Internet",
            "Cicilan Motor",
            "Kondangan & Sosial",
            "Lainnya"
        )
    }

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Catat Transaksi Keuangan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector Person
                item {
                    Text(
                        text = "Pemilik Transaksi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Haikal", "Ummu").forEach { name ->
                            val isChosen = person == name
                            Button(
                                onClick = { person = name },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isChosen) {
                                        if (name == "Haikal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    } else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(name)
                            }
                        }
                    }
                }

                // Selector Type
                item {
                    Text(
                        text = "Jenis Transaksi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "SAVING" to "Tabungan",
                            "INCOME" to "Pendapatan",
                            "EXPENSE" to "Pengeluaran"
                        ).forEach { (code, label) ->
                            val isChosen = type == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isChosen) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        type = code
                                        category = categories.first()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Select Category
                item {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
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
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            category = item
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount
                item {
                    OutlinedTextField(
                        value = amountString,
                        onValueChange = { amountString = it },
                        label = { Text("Jumlah Uang (Rupiah)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Catatan / Keterangan") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountString.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && description.isNotBlank()) {
                        onConfirm(person, type, amt, category, description)
                    }
                },
                enabled = amountString.toDoubleOrNull() != null && description.isNotBlank()
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

// Data structure representing cash flow data point
data class CashFlowDataPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val saving: Double
)

@Composable
fun LegendItem(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Function to partition and group transactions across periods
fun getGroupedCashFlow(transactions: List<TransactionRecord>, period: String): List<CashFlowDataPoint> {
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val weekMillis = 7 * dayMillis

    return when (period) {
        "Harian" -> {
            // Last 7 days, from oldest to newest
            (0..6).reversed().map { d ->
                val dayStart = now - (d + 1) * dayMillis
                val dayEnd = now - d * dayMillis
                val dayLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(dayEnd))
                
                val txsInDay = transactions.filter { it.dateMillis in (dayStart + 1)..dayEnd }
                val inc = txsInDay.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = txsInDay.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val sav = txsInDay.filter { it.type == "SAVING" }.sumOf { it.amount }
                CashFlowDataPoint(dayLabel, inc, exp, sav)
            }
        }
        "Mingguan" -> {
            // Last 4 weeks
            (0..3).reversed().map { w ->
                val weekStart = now - (w + 1) * weekMillis
                val weekEnd = now - w * weekMillis
                val label = if (w == 0) "Minggu Ini" else "W-${w}"
                
                val txsInWeek = transactions.filter { it.dateMillis in (weekStart + 1)..weekEnd }
                val inc = txsInWeek.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = txsInWeek.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val sav = txsInWeek.filter { it.type == "SAVING" }.sumOf { it.amount }
                CashFlowDataPoint(label, inc, exp, sav)
            }
        }
        "Bulanan" -> {
            // Group by month names of the last 6 months
            val sdf = SimpleDateFormat("MMM", Locale.getDefault())
            
            (0..5).reversed().map { m ->
                val calStart = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -m)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    timeInMillis = calStart.timeInMillis
                    add(Calendar.MONTH, 1)
                }
                val label = sdf.format(calStart.time)
                
                val txsInMonth = transactions.filter { 
                    it.dateMillis >= calStart.timeInMillis && it.dateMillis < calEnd.timeInMillis 
                }
                val inc = txsInMonth.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = txsInMonth.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val sav = txsInMonth.filter { it.type == "SAVING" }.sumOf { it.amount }
                CashFlowDataPoint(label, inc, exp, sav)
            }
        }
        "Tahunan" -> {
            // Last 2 years
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            (0..1).reversed().map { y ->
                val year = currentYear - y
                val calStart = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    timeInMillis = calStart.timeInMillis
                    add(Calendar.YEAR, 1)
                }
                val label = year.toString()
                
                val txsInYear = transactions.filter { 
                    it.dateMillis >= calStart.timeInMillis && it.dateMillis < calEnd.timeInMillis 
                }
                val inc = txsInYear.filter { it.type == "INCOME" }.sumOf { it.amount }
                val exp = txsInYear.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val sav = txsInYear.filter { it.type == "SAVING" }.sumOf { it.amount }
                CashFlowDataPoint(label, inc, exp, sav)
            }
        }
        else -> emptyList()
    }
}

@Composable
fun VisualAnalysisSection(
    viewModel: MainViewModel,
    transactions: List<TransactionRecord>,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("Harian") } // "Harian", "Mingguan", "Bulanan", "Tahunan"
    
    val dataPoints = remember(transactions, selectedPeriod) {
        getGroupedCashFlow(transactions, selectedPeriod)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Selector Period
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Harian", "Mingguan", "Bulanan", "Tahunan").forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Summary Statistics of Current Period
        item {
            val totalPeriodIncome = dataPoints.sumOf { it.income }
            val totalPeriodExpense = dataPoints.sumOf { it.expense }
            val totalPeriodSaving = dataPoints.sumOf { it.saving }
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Arus Kas Periode Ini (${selectedPeriod})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Pendapatan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Rp${"%,.0f".format(totalPeriodIncome)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Tabungan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Rp${"%,.0f".format(totalPeriodSaving)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Pengeluaran", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Rp${"%,.0f".format(totalPeriodExpense)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        // Cash Flow Bar Chart
        item {
            CashFlowBarChart(dataPoints = dataPoints)
        }

        // Circular Donut Categories
        item {
            DonutAnalysis(transactions = transactions)
        }

        // AI Budget Optimizer & Smart Tips Card (NEW FEATURE!)
        item {
            val budgetAnalysis by viewModel.budgetAnalysis.collectAsState()
            val isBudgetAiLoading by viewModel.isBudgetAiLoading.collectAsState()

            var showFullDialog by remember { mutableStateOf(false) }

            // Trigger analysis on first display if it's empty
            LaunchedEffect(Unit) {
                if (budgetAnalysis.isEmpty() && !isBudgetAiLoading) {
                    viewModel.generateExpenseAnalysis()
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite, // represent sweetheart / optimization
                                contentDescription = "AI Optimizer",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Langkah Bersama Smart Advisor ✨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Analisis Pengeluaran & Tips Menabung Keuangan Berdua",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isBudgetAiLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "AI sedang menyisir kategori pengeluaran...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else if (budgetAnalysis.isNotBlank()) {
                        // Display the first 8 lines/headlines as preview, and a read more button!
                        val previewText = remember(budgetAnalysis) {
                            budgetAnalysis.lines().take(8).joinToString("\n")
                        }
                        
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (budgetAnalysis.lines().size > 8) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { showFullDialog = true },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Baca Analisis Selengkapnya",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.generateExpenseAnalysis() },
                        enabled = !isBudgetAiLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ganti / Perbarui Analisis AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (showFullDialog) {
                AlertDialog(
                    onDismissRequest = { showFullDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "AI Insights",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Analisis Anggaran AI Pasangan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Text(
                                        text = budgetAnalysis,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showFullDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Selesai Membaca")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CashFlowBarChart(
    dataPoints: List<CashFlowDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return
    
    // Find absolute max value to scale the bars
    val maxVal = dataPoints.flatMap { listOf(it.income, it.expense, it.saving) }.maxOrNull()?.coerceAtLeast(1000.0) ?: 1000.0
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Grafik Arus Kas (Cash Flow)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // The Canvas drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    val paddingLeft = 45f
                    val paddingBottom = 40f
                    val graphWidth = width - paddingLeft - 20f
                    val graphHeight = height - paddingBottom - 10f
                    
                    // Draw horizontal grid lines (3 grid lines)
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val y = graphHeight * (1f - i.toFloat() / gridLinesCount) + 10f
                        drawLine(
                            color = onSurfaceVariant,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - 20f, y),
                            strokeWidth = 1f
                        )
                    }
                    
                    // Draw Bars
                    val numPoints = dataPoints.size
                    val groupWidth = graphWidth / numPoints
                    val barWidth = (groupWidth * 0.22f).coerceAtMost(25f)
                    
                    dataPoints.forEachIndexed { index, dp ->
                        val groupCenterX = paddingLeft + (index * groupWidth) + (groupWidth / 2f)
                        
                        // Positions of the 3 bars relative to group center
                        val xInc = groupCenterX - barWidth * 1.5f
                        val xSav = groupCenterX - barWidth * 0.5f
                        val xExp = groupCenterX + barWidth * 0.5f
                        
                        // Scaled Heights
                        val hInc = (dp.income / maxVal) * graphHeight
                        val hSav = (dp.saving / maxVal) * graphHeight
                        val hExp = (dp.expense / maxVal) * graphHeight
                        
                        // Draw Income Bar (Emerald/Primary)
                        if (hInc > 0) {
                            drawRoundRect(
                                color = Color(0xFF10B981), // Emerald
                                topLeft = Offset(xInc, (graphHeight - hInc + 10f).toFloat()),
                                size = Size(barWidth, hInc.toFloat()),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        
                        // Draw Saving Bar (Sky/Tertiary)
                        if (hSav > 0) {
                            drawRoundRect(
                                color = Color(0xFF0EA5E9), // Sky Blue
                                topLeft = Offset(xSav, (graphHeight - hSav + 10f).toFloat()),
                                size = Size(barWidth, hSav.toFloat()),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        
                        // Draw Expense Bar (Red/Error)
                        if (hExp > 0) {
                            drawRoundRect(
                                color = Color(0xFFEF4444), // Red
                                topLeft = Offset(xExp, (graphHeight - hExp + 10f).toFloat()),
                                size = Size(barWidth, hExp.toFloat()),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }
                
                // Labels Layer below Canvas to avoid issues and render perfectly
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 24.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dataPoints.forEach { dp ->
                        Text(
                            text = dp.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(label = "Pendapatan", color = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(label = "Tabungan", color = Color(0xFF0EA5E9))
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(label = "Pengeluaran", color = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun DonutAnalysis(
    transactions: List<TransactionRecord>,
    modifier: Modifier = Modifier
) {
    var donutType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "SAVING"
    
    val filteredTxs = transactions.filter { it.type == donutType }
    
    val groupedData = filteredTxs.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        
    val totalAmount = groupedData.sumOf { it.second }
    
    val donutColors = listOf(
        Color(0xFF4F46E5), // Indigo
        Color(0xFFEF4444), // Red
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFF0EA5E9), // Sky Blue
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF64748B)  // Slate
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Beban Kategori (${if (donutType == "EXPENSE") "Aliran Pengeluaran" else "Distribusi Tabungan"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Toggle Donut Type
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .padding(2.dp)
                ) {
                    listOf("EXPENSE" to "Saku", "SAVING" to "Saku Bersama").forEach { (code, label) ->
                        val isChosen = donutType == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { donutType = code }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (filteredTxs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data untuk kategori ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Donut Canvas (The actual circle ring!)
                    Box(
                        modifier = Modifier
                            .size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            
                            groupedData.forEachIndexed { idx, pair ->
                                val proportion = if (totalAmount > 0) (pair.second / totalAmount) else 0.0
                                val sweepAngle = (proportion * 360f).toFloat()
                                val color = donutColors[idx % donutColors.size]
                                
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 24f)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        
                        // Middle summarytext
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Rp ${if (totalAmount >= 1000000) "%.1fjt".format(totalAmount / 1000000.0) else "%.0frb".format(totalAmount / 1000.0)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Donut Table (Tabel Donut)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupedData.take(5).forEachIndexed { idx, pair ->
                            val color = donutColors[idx % donutColors.size]
                            val percent = if (totalAmount > 0) (pair.second / totalAmount * 100) else 0.0
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pair.first,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rp${"%,.0f".format(pair.second)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${"%.1f".format(percent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = color
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
