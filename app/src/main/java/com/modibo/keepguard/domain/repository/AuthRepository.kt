package com.modibo.keepguard.domain.repository

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signInAnonymously(): Flow<Resource<User>>
    fun signInWithEmail(email: String, password: String): Flow<Resource<User>>
    fun linkWithEmail(email: String, password: String): Flow<Resource<User>>
    fun continueWithGoogle(): Flow<Resource<User>>
    fun reauthenticateWithEmail(email: String, password: String): Flow<Resource<Unit>>
    fun reauthenticateWithGoogle(): Flow<Resource<Unit>>
    fun getCurrentUser(): User?
    fun signOut()
    fun deleteAccount() : Flow<Resource<Unit>>
}