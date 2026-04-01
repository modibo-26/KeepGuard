package com.modibo.keepguard.presentation.screen.settings

import com.modibo.keepguard.core.util.Resource
import com.modibo.keepguard.domain.model.User
import com.modibo.keepguard.domain.usecase.auth.ContinueWithGoogleUseCase
import com.modibo.keepguard.domain.usecase.auth.DeleteAccountUseCase
import com.modibo.keepguard.domain.usecase.auth.GetCurrentUserUseCase
import com.modibo.keepguard.domain.usecase.auth.LinkAccountUseCase
import com.modibo.keepguard.domain.usecase.auth.ReauthWithEmailUseCase
import com.modibo.keepguard.domain.usecase.auth.ReauthWithGoogleUseCase
import com.modibo.keepguard.domain.usecase.auth.SignInEmailUseCase
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
class SettingsViewModelTest {

    private lateinit var signInEmail: SignInEmailUseCase
    private lateinit var linkAccount: LinkAccountUseCase
    private lateinit var continueWithGoogle: ContinueWithGoogleUseCase
    private lateinit var getCurrentUser: GetCurrentUserUseCase
    private lateinit var reauthWithEmail: ReauthWithEmailUseCase
    private lateinit var reauthWithGoogle: ReauthWithGoogleUseCase
    private lateinit var deleteAccount: DeleteAccountUseCase
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        signInEmail = mockk()
        linkAccount = mockk()
        continueWithGoogle = mockk()
        getCurrentUser = mockk()
        reauthWithEmail = mockk()
        reauthWithGoogle = mockk()
        deleteAccount = mockk()

        every { getCurrentUser() } returns null
        viewModel = SettingsViewModel(signInEmail, linkAccount, continueWithGoogle, getCurrentUser, reauthWithEmail, reauthWithGoogle, deleteAccount)
    }

    // ==================== Champs simples ====================

    @Test
    fun `onEmailChange updates email`() {
        viewModel.onEmailChange("test@test.com")
        assertEquals("test@test.com", viewModel.state.value.email)
    }

    @Test
    fun `onPasswordChange updates password`() {
        viewModel.onPasswordChange("secret")
        assertEquals("secret", viewModel.state.value.password)
    }

    // ==================== loadUser ====================

    @Test
    fun `loadUser fills user from getCurrentUser`() {
        val user = User(id = "123", email = "test@test.com", displayName = "Modibo")
        every { getCurrentUser() } returns user
        viewModel.loadUser()
        assertEquals("test@test.com", viewModel.state.value.email)
        assertEquals("Modibo", viewModel.state.value.displayName)
        assertEquals(user, viewModel.state.value.user)
    }

    @Test
    fun `loadUser handles null user`() {
        every { getCurrentUser() } returns null
        viewModel.loadUser()
        assertEquals(null, viewModel.state.value.user)
        assertEquals("", viewModel.state.value.email)
    }

    // ==================== signInWithEmail ====================

    @Test
    fun `signInWithEmail sets authSuccess true after Success`() = runTest {
        val user = User(id = "123", email = "test@test.com")
        every { signInEmail(any(), any()) } returns flowOf(Resource.Success(user))
        viewModel.signInWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.authSuccess)
        assertEquals(user, viewModel.state.value.user)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `signInWithEmail sets error after Error`() = runTest {
        every { signInEmail(any(), any()) } returns flowOf(Resource.Error("auth error"))
        viewModel.signInWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals("auth error", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // ==================== linkWithEmail ====================

    @Test
    fun `linkWithEmail sets authSuccess true after Success`() = runTest {
        val user = User(id = "123", email = "test@test.com")
        every { linkAccount(any(), any()) } returns flowOf(Resource.Success(user))
        viewModel.linkWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.authSuccess)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `linkWithEmail sets error after Error`() = runTest {
        every { linkAccount(any(), any()) } returns flowOf(Resource.Error("link error"))
        viewModel.linkWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals("link error", viewModel.state.value.error)
    }

    // ==================== logWithGoogle ====================

    @Test
    fun `logWithGoogle sets authSuccess true after Success`() = runTest {
        val user = User(id = "123")
        every { continueWithGoogle() } returns flowOf(Resource.Success(user))
        viewModel.logWithGoogle()
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.authSuccess)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `logWithGoogle sets error after Error`() = runTest {
        every { continueWithGoogle() } returns flowOf(Resource.Error("google error"))
        viewModel.logWithGoogle()
        advanceUntilIdle()
        assertEquals("google error", viewModel.state.value.error)
    }

    // ==================== clearAuthSuccess / clearError ====================

    @Test
    fun `clearAuthSuccess resets authSuccess`() = runTest {
        val user = User(id = "123")
        every { signInEmail(any(), any()) } returns flowOf(Resource.Success(user))
        viewModel.signInWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals(true, viewModel.state.value.authSuccess)

        viewModel.clearAuthSuccess()
        assertEquals(false, viewModel.state.value.authSuccess)
    }

    @Test
    fun `clearError resets error`() = runTest {
        every { signInEmail(any(), any()) } returns flowOf(Resource.Error("error"))
        viewModel.signInWithEmail("test@test.com", "password")
        advanceUntilIdle()
        assertEquals("error", viewModel.state.value.error)

        viewModel.clearError()
        assertEquals(null, viewModel.state.value.error)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
