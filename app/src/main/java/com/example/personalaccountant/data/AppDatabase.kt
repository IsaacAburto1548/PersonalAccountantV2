package com.example.personalaccountant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Account::class, 
        Transaction::class, 
        FixedTransactionRule::class, 
        MonthlyPaymentInstance::class,
        CreditCardCharge::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun fixedTransactionDao(): FixedTransactionDao
    abstract fun creditCardChargeDao(): CreditCardChargeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 3 to 4: Add frequency fields
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to fixed_transaction_rules table
                database.execSQL(
                    "ALTER TABLE fixed_transaction_rules ADD COLUMN frequencyType TEXT NOT NULL DEFAULT 'MONTHLY'"
                )
                database.execSQL(
                    "ALTER TABLE fixed_transaction_rules ADD COLUMN intervalDays INTEGER"
                )
                database.execSQL(
                    "ALTER TABLE fixed_transaction_rules ADD COLUMN lastGeneratedDate INTEGER"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_accountant_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration() // Only if migration fails
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
