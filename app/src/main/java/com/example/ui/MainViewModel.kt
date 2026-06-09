package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // Exposure of Flows
    val allGoals = repository.allGoals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allTransactions = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allSchedules = repository.allSchedules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allJournalEntries = repository.allJournalEntries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // AI Consultant State
    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // AI Budget & Expense Analyzer State
    private val _budgetAnalysis = MutableStateFlow<String>("")
    val budgetAnalysis: StateFlow<String> = _budgetAnalysis.asStateFlow()

    private val _isBudgetAiLoading = MutableStateFlow(false)
    val isBudgetAiLoading: StateFlow<Boolean> = _isBudgetAiLoading.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed initial data if database is brand new
            allGoals.collectLatest { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        val initialGoals = listOf(
            SavingsGoal(
                title = "Pernikahan Sederhana & Berkah",
                description = "Tabungan untuk akad nikah, sewa baju, katering mandiri, mas kawin dasar, dan syukuran hangat di rumah/KUA.",
                targetAmount = 15000000.0,
                currentSavedAmount = 0.0,
                targetDateString = "Desember 2027",
                category = "Pernikahan",
                priority = 1
            ),
            SavingsGoal(
                title = "Kontrakan Rumah Tahun Pertama",
                description = "Uang sewa rumah sederhana tipe 36 atau kontrakan bulanan untuk tempat mandiri pertama saat sah nanti.",
                targetAmount = 8000000.0,
                currentSavedAmount = 0.0,
                targetDateString = "Maret 2028",
                category = "Tempat Tinggal",
                priority = 2
            ),
            SavingsGoal(
                title = "Tabungan Dana Darurat Bersama",
                description = "Antisipasi kebutuhan medis, perbaikan penting, atau dana darurat jika ada kendala pekerjaan berdua.",
                targetAmount = 3000000.0,
                currentSavedAmount = 0.0,
                targetDateString = "Juni 2028",
                category = "Dana Darurat",
                priority = 2
            ),
            SavingsGoal(
                title = "Perabotan Rumah Tangga Dasar",
                description = "Keperluan esensial seperti kasur, kompor gas, penanak nasi, kipas angin, dan peralatan makan dasar.",
                targetAmount = 2000000.0,
                currentSavedAmount = 0.0,
                targetDateString = "Mei 2028",
                category = "Perangkat Rumah",
                priority = 3
            )
        )

        val initialSchedules = listOf(
            DailySchedule(
                assignee = "Haikal",
                title = "Riset berkas pendaftaran KUA",
                description = "Mencari tahu berkas administrasi nikah beda daerah & biaya resmi nikah di luar KUA vs di dalam KUA.",
                dateString = "Minggu Ini",
                isCompleted = false,
                category = "Riset"
            ),
            DailySchedule(
                assignee = "Ummu",
                title = "Buat list rincian mahar & mas kawin",
                description = "Mencocokkan harga emas antam atau mas kawin sederhana yang realistis untuk awal hidup berdua.",
                dateString = "Hari Ini",
                isCompleted = false,
                category = "Persiapan"
            ),
            DailySchedule(
                assignee = "Bersama",
                title = "Diskusi mingguan alokasi tabungan",
                description = "Berdiskusi santai di akhir pekan tentang pendapatan masing-masing dan komitmen menyisihkan dana tabungan berdua.",
                dateString = "Setiap Sabtu",
                isCompleted = false,
                category = "Rencana"
            ),
            DailySchedule(
                assignee = "Haikal",
                title = "Konsisten tabungan harian Rp20.000",
                description = "Menyisihkan uang kopi harian untuk mempercepat target dana nikah bersama Ummu.",
                dateString = "Setiap Hari",
                isCompleted = false,
                category = "Tabungan"
            ),
            DailySchedule(
                assignee = "Ummu",
                title = "Konsisten tabungan harian Rp20.000",
                description = "Menyisihkan dana harian belanja ringan ditaruh langsung ke celengan digital untuk pernikahan.",
                dateString = "Setiap Hari",
                isCompleted = false,
                category = "Tabungan"
            )
        )

        // Preload transactions across different dates for rich cash flow analysis
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val initialTransactions = listOf(
            // Haikal's base transactions (Today)
            TransactionRecord(
                person = "Haikal",
                type = "INCOME",
                amount = 4800000.0,
                category = "Gaji",
                description = "Gaji pokok bulanan Haikal kerja desain grafis",
                dateMillis = now - 5 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "EXPENSE",
                amount = 550000.0,
                category = "Cicilan Motor",
                description = "Cicilan motor bulanan Ke-7 (Sisa 29 bulan lagi)",
                dateMillis = now - 4 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "SAVING",
                amount = 600000.0,
                category = "Tabungan Bersama",
                description = "Setoran tabungan berkala Haikal",
                dateMillis = now - 3 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "EXPENSE",
                amount = 120000.0,
                category = "Makan & Kebutuhan",
                description = "Beli belanjaan mingguan",
                dateMillis = now - 1 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "EXPENSE",
                amount = 45000.0,
                category = "Transportasi",
                description = "Isi bensin motor mingguan",
                dateMillis = now
            ),

            // Ummu's base transactions (Today and past days)
            TransactionRecord(
                person = "Ummu",
                type = "INCOME",
                amount = 3500000.0,
                category = "Pekerjaan Sampingan",
                description = "Hasil bisnis katering kue & mengajar les privat",
                dateMillis = now - 4 * oneDayMillis
            ),
            TransactionRecord(
                person = "Ummu",
                type = "SAVING",
                amount = 500000.0,
                category = "Tabungan Bersama",
                description = "Hasil tabungan sampingan Ummu",
                dateMillis = now - 3 * oneDayMillis
            ),
            TransactionRecord(
                person = "Ummu",
                type = "EXPENSE",
                amount = 85000.0,
                category = "Makan & Kebutuhan",
                description = "Bahan pokok katering kue",
                dateMillis = now - 2 * oneDayMillis
            ),
            TransactionRecord(
                person = "Ummu",
                type = "EXPENSE",
                amount = 30000.0,
                category = "Lainnya",
                description = "Uang kuota internet",
                dateMillis = now - 1 * oneDayMillis
            ),

            // Older transactions from last month to showcase monthly and weekly variety
            TransactionRecord(
                person = "Haikal",
                type = "INCOME",
                amount = 300000.0,
                category = "Pekerjaan Sampingan",
                description = "Freelance logo UMKM lokal",
                dateMillis = now - 12 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "EXPENSE",
                amount = 150000.0,
                category = "Makan & Kebutuhan",
                description = "Makan luar santai berdua",
                dateMillis = now - 10 * oneDayMillis
            ),
            TransactionRecord(
                person = "Ummu",
                type = "SAVING",
                amount = 200000.0,
                category = "Sisa Belanja Ummu",
                description = "Uang sisa saku disisihkan",
                dateMillis = now - 15 * oneDayMillis
            ),
            TransactionRecord(
                person = "Haikal",
                type = "EXPENSE",
                amount = 550000.0,
                category = "Cicilan Motor",
                description = "Cicilan motor bulanan Ke-6 (Sejarah bulan lalu)",
                dateMillis = now - 32 * oneDayMillis
            )
        )

        val initialJournals = listOf(
            JournalEntry(
                author = "Haikal",
                content = "Hari ini seneng banget akhirnya bisa duduk berdua lagi setelah seminggu sibuk masing-masing. Kita bahas pelan-pelan soal target tabungan nikah. Ummu dengerin dengan penuh perhatian.",
                gratitude = "Sangat bersyukur punya calon istri yang pengertian dan mau berjuang dari nol bareng-bareng.",
                mood = "Bersyukur",
                dateMillis = now - 3 * oneDayMillis
            ),
            JournalEntry(
                author = "Ummu",
                content = "Selesai bikin list menu katering buat usaha kecilku hari ini, dapet untung bersih lumayan langsung aku masukin ke celengan digital buat nikah nanti! Haikal juga semangatin terus lewat chat.",
                gratitude = "Lancar rezeki hari ini, dan pasangan yang selalu jadi supporter utama di setiap hal kecil.",
                mood = "Optimis",
                dateMillis = now - 2 * oneDayMillis
            ),
            JournalEntry(
                author = "Bersama",
                content = "Kita survei online bareng tipe kontrakan sederhana di sekitar area kerja kita berdua. Kelihatannya ada yang cocok dan harganya masuk akal buat tahun pertama nikah nanti.",
                gratitude = "Selangkah lebih dekat dengan impian tinggal di bawah atap yang sama. Semoga dipermudah jalannya.",
                mood = "Senang",
                dateMillis = now - 1 * oneDayMillis
            )
        )

        repository.insertGoals(initialGoals)
        repository.insertSchedules(initialSchedules)
        repository.insertTransactions(initialTransactions)
        repository.insertJournals(initialJournals)
    }

    // Goal Actions
    fun addGoal(title: String, description: String, targetAmount: Double, targetDateString: String, category: String, priority: Int) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoal(
                    title = title,
                    description = description,
                    targetAmount = targetAmount,
                    targetDateString = targetDateString,
                    category = category,
                    priority = priority
                )
            )
        }
    }

    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Transaction Actions
    fun addTransaction(person: String, type: String, amount: Double, category: String, description: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionRecord(
                    person = person,
                    type = type,
                    amount = amount,
                    category = category,
                    description = description,
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(record: TransactionRecord) {
        viewModelScope.launch {
            repository.deleteTransaction(record)
        }
    }

    // Schedule Actions
    fun addSchedule(assignee: String, title: String, description: String, dateString: String, category: String) {
        viewModelScope.launch {
            repository.insertSchedule(
                DailySchedule(
                    assignee = assignee,
                    title = title,
                    description = description,
                    dateString = dateString,
                    category = category
                )
            )
        }
    }

    fun toggleScheduleCompleted(schedule: DailySchedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule.copy(isCompleted = !schedule.isCompleted))
        }
    }

    fun deleteSchedule(schedule: DailySchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    // Journal Entry Actions
    fun addJournalEntry(author: String, content: String, gratitude: String, mood: String) {
        viewModelScope.launch {
            repository.insertJournal(
                JournalEntry(
                    author = author,
                    content = content,
                    gratitude = gratitude,
                    mood = mood,
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.updateJournal(entry)
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournal(entry)
        }
    }

    // AI Expense / Budget Analyzer & Smart Tips Generator
    fun generateExpenseAnalysis() {
        viewModelScope.launch {
            _isBudgetAiLoading.value = true
            _budgetAnalysis.value = "Menganalisis data pengeluaran berdasarkan kategori, menyusun strategi pemotongan anggaran, dan merancang smart tips..."

            val goalsList = allGoals.value
            val txList = allTransactions.value

            val totalGoalAmount = goalsList.sumOf { it.targetAmount }
            val transactions = txList

            // Calculate aggregate expenses by category
            val expensesByCategory = transactions
                .filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalSaving = transactions.filter { it.type == "SAVING" }.sumOf { it.amount }

            val haikalTransactions = transactions.filter { it.person == "Haikal" }
            val ummuTransactions = transactions.filter { it.person == "Ummu" }

            val haikalIncome = haikalTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val haikalExpense = haikalTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val haikalSaving = haikalTransactions.filter { it.type == "SAVING" }.sumOf { it.amount }

            val ummuIncome = ummuTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val ummuExpense = ummuTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val ummuSaving = ummuTransactions.filter { it.type == "SAVING" }.sumOf { it.amount }

            val key = com.example.BuildConfig.GEMINI_API_KEY

            val categoryBreakdownString = expensesByCategory.entries.joinToString("\n") { (cat, amt) ->
                "- $cat: Rp ${"%,.0f".format(amt)}"
            }

            val prompt = """
                Anda adalah seorang "AI Financial Budget & Saving Optimizer" cerdas bernama "Langkah Bersama Smart Advisor".
                Klien Anda adalah pasangan Haikal & Ummu yang sedang berjuang menabung dari nol demi impian hidup bersama (menikah & kontrak rumah).
                
                Berikut adalah data keuangan real-time bulanan mereka saat ini:
                - Target Goals Keuangan Bersama: Rp ${"%,.0f".format(totalGoalAmount)}
                  * Rincian target: ${goalsList.joinToString { "${it.title} (Rp ${"%,.0f".format(it.targetAmount)})" }}
                
                - Ringkasan Dana Sebulan:
                  * Total Pemasukan: Rp ${"%,.0f".format(totalIncome)} (Haikal: Rp ${"%,.0f".format(haikalIncome)}, Ummu: Rp ${"%,.0f".format(ummuIncome)})
                  * Total Pengeluaran: Rp ${"%,.0f".format(totalExpense)} (Haikal: Rp ${"%,.0f".format(haikalExpense)}, Ummu: Rp ${"%,.0f".format(ummuExpense)})
                  * Total Ditabung: Rp ${"%,.0f".format(totalSaving)} (Haikal: Rp ${"%,.0f".format(haikalSaving)}, Ummu: Rp ${"%,.0f".format(ummuSaving)})
                
                - Rincian Pengeluaran berdasarkan POS / KATEGORI:
                $categoryBreakdownString
                
                - Utang/Tanggungan Tetap:
                  * Haikal memiliki beban Cicilan Motor wajib sebesar Rp 550.000 / bulan selama 30 bulan lagi.
                
                Berikan Analisis Anggaran Kategori dan "Smart Budget Tips" yang sangat spesifik, terukur, realistis, dan praktis dalam Bahasa Indonesia yang hangat namun profesional:
                
                Kerangka jawaban harus memiliki format visual yang indah di layar HP:
                1. 📊 PERSENTASE & RASIO PENGELUARAN: Analisis kategori mana yang paling memakan anggaran (misal: Makan, Transportasi atau DP Kontrakan), apakah rasio pengeluaran dibanding pemasukan sehat (misal: idealnya pengeluaran maks 50-60%).
                2. 🔍 ANALISIS POS EXPENSE UTAMA: Berikan evaluasi spesifik untuk kategori dengan pengeluaran terbesar yang bisa dipotong/dihemat gila-gilaan demi mengencangkan ikat pinggang.
                3. 🚀 3 SMART TIPS OPTIMALISASI ANGGARAN: Tiga kiat aksi (actionable tips) nyata agar Haikal dan Ummu bisa meningkatkan dana menabung mereka hingga 20-30% lebih cepat berbekal data pengeluaran di atas (kaitkan dengan usaha katering Ummu dan efisiensi motor Haikal!).
                4. 💡 REKOMENDASI NOMINAL TABUNGAN IDEAL: Berapa target nominal yang sehat untuk ditabung Haikal & Ummu setiap bulan agar target pernikahan dan kontrakan tercapai lebih cepat tanpa membuat napas keuangan sesak.
                
                Buatlah tulisan yang rapi dengan bullet points, ikon menarik di setiap awal kalimat, dan tulisan tebal pada kata-kata penting agar mudah untuk dibaca di perangkat mobile.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            try {
                if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                    _budgetAnalysis.value = generateLocalBudgetAdvice(totalGoalAmount, totalIncome, totalExpense, totalSaving, expensesByCategory)
                } else {
                    val response = RetrofitClient.service.generateContent(key, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _budgetAnalysis.value = resultText ?: "Gagal mendapatkan analisis budget AI. Silakan coba kembali."
                }
            } catch (e: Exception) {
                _budgetAnalysis.value = "💡 [Analisis Lokal] Data Pengeluaran Berhasil Dianalisis:\n\n" +
                        generateLocalBudgetAdvice(totalGoalAmount, totalIncome, totalExpense, totalSaving, expensesByCategory)
            } finally {
                _isBudgetAiLoading.value = false
            }
        }
    }

    private fun generateLocalBudgetAdvice(
        totalGoal: Double,
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        expensesByCategory: Map<String, Double>
    ): String {
        val mostSpentCategory = expensesByCategory.maxByOrNull { it.value }
        val categoryString = if (mostSpentCategory != null) {
            "Kategori **\"${mostSpentCategory.key}\"** memiliki pengeluaran bulanan gabungan terbesar, yaitu **Rp ${"%,.0f".format(mostSpentCategory.value)}**."
        } else {
            "Belum ada data pengeluaran yang diinput oleh kalian berdua."
        }

        val expenseIncomeRatio = if (totalIncome > 0) (totalExpense / totalIncome) * 100 else 0.0
        val savingIncomeRatio = if (totalIncome > 0) (totalSaving / totalIncome) * 100 else 0.0

        val ratioStatus = when {
            expenseIncomeRatio <= 50.0 -> "Sangat Sehat (di bawah 50%)! Kalian mengelola pengeluaran dengan sangat disiplin."
            expenseIncomeRatio <= 70.0 -> "Cukup Sehat (50% - 70%). Namun, masih ada beberapa pos yang bisa ditekan lagi agar porsi tabungan nikah kalian membengkak."
            else -> "Membutuhkan Perhatian Khusus (>70%). Pengeluaran kalian terlalu besar dibanding pemasukan, menyisakan ruang tabungan yang sangat sempit."
        }

        return """
            📊 **Analisis Anggaran Kategori & Smart Saving Tips** 📊

            Analisis ini disusun khusus berdasarkan data pencatatan pengeluaran gabungan Haikal & Ummu secara real-time.

            📈 **1. Persentase & Rasio Pengeluaran Berdua**
            * **Rasio Pengeluaran**:  **${"%.1f".format(expenseIncomeRatio)}%** dari total pemasukan bersama. Status: **$ratioStatus**
            * **Porsi Tabungan**:  **${"%.1f".format(savingIncomeRatio)}%** dari total pemasukan telah dialokasikan ke tabungan pernikahan & masa depan.
            * *Kondisi*: $categoryString

            🔍 **2. Deteksi Pemborosan & Evaluasi Kategori**
            * **Makan & Kebutuhan**: Sering kali pos ini membengkak karena kurangnya masak sendiri. Mengingat Ummu memiliki bisnis katering, Ummu bisa membagikan sisa menu katering atau bahan segar sisa produksi yang aman untuk diolah bersama Haikal guna menekan biaya makan bulanan hingga **30%**.
            * **Cicilan Motor Haikal (Rp 550.000)**: Ini memakan porsi pendapatan Haikal yang cukup lumayan. Pastikan Haikal merawat motor ini dengan baik agar biaya servis tidak membengkak di luar dugaan, serta pertimbangkan untuk menggunakan motor ini sebagai alat kerja tambahan di waktu senggang.
            * **DP & Kontrakan**: Mulailah mencari listing kontrakan dengan harga ramah kantong jauh sebelum pernikahan berlangsung.

            🚀 **3 Smart Tips Optimalisasi Anggaran Berdua**
            1. **💡 Aturan Penahanan 3 Hari**: Sebelum melakukan pengeluaran besar di luar kategori kebutuhan pokok (seperti hobi atau makan mewah), berdisiplinlah untuk menundanya selama 3 hari. Ini memotong nafsu belanja impulsif hingga **80%**.
            2. **💡 Memanfaatkan Sinergi Katering**: Jadikan keahlian memasak Ummu sebagai tameng keuangan utama kalian. Rencanakan bekal kerja Haikal dari dapur Ummu untuk menghemat biaya makan siang harian.
            3. **💡 Auto-Debet Celengan Digital**: Sisihkan komitmen tabungan minimal **Rp 20.000 per hari** per orang langsung saat pagi hari atau otomatis setelah gajian sebelum uangnya habis untuk kebutuhan lain.

            💡 **Rekomendasi Nominal Tabungan Ideal**
            Untuk mempercepat pencapaian target pernikahan dan sewa kontrakan (total target Rp $totalGoal), kami merekomendasikan target tabungan bulanan minimal sebesar **Rp 1.500.000** secara konsisten. Jalur ini akan mengantarkan kalian ke hari akad nikah dengan lebih tenang dan berkah tanpa utang!
        """.trimIndent()
    }

    // AI / Smart Suggestions Engine
    fun generateSmartAdvice(customQuestion: String? = null) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Sedang menganalisis keuangan, cicilan motor Haikal, dan membuatkan saran..."

            val goalsList = allGoals.value
            val txList = allTransactions.value

            // Compute summary variables
            val totalGoalAmount = goalsList.sumOf { it.targetAmount }
            
            // Current savings in database (type SAVING, or input balances)
            val haikalTransactions = txList.filter { it.person == "Haikal" }
            val ummuTransactions = txList.filter { it.person == "Ummu" }

            val haikalIncome = haikalTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val haikalExpense = haikalTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val haikalSavings = haikalTransactions.filter { it.type == "SAVING" }.sumOf { it.amount }

            val ummuIncome = ummuTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val ummuExpense = ummuTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val ummuSavings = ummuTransactions.filter { it.type == "SAVING" }.sumOf { it.amount }

            val combinedSavings = haikalSavings + ummuSavings

            val key = com.example.BuildConfig.GEMINI_API_KEY

            val defaultPrompt = """
                Anda adalah seorang "Konsultan Hubungan & Keuangan Islami/Pribadi" yang bijaksana, hangat, suportif, dan realistis bernama "Langkah Bersama Assistant".
                Analisis situasi kehidupan dan finansial pasangan Haikal & Ummu berikut ini:
                - Target Hidup Bersama / Menikah:
                  * Total Sasaran Target: Rp ${"%,.0f".format(totalGoalAmount)}
                  * Rincian target: ${goalsList.joinToString { "${it.title} (Rp ${"%,.0f".format(it.targetAmount)})" }}
                  * Kondisi Saat ini: Masing-masing terpisah, belum punya aset besar ("belum punya apa-apa") dan baru mulai menabung.
                - Profil Keuangan Haikal:
                  * Pendapatan tercatat: Rp ${"%,.0f".format(haikalIncome)} per bulan
                  * Tabungan terkumpul: Rp ${"%,.0f".format(haikalSavings)}
                  * Beban Cicilan Motor: Rp 550.000 / bulan (Masih sisa 30 bulan lagi, total beban Rp 16.500.000)
                - Profil Keuangan Ummu:
                  * Pendapatan tercatat: Rp ${"%,.0f".format(ummuIncome)} per bulan
                  * Tabungan terkumpul: Rp ${"%,.0f".format(ummuSavings)}
                - Total Tabungan Bersama Saat Ini: Rp ${"%,.0f".format(combinedSavings)}

                Berikan "Analisis, Saran Strategis, dan Pertimbangan" bagi Haikal & Ummu agar mereka dapat mencapai impian hidup bersama secara realistis dengan kondisi ini, menyikapi cicilan motor Haikal 550 ribu per bulan dengan baik.
                
                Struktur jawaban harus rapi, menyertakan:
                1. ANALISIS REALISTIS: Mengulas kondisi keuangan saat ini dari kacamata realistis namun positif.
                2. SARAN STRATEGIS CICILAN MOTOR HAIKAL: Langkah menyikapi cicilan motor 550 ribu selama 30 bulan ke depan agar tidak menghambat tabungan bersama.
                3. PLANNING & ROADMAP MENABUNG: Strategi menyisihkan uang dari nol untuk mencapai target pernikahan (Rp 15jt) dan sewa kontrakan (Rp 8jt).
                4. PERTIMBANGAN HUBUNGAN: Kata-kata pemantik semangat dan motivasi spiritual/emosional yang manis untuk pasangan berjuang dari nol agar tidak minder.

                Bahasa: Bahasa Indonesia yang santun, menyejukkan, memotivasi, dan terstruktur dengan poin-poin/bullet format agar mudah dibaca di layar HP.
            """.trimIndent()

            val promptToUse = if (!customQuestion.isNullOrBlank()) {
                "$defaultPrompt\n\nSelain itu, jawablah pertanyaan khusus berikut dari pengguna:\n\"$customQuestion\""
            } else {
                defaultPrompt
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = promptToUse))))
            )

            try {
                if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                    // Fallback to beautiful expert rule-based local suggestions if API Key is not set yet
                    val localAdvice = generateLocalAdvice(
                        totalGoalAmount,
                        combinedSavings,
                        haikalIncome,
                        ummuIncome,
                        customQuestion
                    )
                    _aiResponse.value = localAdvice
                } else {
                    val response = RetrofitClient.service.generateContent(key, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _aiResponse.value = resultText ?: "Maaf, sistem tidak dapat menghasilkan saran saat ini. Harap coba lagi."
                }
            } catch (e: Exception) {
                // Return beautiful local response as fallback in case of no network / timeout
                val localAdvice = generateLocalAdvice(
                    totalGoalAmount,
                    combinedSavings,
                    haikalIncome,
                    ummuIncome,
                    customQuestion
                )
                _aiResponse.value = "💡 [Koneksi Lokal] Kami menyusun saran praktis ini untuk Anda:\n\n$localAdvice"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun generateLocalAdvice(
        totalGoal: Double,
        combinedSavings: Double,
        haikalIncome: Double,
        ummuIncome: Double,
        customQuestion: String?
    ): String {
        return """
            🌸 **Analisis, Saran & Pertimbangan Langkah Bersama (Haikal & Ummu)** 🌸

            Kalian memulai perjuangan ini benar-benar dari nol ("belum punya apa-apa"), dan ini adalah awal yang sangat mulia! Banyak pasangan hebat terbentuk dari perjuangan bersama seperti ini. Mari kita bedah jalannya:

            📊 **1. Analisis Finansial Realistis**
            * **Total Target Bersama**: Rp ${"%,.0f".format(totalGoal)} (Terdiri dari Pernikahan, Sewa Kontrakan, Dana Darurat & Alat Rumah).
            * **Tabungan Terkumpul**: Rp ${"%,.0f".format(combinedSavings)} (Masih jauh dari target, tapi setiap langkah kecil berharga).
            * **Tantangan Utama**: Cicilan motor Haikal sebesar **Rp 550.000/bulan** selama **30 bulan** ke depan. Ini adalah pengeluaran tetap wajib yang harus diamankan setiap bulan.

            🛠️ **2. Saran Penanganan Cicilan Motor Haikal (Rp 550.000)**
            * **Amankan di Awal**: Haikal wajib memisahkan Rp 550.000 langsung setelah menerima pemasukan setiap bulannya. Jangan pernah memakai uang pos cicilan ini untuk urusan konsumtif.
            * **Gunakan Motor untuk Produktivitas**: Karena motor ini dicicil, manfaatkan secara optimal untuk menghemat biaya transportasi kerja atau bahkan mencari pemasukan tambahan (misal: ojek online paruh waktu, kurir instan) untuk mempercepat pelunasan atau memperbesar tabungan nikah.

            📈 **3. Roadmap & Planning Menabung dari Nol**
            * **Metode Tabungan Terpisah & Terbuka**: Meskipun saat ini masing-masing masih sendiri dan terpisah, buatlah rekening tabungan khusus "Wedding Fund". Haikal dan Ummu bisa berkomitmen menyetor jumlah yang disepakati bersama secara transparan setiap bulannya.
            * **Target Bertahap (Milestone)**: 
              1. **Fase 1**: Prioritaskan **Pernikahan Sederhana (Rp 15.000.000)**. Nikah di KUA pada hari kerja (Gratis!) dan buat akad sederhana dibatasi hanya keluarga inti. Ini memangkas biaya hingga 80%.
              2. **Fase 2**: Tabungan **Sewa Kontrakan (Rp 8.000.000)** setelah akad nikah sah. 
            * **Alokasi Harian**: Mengingat target harian di rutinitas adalah Rp 20.000/hari per orang, dalam sebulan terkumpul Rp 600.000 per orang atau Rp 1.200.000 berdua. Dalam 12-13 bulan, kalian bisa melampaui target pernikahan dasar!

            💞 **4. Pertimbangan & Motivasi Hubungan**
            * **Jangan Minder**: Belum punya apa-apa hari ini bukanlah aib. Membangun semuanya bersama dari nol akan memberi rasa memiliki dan ikatan batin yang sangat kuat di masa depan.
            * **Saling Terbuka & Mengapresiasi**: Selalu komunikasikan jika ada kendala darurat finansial. Saling mendukung dan tidak menuntut di luar batas kemampuan pasangan adalah kunci utama.

            ${if (!customQuestion.isNullOrBlank()) "\n💬 **Menjawab Pertanyaan Spesifik Anda:**\nUntuk pertanyaan *\"$customQuestion\"*, kami menyarankan Anda berdua untuk fokus pada transparansi keuangan harian, kurangi jajan kopi/kuliner luar sebesar 50%, dan buat celengan fisik/digital yang ditarik otomatis setiap gajian." else ""}
        """.trimIndent()
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
