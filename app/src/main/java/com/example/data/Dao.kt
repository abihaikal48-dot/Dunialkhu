package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY priority ASC, targetAmount DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<SavingsGoal>)

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_records ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transaction_records WHERE person = :person ORDER BY dateMillis DESC")
    fun getTransactionsByPerson(person: String): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(record: TransactionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(records: List<TransactionRecord>)

    @Delete
    suspend fun deleteTransaction(record: TransactionRecord)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM daily_schedules ORDER BY isCompleted ASC, id DESC")
    fun getAllSchedules(): Flow<List<DailySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DailySchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DailySchedule>)

    @Update
    suspend fun updateSchedule(schedule: DailySchedule)

    @Delete
    suspend fun deleteSchedule(schedule: DailySchedule)
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournals(entries: List<JournalEntry>)

    @Update
    suspend fun updateJournal(entry: JournalEntry)

    @Delete
    suspend fun deleteJournal(entry: JournalEntry)
}

