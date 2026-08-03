package com.example.kiblasalat.di

import android.content.Context
import androidx.room.Room
import com.example.kiblasalat.data.database.AyahDao
import com.example.kiblasalat.data.database.BookmarkDao
import com.example.kiblasalat.data.database.QuranDatabase
import com.example.kiblasalat.data.database.SettingDao
import com.example.kiblasalat.data.database.SurahDao
import com.example.kiblasalat.data.location.LocationProvider
import com.example.kiblasalat.data.location.LocationProviderImpl
import com.example.kiblasalat.data.repository.QuranRepositoryImpl
import com.example.kiblasalat.data.repository.SettingsRepositoryImpl
import com.example.kiblasalat.domain.repository.QuranRepository
import com.example.kiblasalat.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DIModules {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuranDatabase {
        return Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            "quran.db"
        )
        .createFromAsset("databases/quran.db")
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideSurahDao(db: QuranDatabase): SurahDao = db.surahDao()

    @Provides
    @Singleton
    fun provideAyahDao(db: QuranDatabase): AyahDao = db.ayahDao()

    @Provides
    @Singleton
    fun provideBookmarkDao(db: QuranDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideSettingDao(db: QuranDatabase): SettingDao = db.settingDao()

    @Provides
    @Singleton
    fun provideLocationProvider(impl: LocationProviderImpl): LocationProvider = impl

    @Provides
    @Singleton
    fun provideQuranRepository(impl: QuranRepositoryImpl): QuranRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
