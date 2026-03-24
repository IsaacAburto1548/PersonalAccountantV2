package com.example.personalaccountant.di

import android.content.Context
import com.example.personalaccountant.data.AppDatabase
import com.example.personalaccountant.data.prefs.PreferenceManager
import com.example.personalaccountant.data.repository.FinanceRepository
import com.example.personalaccountant.utils.CsvExporter
import com.example.personalaccountant.utils.PdfExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun providePdfExporter(@ApplicationContext context: Context): PdfExporter {
        return PdfExporter(context)
    }

    @Provides
    @Singleton
    fun provideCsvExporter(@ApplicationContext context: Context): CsvExporter {
        return CsvExporter(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideFinanceRepository(database: AppDatabase): FinanceRepository {
        return FinanceRepository(database)
    }
}
