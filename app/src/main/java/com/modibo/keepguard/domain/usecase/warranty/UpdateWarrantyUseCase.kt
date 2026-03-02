package com.modibo.keepguard.domain.usecase.warranty

import com.modibo.keepguard.domain.model.Warranty
import com.modibo.keepguard.domain.repository.WarrantyRepository
import javax.inject.Inject

class UpdateWarrantyUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    operator fun invoke(warranty: Warranty) = repository.updateWarranty(warranty)
}