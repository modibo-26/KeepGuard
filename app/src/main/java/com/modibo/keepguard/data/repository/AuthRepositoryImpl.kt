package com.modibo.keepguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import com.modibo.keepguard.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override fun signInAnonymously(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = firebaseAuth.signInAnonymously().await()
            val firebaseUser = result.user!!
            val user = User(
                id = firebaseUser.uid,
                isAnonymous = true,
                createdAt = System.currentTimeMillis()
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'authentification"))
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let {
            User(
                id = it.uid,
                displayName = it.displayName ?: "",
                isAnonymous = it.isAnonymous,
            )
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

}