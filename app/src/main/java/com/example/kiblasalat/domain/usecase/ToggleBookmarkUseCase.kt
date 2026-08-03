package com.example.kiblasalat.domain.usecase

import com.example.kiblasalat.domain.repository.QuranRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend operator fun invoke(surahId: Int, ayahNumber: Int) {
        repository.toggleBookmark(surahId, ayahNumber)
    }
}
