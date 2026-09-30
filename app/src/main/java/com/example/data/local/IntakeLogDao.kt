package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {
    @Query("SELECT * FROM intake_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE date = :date AND slotId = :slotId LIMIT 1")
    suspend fun getLogForDateAndSlot(date: String, slotId: String): IntakeLogEntity?

    @Query("SELECT * FROM intake_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<IntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: IntakeLogEntity): Long

    @Query("UPDATE intake_logs SET status = :status, timestamp = :timestamp, notes = :notes WHERE date = :date AND slotId = :slotId")
    suspend fun updateStatus(date: String, slotId: String, status: String, timestamp: Long, notes: String)

    @Query("SELECT COUNT(*) FROM intake_logs WHERE status = 'TAKEN'")
    fun getTotalTakenCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM intake_logs WHERE status = 'MISSED'")
    fun getTotalMissedCount(): Flow<Int>

    @Query("DELETE FROM intake_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM intake_logs")
    suspend fun clearAllLogs()
}
