package com.modibo.keepguard.presentation.screen.splash

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import com.modibo.keepguard.domain.usecase.auth.SignInAnonymouslyUseCase
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private lateinit var signInAnonymously: SignInAnonymouslyUseCase
    private lateinit var getCurrentUser: GetCurrentUserUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        signInAnonymously = mockk()
        getCurrentUser = mockk()
    }

    // ==================== init ====================

    @Test
    fun `isReady true when user already exists`() = runTest {
        every { getCurrentUser() } returns User(id = "123")
        val viewModel = SplashViewModel(signInAnonymously, getCurrentUser)
        advanceUntilIdle()
        assertEquals(true, viewModel.isReady.value)
    }

    @Test
    fun `isReady true after anonymous sign in Success`() = runTest {
        every { getCurrentUser() } returns null
        every { signInAnonymously() } returns flowOf(Resource.Success(User(id = "anon")))
        val viewModel = SplashViewModel(signInAnonymously, getCurrentUser)
        advanceUntilIdle()
        assertEquals(true, viewModel.isReady.value)
    }

    @Test
    fun `isReady false after anonymous sign in Error`() = runTest {
        every { getCurrentUser() } returns null
        every { signInAnonymously() } returns flowOf(Resource.Error("auth error"))
        val viewModel = SplashViewModel(signInAnonymously, getCurrentUser)
        advanceUntilIdle()
        assertEquals(false, viewModel.isReady.value)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
