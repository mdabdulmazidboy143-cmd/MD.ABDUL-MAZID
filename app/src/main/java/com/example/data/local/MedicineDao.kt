package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getActiveMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forMorning = 1")
    fun getMorningMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forNoon = 1")
    fun getNoonMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forEvening = 1")
    fun getEveningMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forMorning = 1")
    suspend fun getMorningMedicinesList(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forNoon = 1")
    suspend fun getNoonMedicinesList(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE isActive = 1 AND forEvening = 1")
    suspend fun getEveningMedicinesList(): List<MedicineEntity>

    @Query("SELECT COUNT(*) FROM medicines")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteById(id: Long)
}
