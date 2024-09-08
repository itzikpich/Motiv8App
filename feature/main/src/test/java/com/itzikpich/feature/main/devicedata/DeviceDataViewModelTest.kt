package com.itzikpich.feature.main.devicedata

import com.itzikpich.domain.DeviceDataUseCase
import com.itzikpich.model.DeviceData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DeviceDataViewModelTest {

    private val deviceDataUseCase: DeviceDataUseCase = mockk()
    private lateinit var viewModel: DeviceDataViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `uiState emits correct states when use case succeeds`() = runTest {
        // Given
        val deviceData = DeviceData(
            deviceModel = "Test Model",
            manufacturer = "Test Manufacturer",
            osVersion = "Test OS",
            screenResolution = "1080x2400"
        )
        coEvery { deviceDataUseCase() } returns deviceData

        // When
        viewModel = DeviceDataViewModel(deviceDataUseCase)

        val states = mutableListOf<DeviceDataViewModel.UIState>()
        val job = launch {
            viewModel.uiState.toList(states)
        }

        // Advance time to allow all coroutines to complete
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(2, states.size)
        assertEquals(DeviceDataViewModel.UIState.Loading, states[0])
        assertEquals(DeviceDataViewModel.UIState.Success(deviceData), states[1])

        coVerify { deviceDataUseCase() }

        job.cancel()
    }

    @Test
    fun `uiState emits correct states when use case fails`() = runTest {
        // Given
        coEvery { deviceDataUseCase() } throws Exception("Test exception")

        // When
        viewModel = DeviceDataViewModel(deviceDataUseCase)

        val states = mutableListOf<DeviceDataViewModel.UIState>()
        val job = launch {
            viewModel.uiState.toList(states)
        }

        // Advance time to allow all coroutines to complete
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(2, states.size)
        assertEquals(DeviceDataViewModel.UIState.Loading, states[0])
        assert(states[1] is DeviceDataViewModel.UIState.Failed)

        coVerify { deviceDataUseCase() }

        job.cancel()
    }
}