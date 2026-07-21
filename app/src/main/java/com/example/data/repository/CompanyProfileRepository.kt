package com.example.data.repository

import com.example.data.local.dao.CompanyProfileDao
import com.example.data.local.entity.CompanyProfileEntity
import kotlinx.coroutines.flow.Flow

class CompanyProfileRepository(private val dao: CompanyProfileDao) {
    fun getProfileFlow(): Flow<CompanyProfileEntity?> = dao.getProfileFlow()
    suspend fun getProfile(): CompanyProfileEntity? = dao.getProfile()
    suspend fun saveProfile(profile: CompanyProfileEntity) = dao.saveProfile(profile)
}
