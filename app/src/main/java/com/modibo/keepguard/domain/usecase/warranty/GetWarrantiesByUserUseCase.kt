package com.modibo.keepguard.domain.usecase.warranty

import com.modibo.keepguard.domain.repository.WarrantyRepository
import javax.inject.Inject

class GetWarrantiesByUserUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    operator fun invoke(userId: String) = repository.getWarrantiesByUser(userId)
}