package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotSettingDao {
    @Query("SELECT * FROM slot_settings")
    fun getAllSettings(): Flow<List<SlotSettingEntity>>

    @Query("SELECT * FROM slot_settings WHERE slotId = :slotId LIMIT 1")
    suspend fun getSetting(slotId: String): SlotSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: SlotSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<SlotSettingEntity>)
}
