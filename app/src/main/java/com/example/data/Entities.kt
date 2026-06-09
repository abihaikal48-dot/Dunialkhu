package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentSavedAmount: Double = 0.0,
    val targetDateString: String, // format: "YYYY-MM" or "DD MMM YYYY"
    val category: String, // "Pernikahan", "Tempat Tinggal", "Dana Darurat", "Perangkat Rumah", "Lainnya"
    val priority: Int = 2 // 1: Tinggi, 2: Sedang, 3: Rendah
)

@Entity(tableName = "transaction_records")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val person: String, // "Haikal" or "Ummu"
    val type: String, // "INCOME" (Pendapatan), "EXPENSE" (Pengeluaran), "SAVING" (Tabungan Khusus)
    val amount: Double,
    val category: String, // "Gaji/Hasil Usaha", "Cicilan Motor", "Tabungan Bersama", "Transportasi", "Makan & Kebutuhan", "Lainnya"
    val description: String,
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_schedules")
data class DailySchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assignee: String, // "Haikal", "Ummu", "Bersama"
    val title: String,
    val description: String,
    val dateString: String, // e.g., "Setiap Hari", "9 Jun 2026", "Hari ini"
    val isCompleted: Boolean = false,
    val category: String = "Rencana" // "Persiapan", "Tabungan", "Riset", "Lainnya"
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val author: String, // "Haikal" or "Ummu" or "Bersama"
    val content: String, // Daily experience reflection
    val gratitude: String, // Things they are grateful for / reflections
    val mood: String = "Senang" // "Senang", "Syukur", "Optimis", "Tantangan"
)

