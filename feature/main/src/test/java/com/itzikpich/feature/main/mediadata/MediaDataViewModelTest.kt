package com.itzikpich.feature.main.mediadata

import com.itzikpich.domain.CheckPermissionUseCase
import com.itzikpich.domain.MediaDataUseCase
import com.itzikpich.feature.main.mediaPermissions
import com.itzikpich.feature.main.mediadata.model.MediaDataList
import com.itzikpich.feature.main.model.PermissionState
import com.itzikpich.model.MediaData
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class MediaDataViewModelTest {

    private lateinit var viewModel: MediaDataViewModel
    private lateinit var mediaDataUseCase: MediaDataUseCase
    private lateinit var checkPermissionUseCase: CheckPermissionUseCase
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mediaDataUseCase = mockk()
        checkPermissionUseCase = mockk()

        // Mock the invoke function of CheckPermissionUseCase
        every { checkPermissionUseCase.invoke(any()) } returns true

        viewModel = MediaDataViewModel(mediaDataUseCase, checkPermissionUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init should check permissions`() = runTest {
        // Given
        coEvery { mediaDataUseCase() } returns flowOf()

        // When
        MediaDataViewModel(mediaDataUseCase, checkPermissionUseCase)

        // Then
        verify { checkPermissionUseCase.invoke(mediaPermissions) }
    }

    @Test
    fun `onPermissionGranted should load media data`() = runTest {
        // Given
        val mediaData = mockk<MediaData>()
        coEvery { mediaDataUseCase() } returns flowOf(mediaData)

        // When
        viewModel.onPermissionGranted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is MediaDataViewModel.UIState.Success)
        val successState = viewModel.uiState.value as MediaDataViewModel.UIState.Success
        assertEquals(1, successState.mediaData.size)
    }

    @Test
    fun `onPermissionDenied should update UI state`() = runTest {
        // When
        viewModel.onPermissionDenied()

        // Then
        assertEquals(MediaDataViewModel.UIState.PermissionRequired, viewModel.uiState.value)
    }

    @Test
    fun `loadMediaData should handle errors`() = runTest {
        // Given
        coEvery { mediaDataUseCase() } throws RuntimeException("Test error")

        // When
        viewModel.onPermissionGranted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is MediaDataViewModel.UIState.Failed)
        val failedState = viewModel.uiState.value as MediaDataViewModel.UIState.Failed
        assertEquals("Test error", failedState.error)
    }

    @Test
    fun `retryLoading should check permissions again`() = runTest {
        // Given
        coEvery { mediaDataUseCase() } returns flowOf()

        // When
        viewModel.retryLoading()

        // Then
        verify { checkPermissionUseCase.invoke(mediaPermissions) }
    }
}