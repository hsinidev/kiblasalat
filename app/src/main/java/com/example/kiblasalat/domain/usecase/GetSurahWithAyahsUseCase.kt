package com.example.kiblasalat.domain.usecase

import com.example.kiblasalat.domain.model.Ayah
import com.example.kiblasalat.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSurahWithAyahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    operator fun invoke(surahId: Int): Flow<List<Ayah>> {
        return repository.getAyahsForSurah(surahId)
    }
}
