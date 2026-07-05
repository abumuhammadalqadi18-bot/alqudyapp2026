package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EmployeeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeEntity): Long

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Query("UPDATE employees SET status = 'INACTIVE' WHERE id = :id")
    suspend fun archiveEmployee(id: Long)

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE status = 'INACTIVE' ORDER BY name ASC")
    fun getArchivedEmployees(): Flow<List<EmployeeEntity>>
    
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Transaction
    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeWithDetails(id: Long): Flow<EmployeeWithDetails?>
}
