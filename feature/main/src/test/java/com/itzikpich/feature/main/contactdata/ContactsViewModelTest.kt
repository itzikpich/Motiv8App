package com.itzikpich.feature.main.contactdata


import com.itzikpich.domain.CheckPermissionUseCase
import com.itzikpich.domain.GetContactsUseCase
import com.itzikpich.feature.main.contacts.ContactsViewModel
import com.itzikpich.feature.main.contactsPermission
import com.itzikpich.model.Contact
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ContactsViewModelTest {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var getContactsUseCase: GetContactsUseCase
    private lateinit var checkPermissionUseCase: CheckPermissionUseCase
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getContactsUseCase = mockk()
        checkPermissionUseCase = mockk()

        // Mock the invoke function of CheckPermissionUseCase
        every { checkPermissionUseCase.invoke(any()) } returns true

        viewModel = ContactsViewModel(getContactsUseCase, checkPermissionUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init should check permissions`() = runTest {
        // Given
        coEvery { getContactsUseCase() } returns flowOf()

        // When
        ContactsViewModel(getContactsUseCase, checkPermissionUseCase)

        // Then
        verify { checkPermissionUseCase.invoke(arrayOf(contactsPermission)) }
    }

    @Test
    fun `onPermissionGranted should load contacts`() = runTest {
        // Given
        val contact = Contact("1", "John Doe", "1234567890")
        coEvery { getContactsUseCase() } returns flowOf(contact)

        // When
        viewModel.onPermissionGranted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is ContactsViewModel.UIState.Success)
        val successState = viewModel.uiState.value as ContactsViewModel.UIState.Success
        assertEquals(1, successState.contacts.size)
    }

    @Test
    fun `onPermissionDenied should update UI state`() = runTest {
        // When
        viewModel.onPermissionDenied()

        // Then
        assertTrue(viewModel.uiState.value is ContactsViewModel.UIState.PermissionRequired)
    }

    @Test
    fun `loadContacts should handle errors`() = runTest {
        // Given
        coEvery { getContactsUseCase() } throws RuntimeException("Test error")

        // When
        viewModel.onPermissionGranted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is ContactsViewModel.UIState.Failed)
        val failedState = viewModel.uiState.value as ContactsViewModel.UIState.Failed
        assertEquals("Test error", failedState.error)
    }

    @Test
    fun `loadContacts should update UI state in batches`() = runTest {
        // Given
        val contacts = List(25) { Contact("1", "Name$it", "Phone$it") }
        coEvery { getContactsUseCase() } returns flowOf(*contacts.toTypedArray())

        // When
        viewModel.onPermissionGranted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is ContactsViewModel.UIState.Success)
        val successState = viewModel.uiState.value as ContactsViewModel.UIState.Success
        assertEquals(25, successState.contacts.size)
    }

    @Test
    fun `retryLoading should check permissions again`() = runTest {
        // Given
        coEvery { getContactsUseCase() } returns flowOf()

        // When
        viewModel.retryLoading()
        advanceUntilIdle()

        // Then
        verify { checkPermissionUseCase.invoke(arrayOf(contactsPermission)) }
    }

    @Test
    fun `initial state should be Loading`() {
        // Then
        assertTrue(viewModel.uiState.value is ContactsViewModel.UIState.Loading)
    }
}