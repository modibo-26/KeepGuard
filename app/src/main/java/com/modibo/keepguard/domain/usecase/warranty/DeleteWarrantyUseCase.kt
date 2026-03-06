package com.modibo.keepguard.domain.usecase.warranty

import com.modibo.keepguard.domain.repository.WarrantyRepository
import javax.inject.Inject

class DeleteWarrantyUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    operator fun invoke(warrantyId: String) = repository.deleteWarranty(warrantyId)
}