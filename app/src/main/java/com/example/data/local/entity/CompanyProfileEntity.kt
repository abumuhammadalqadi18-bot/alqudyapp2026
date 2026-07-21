package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_profile")
data class CompanyProfileEntity(
    @PrimaryKey val id: Int = 1,
    val companyName: String,
    val phoneNumbers: String,
    val address: String,
    val services: String,
    val logoUri: String?,
    val sealUri: String?,
    val signatureUri: String?,
    val footerNote: String
)
