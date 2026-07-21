package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CompanyProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyProfileDao {
    @Query("SELECT * FROM company_profile WHERE id = 1")
    fun getProfileFlow(): Flow<CompanyProfileEntity?>

    @Query("SELECT * FROM company_profile WHERE id = 1")
    suspend fun getProfile(): CompanyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: CompanyProfileEntity)
}
