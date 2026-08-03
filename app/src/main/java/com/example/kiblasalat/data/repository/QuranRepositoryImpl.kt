package com.example.kiblasalat.data.repository

import com.example.kiblasalat.data.database.AyahDao
import com.example.kiblasalat.data.database.BookmarkDao
import com.example.kiblasalat.data.database.BookmarkEntity
import com.example.kiblasalat.data.database.SurahDao
import com.example.kiblasalat.domain.model.Ayah
import com.example.kiblasalat.domain.model.Surah
import com.example.kiblasalat.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val bookmarkDao: BookmarkDao
) : QuranRepository {

    override fun getAllSurahs(): Flow<List<Surah>> {
        return combine(
            surahDao.getAllSurahs(),
            bookmarkDao.getAllBookmarks()
        ) { surahs, bookmarks ->
            val bookmarkedSurahs = bookmarks.map { it.surahId }.toSet()
            surahs.map { entity ->
                Surah(
                    id = entity.id,
                    name = entity.name,
                    englishName = entity.englishName,
                    englishNameTranslation = entity.englishNameTranslation,
                    revelationType = entity.revelationType,
                    totalAyahs = entity.totalAyahs,
                    isBookmarkedAny = bookmarkedSurahs.contains(entity.id)
                )
            }
        }
    }

    override fun getSurahById(id: Int): Flow<Surah?> {
        return surahDao.getSurahById(id).map { entity ->
            entity?.let {
                Surah(
                    id = it.id,
                    name = it.name,
                    englishName = it.englishName,
                    englishNameTranslation = it.englishNameTranslation,
                    revelationType = it.revelationType,
                    totalAyahs = it.totalAyahs
                )
            }
        }
    }

    override fun searchSurahs(query: String): Flow<List<Surah>> {
        val dbQuery = "%$query%"
        return combine(
            surahDao.searchSurahs(dbQuery),
            bookmarkDao.getAllBookmarks()
        ) { surahs, bookmarks ->
            val bookmarkedSurahs = bookmarks.map { it.surahId }.toSet()
            surahs.map { entity ->
                Surah(
                    id = entity.id,
                    name = entity.name,
                    englishName = entity.englishName,
                    englishNameTranslation = entity.englishNameTranslation,
                    revelationType = entity.revelationType,
                    totalAyahs = entity.totalAyahs,
                    isBookmarkedAny = bookmarkedSurahs.contains(entity.id)
                )
            }
        }
    }

    override fun getAyahsForSurah(surahId: Int): Flow<List<Ayah>> {
        return combine(
            ayahDao.getAyahsForSurah(surahId),
            bookmarkDao.getAllBookmarks()
        ) { ayahs, bookmarks ->
            val bookmarkedKeys = bookmarks.map { "${it.surahId}_${it.ayahNumber}" }.toSet()
            ayahs.map { entity ->
                Ayah(
                    id = entity.id,
                    surahId = entity.surahId,
                    numberInSurah = entity.numberInSurah,
                    textArabic = entity.textArabic,
                    textEnglish = entity.textEnglish,
                    juz = entity.juz,
                    isBookmarked = bookmarkedKeys.contains("${entity.surahId}_${entity.numberInSurah}")
                )
            }
        }
    }

    override fun searchAyahs(query: String): Flow<List<Ayah>> {
        val dbQuery = "%$query%"
        return combine(
            ayahDao.searchAyahs(dbQuery),
            bookmarkDao.getAllBookmarks()
        ) { ayahs, bookmarks ->
            val bookmarkedKeys = bookmarks.map { "${it.surahId}_${it.ayahNumber}" }.toSet()
            ayahs.map { entity ->
                Ayah(
                    id = entity.id,
                    surahId = entity.surahId,
                    numberInSurah = entity.numberInSurah,
                    textArabic = entity.textArabic,
                    textEnglish = entity.textEnglish,
                    juz = entity.juz,
                    isBookmarked = bookmarkedKeys.contains("${entity.surahId}_${entity.numberInSurah}")
                )
            }
        }
    }

    override fun getBookmarkedAyahs(): Flow<List<Ayah>> {
        return combine(
            ayahDao.getBookmarkedAyahs(),
            bookmarkDao.getAllBookmarks()
        ) { ayahs, bookmarks ->
            val bookmarkedKeys = bookmarks.map { "${it.surahId}_${it.ayahNumber}" }.toSet()
            ayahs.map { entity ->
                Ayah(
                    id = entity.id,
                    surahId = entity.surahId,
                    numberInSurah = entity.numberInSurah,
                    textArabic = entity.textArabic,
                    textEnglish = entity.textEnglish,
                    juz = entity.juz,
                    isBookmarked = bookmarkedKeys.contains("${entity.surahId}_${entity.numberInSurah}")
                )
            }
        }
    }

    override suspend fun toggleBookmark(surahId: Int, ayahNumber: Int) {
        val exists = bookmarkDao.isBookmarked(surahId, ayahNumber).first()
        if (exists) {
            bookmarkDao.deleteBookmark(surahId, ayahNumber)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    surahId = surahId,
                    ayahNumber = ayahNumber
                )
            )
        }
    }

    override fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(surahId, ayahNumber)
    }
}
