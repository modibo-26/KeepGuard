package com.modibo.keepguard.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import com.modibo.keepguard.domain.repository.AuthRepository
import com.modibo.keepguard.domain.usecase.auth.ContinueWithGoogleUseCase
import com.modibo.keepguard.domain.usecase.auth.LinkAccountUseCase
import com.modibo.keepguard.domain.usecase.auth.SignInEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val user: User? = null,
    val email: String = "",
    val displayName: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val authSuccess: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signInEmail: SignInEmailUseCase,
    private val linkAccount: LinkAccountUseCase,
    private val continueWithGoogle: ContinueWithGoogleUseCase ,
    private val repository: AuthRepository,
): ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        loadUser()
    }

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value) }

    fun loadUser() {
        val user = repository.getCurrentUser()
        _state.value = _state.value.copy(user = user)
        _state.value = _state.value.copy(email = user?.email ?:"")
        _state.value = _state.value.copy(displayName = user?.displayName ?: "")
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            signInEmail(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(user = resource.data, authSuccess = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun linkWithEmail(email: String, password: String) {
        viewModelScope.launch {
            linkAccount(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(user = resource.data, authSuccess = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun logWithGoogle() {
        viewModelScope.launch {
            continueWithGoogle().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(user = resource.data, authSuccess = true, isLoading = false)
                    is Resource.Error -> _state.value = _state.value.copy(error = resource.message, isLoading = false)
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun clearAuthSuccess() { _state.value = _state.value.copy(authSuccess = false) }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
