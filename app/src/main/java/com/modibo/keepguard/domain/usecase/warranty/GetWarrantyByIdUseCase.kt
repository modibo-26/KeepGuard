package com.modibo.keepguard.domain.usecase.warranty

import com.modibo.keepguard.domain.repository.WarrantyRepository
import javax.inject.Inject

class GetWarrantyByIdUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    operator fun invoke(warrantyId: String) = repository.getWarrantyById(warrantyId)
}