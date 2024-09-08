package com.itzikpich.feature.main.devicedata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itzikpich.domain.DeviceDataUseCase
import com.itzikpich.model.DeviceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state and logic related to media data.
 *
 * This ViewModel handles:
 * - Loading device data.
 * - Providing the UI state to the view.
 *
 * @param deviceDataUseCase Use case for retrieving device data.
 */
@HiltViewModel
class DeviceDataViewModel @Inject constructor(
    deviceDataUseCase: DeviceDataUseCase
) : ViewModel() {

    internal val uiState: StateFlow<UIState> = flow {
        emit(UIState.Loading)
        runCatching {
            val result = deviceDataUseCase()
            emit(UIState.Success(result))
        }.onFailure {
            emit(UIState.Failed)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UIState.Loading
    )

    sealed interface UIState {
        data object Loading : UIState
        data class Success(
            val deviceData: DeviceData
        ) : UIState

        data object Failed : UIState
    }

}