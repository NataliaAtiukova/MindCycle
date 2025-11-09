package com.app.mindcycle.di

import android.content.Context
import androidx.room.Room
import com.app.mindcycle.data.db.MoodDatabase
import com.app.mindcycle.data.db.MoodEntryDao
import com.app.mindcycle.data.forecast.CycleForecastCalculator
import com.app.mindcycle.data.preferences.UserPreferencesManager
import com.app.mindcycle.data.repository.CycleRepository
import com.app.mindcycle.reminders.ReminderScheduler
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
    fun provideDatabase(@ApplicationContext context: Context): MoodDatabase =
        Room.databaseBuilder(
            context,
            MoodDatabase::class.java,
            "mind_cycle_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMoodEntryDao(database: MoodDatabase): MoodEntryDao = database.moodEntryDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferencesManager =
        UserPreferencesManager(context)

    @Provides
    @Singleton
    fun provideCycleForecastCalculator(): CycleForecastCalculator = CycleForecastCalculator()

    @Provides
    @Singleton
    fun provideCycleRepository(
        dao: MoodEntryDao,
        preferencesManager: UserPreferencesManager,
        calculator: CycleForecastCalculator
    ): CycleRepository = CycleRepository(dao, preferencesManager, calculator)

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context): ReminderScheduler =
        ReminderScheduler(context)
}
