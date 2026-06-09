package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val savingsGoalDao: SavingsGoalDao,
    private val transactionDao: TransactionDao,
    private val scheduleDao: ScheduleDao,
    private val journalEntryDao: JournalEntryDao
) {
    val allGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    val allTransactions: Flow<List<TransactionRecord>> = transactionDao.getAllTransactions()
    val allSchedules: Flow<List<DailySchedule>> = scheduleDao.getAllSchedules()
    val allJournalEntries: Flow<List<JournalEntry>> = journalEntryDao.getAllJournalEntries()

    fun getTransactionsByPerson(person: String): Flow<List<TransactionRecord>> {
        return transactionDao.getTransactionsByPerson(person)
    }

    suspend fun insertJournal(entry: JournalEntry) {
        journalEntryDao.insertJournal(entry)
    }

    suspend fun insertJournals(entries: List<JournalEntry>) {
        journalEntryDao.insertJournals(entries)
    }

    suspend fun updateJournal(entry: JournalEntry) {
        journalEntryDao.updateJournal(entry)
    }

    suspend fun deleteJournal(entry: JournalEntry) {
        journalEntryDao.deleteJournal(entry)
    }


    suspend fun insertGoal(goal: SavingsGoal) {
        savingsGoalDao.insertGoal(goal)
    }

    suspend fun insertGoals(goals: List<SavingsGoal>) {
        savingsGoalDao.insertGoals(goals)
    }

    suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun insertTransaction(record: TransactionRecord) {
        transactionDao.insertTransaction(record)
    }

    suspend fun insertTransactions(records: List<TransactionRecord>) {
        transactionDao.insertTransactions(records)
    }

    suspend fun deleteTransaction(record: TransactionRecord) {
        transactionDao.deleteTransaction(record)
    }

    suspend fun insertSchedule(schedule: DailySchedule) {
        scheduleDao.insertSchedule(schedule)
    }

    suspend fun insertSchedules(schedules: List<DailySchedule>) {
        scheduleDao.insertSchedules(schedules)
    }

    suspend fun updateSchedule(schedule: DailySchedule) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: DailySchedule) {
        scheduleDao.deleteSchedule(schedule)
    }
}
