package com.modibo.keepguard.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.work.WorkManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.modibo.keepguard.BuildConfig
import com.modibo.keepguard.core.util.Constants.Collections
import com.modibo.keepguard.core.util.Constants.ErrorMessages
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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val manager: WorkManager,
    @param:ApplicationContext private val context: Context
) : AuthRepository {
    override fun signInAnonymously(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInAnonymously().await()
            val firebaseUser = result.user!!
            emit(Resource.Success(userFromFirebase(firebaseUser)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.AUTH_ERROR))
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
            emit(Resource.Success(userFromFirebase(firebaseUser)))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.AUTH_ERROR))
        }
    }

    override fun linkWithEmail(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val firebaseUser = auth.currentUser ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val credential = EmailAuthProvider.getCredential(email, password)
            firebaseUser.linkWithCredential(credential).await()
            emit(Resource.Success(userFromFirebase(firebaseUser)))
        } catch (e:  Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.LINK_ERROR))
        }
    }

    override fun continueWithGoogle(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val idToken = getGoogleIdToken()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val firebaseUser = auth.currentUser ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
                firebaseUser.linkWithCredential(credential).await()
                emit(Resource.Success(userFromFirebase(firebaseUser)))
            } catch (e: FirebaseAuthUserCollisionException) {
                val result = auth.signInWithCredential(credential).await()
                emit(Resource.Success(userFromFirebase(result.user!!)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.AUTH_ERROR))
        }
    }

    override fun reauthenticateWithEmail(
        email: String,
        password: String
    ): Flow<Resource<Unit>> = flow{
        emit(Resource.Loading())
        try {
            val user = auth.currentUser ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.REAUTH_ERROR))
        }
    }

    override fun reauthenticateWithGoogle(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val user = auth.currentUser ?: throw Exception(ErrorMessages.NOT_AUTHENTICATED)
            val idToken = getGoogleIdToken()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            user.reauthenticate(credential).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.REAUTH_ERROR))
        }
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.let { userFromFirebase(it) }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun deleteAccount(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val userId = getCurrentUser()?.id
            val collectDoc = firestore.collection(Collections.DOCUMENTS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val collectAsset = firestore.collection(Collections.ASSETS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val warranties = firestore.collection(Collections.WARRANTIES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val maintenances = firestore.collection(Collections.MAINTENANCES)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            collectDoc.documents.forEach { doc ->
                val fileUrl = doc.getString("fileUrl")
                if (!fileUrl.isNullOrEmpty()) {
                    storage.getReferenceFromUrl(fileUrl).delete().await()
                }
                firestore.collection(Collections.DOCUMENTS).document(doc.id).delete().await()
            }
            collectAsset.documents.forEach { doc ->
                val imageUrl = doc.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    storage.getReferenceFromUrl(imageUrl).delete().await()
                }
                firestore.collection(Collections.ASSETS).document(doc.id).delete().await()
            }
            warranties.documents.forEach { doc ->
                firestore.collection(Collections.WARRANTIES).document(doc.id).delete().await()
            }
            maintenances.documents.forEach { doc ->
                firestore.collection(Collections.MAINTENANCES).document(doc.id).delete().await()
            }
            manager.cancelAllWork()
            auth.currentUser?.delete()?.await()
            auth.signInAnonymously().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.deleteError("compte")))
        }
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
            providerId = firebaseUser.providerData.firstOrNull { it.providerId != "firebase" }?.providerId ?: "",
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            isAnonymous = firebaseUser.isAnonymous,
            createdAt = System.currentTimeMillis()
        )
        return user
    }

}