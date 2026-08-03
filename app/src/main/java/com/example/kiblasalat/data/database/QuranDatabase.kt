package com.example.kiblasalat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        BookmarkEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun settingDao(): SettingDao
}
