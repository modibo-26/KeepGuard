package com.modibo.keepguard.domain.usecase.auth

import com.modibo.keepguard.domain.repository.AuthRepository
import javax.inject.Inject

class ReauthWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String) = repository.reauthenticateWithEmail(email, password)
}