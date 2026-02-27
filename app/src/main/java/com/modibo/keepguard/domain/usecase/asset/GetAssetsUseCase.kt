package com.modibo.keepguard.domain.usecase.asset

import com.modibo.keepguard.domain.repository.AssetRepository
import javax.inject.Inject

class GetAssetsUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    operator fun invoke(userId: String) = repository.getAssets(userId)
}