package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converter.Converters
import com.example.data.local.dao.AdjustmentDao
import com.example.data.local.dao.BackupHistoryDao
import com.example.data.local.dao.EmployeeDao
import com.example.data.local.dao.SettingDao
import com.example.data.local.dao.WageRateHistoryDao
import com.example.data.local.dao.WageRecordDao
import com.example.data.local.dao.WithdrawalDao
import com.example.data.local.entity.AdjustmentEntity
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.WageRateHistoryEntity
import com.example.data.local.entity.WageRecordEntity
import com.example.data.local.entity.WithdrawalEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        EmployeeEntity::class,
        WageRateHistoryEntity::class,
        WageRecordEntity::class,
        WithdrawalEntity::class,
        AdjustmentEntity::class,
        BackupHistoryEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao
    abstract fun wageRateHistoryDao(): WageRateHistoryDao
    abstract fun wageRecordDao(): WageRecordDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun adjustmentDao(): AdjustmentDao
    abstract fun backupHistoryDao(): BackupHistoryDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alqadi_database.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
