package com.example.kiblasalat.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "english_name") val englishName: String,
    @ColumnInfo(name = "english_name_translation") val englishNameTranslation: String,
    @ColumnInfo(name = "revelation_type") val revelationType: String,
    @ColumnInfo(name = "total_ayahs") val totalAyahs: Int
)

@Entity(
    tableName = "ayah",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["id"],
            childColumns = ["surah_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["surah_id"])]
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "number_in_surah") val numberInSurah: Int,
    @ColumnInfo(name = "text_arabic") val textArabic: String,
    @ColumnInfo(name = "text_english") val textEnglish: String,
    val juz: Int
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["id"],
            childColumns = ["surah_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["surah_id"])]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "ayah_number") val ayahNumber: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
