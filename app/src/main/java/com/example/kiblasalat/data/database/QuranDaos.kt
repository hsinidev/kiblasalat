package com.example.kiblasalat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surah ORDER BY id ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surah WHERE id = :id")
    fun getSurahById(id: Int): Flow<SurahEntity?>

    @Query("SELECT * FROM surah WHERE english_name LIKE :query OR name LIKE :query")
    fun searchSurahs(query: String): Flow<List<SurahEntity>>
}

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayah WHERE surah_id = :surahId ORDER BY number_in_surah ASC")
    fun getAyahsForSurah(surahId: Int): Flow<List<AyahEntity>>

    @Query("SELECT COUNT(*) FROM ayah")
    suspend fun getAyahCount(): Int

    @Query("SELECT * FROM ayah WHERE text_english LIKE :query OR text_arabic LIKE :query")
    fun searchAyahs(query: String): Flow<List<AyahEntity>>

    @Query("SELECT a.* FROM ayah a INNER JOIN bookmarks b ON a.surah_id = b.surah_id AND a.number_in_surah = b.ayah_number ORDER BY b.created_at DESC")
    fun getBookmarkedAyahs(): Flow<List<AyahEntity>>
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY created_at DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surah_id = :surahId AND ayah_number = :ayahNumber")
    suspend fun deleteBookmark(surahId: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surah_id = :surahId AND ayah_number = :ayahNumber)")
    fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean>
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<SettingEntity?>

    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingEntity)
}
