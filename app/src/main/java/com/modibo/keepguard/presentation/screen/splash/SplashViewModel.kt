package com.modibo.keepguard.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import com.modibo.keepguard.domain.usecase.auth.SignInAnonymouslyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        val currentUser = getCurrentUserUseCase()
        if (currentUser != null) {
            _isReady.value = true
        } else {
            signInAnonymouslyUseCase().onEach { result ->
                when(result) {
                    is Resource.Success -> _isReady.value = true
                    is Resource.Error -> _isReady.value = false
                    is Resource.Loading<*> -> { }
                }
            }.launchIn(viewModelScope)
        }
    }
}