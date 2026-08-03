package com.example.kiblasalat.domain.usecase

import com.example.kiblasalat.domain.model.Surah
import com.example.kiblasalat.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuranSurahListUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    operator fun invoke(searchQuery: String = ""): Flow<List<Surah>> {
        return if (searchQuery.isBlank()) {
            repository.getAllSurahs()
        } else {
            repository.searchSurahs(searchQuery)
        }
    }
}
