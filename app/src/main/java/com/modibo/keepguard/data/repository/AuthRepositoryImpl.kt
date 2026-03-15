package com.modibo.keepguard.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.modibo.keepguard.BuildConfig
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import com.modibo.keepguard.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context
) : AuthRepository {
    override fun signInAnonymously(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInAnonymously().await()
            val firebaseUser = result.user!!
            val user = userFromFirebase(firebaseUser)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'authentification"))
        }
    }

    override fun signInWithEmail(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            val user = userFromFirebase(firebaseUser)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'authentification"))
        }
    }

    override fun linkWithEmail(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.linkWithCredential(credential)?.await()
            val firebaseUser = auth.currentUser!!
            val user = userFromFirebase(firebaseUser)
            emit(Resource.Success(user))
        } catch (e:  Exception) {
            emit(Resource.Error(e.message ?: "Erreur de liaison du compte"))
        }
    }

    override fun continueWithGoogle(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val idToken = getGoogleIdToken()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                auth.currentUser?.linkWithCredential(credential)?.await()
                val firebaseUser = auth.currentUser!!
                emit(Resource.Success(userFromFirebase(firebaseUser)))
            } catch (e: FirebaseAuthUserCollisionException) {
                val result = auth.signInWithCredential(credential).await()
                emit(Resource.Success(userFromFirebase(result.user!!)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur d'authentification"))
        }
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.let { userFromFirebase(it) }
    }

    override fun signOut() {
        auth.signOut()
    }

    private suspend fun getGoogleIdToken(): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        val idToken = GoogleIdTokenCredential
            .createFrom(result.credential.data)
            .idToken

        return idToken
    }

    private fun userFromFirebase (firebaseUser: FirebaseUser): User{
        val user = User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            isAnonymous = firebaseUser.isAnonymous,
            createdAt = System.currentTimeMillis()
        )
        return user
    }

}