package com.modibo.keepguard.domain.usecase.auth

import com.modibo.keepguard.domain.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.signInAnonymously()
}