package com.itzikpich.feature.main.mediadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itzikpich.domain.CheckPermissionUseCase
import com.itzikpich.domain.MediaDataUseCase
import com.itzikpich.feature.main.mediaPermissions
import com.itzikpich.feature.main.mediadata.model.MediaDataList
import com.itzikpich.model.MediaData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state and logic related to media data.
 *
 * This ViewModel handles:
 * - Checking media permissions.
 * - Loading media data from the device.
 * - Providing the UI state to the view.
 *
 * @param mediaDataUseCase Use case for retrieving media data.
 * @param checkPermissionUseCase Use case for checking media permissions.
 */
@HiltViewModel
internal class MediaDataViewModel @Inject constructor(
    private val mediaDataUseCase: MediaDataUseCase,
    private val checkPermissionUseCase: CheckPermissionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        checkPermission()
    }

    private fun checkPermission() {
        when {
            checkPermissionUseCase(mediaPermissions) -> onPermissionGranted()
            else -> onPermissionDenied()
        }
    }

    internal fun onPermissionGranted() {
        loadMediaData()
    }

    internal fun onPermissionDenied() {
        _uiState.value = UIState.PermissionRequired
    }

    /**
     * Loads media data from the device using the [mediaDataUseCase].
     * Updates the UI state with loading progress and results.
     * Handles errors during data loading.
     */
    private fun loadMediaData() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            runCatching {
                val tempList = mutableListOf<MediaData>()
                mediaDataUseCase().buffer(capacity = 100).onCompletion {
                    if (_uiState.value is UIState.Loading) {
                        _uiState.update { UIState.NoDataFound }
                    }
                }.collect { mediaItem ->
                    tempList.add(mediaItem)
                    if (tempList.size == 20) {
                        val currentList =
                            (_uiState.value as? UIState.Success)?.mediaData ?: MediaDataList.EMPTY
                        _uiState.update { UIState.Success(MediaDataList(currentList + tempList)) }
                        tempList.clear()
                    }
                }
                // Add any remaining items
                if (tempList.isNotEmpty()) {
                    val currentList =
                        (_uiState.value as? UIState.Success)?.mediaData ?: MediaDataList.EMPTY
                    _uiState.value = UIState.Success(MediaDataList(currentList + tempList))
                }
            }.onFailure { error ->
                _uiState.value = UIState.Failed(error.message ?: "Unknown error occurred")
            }
        }
    }

    sealed interface UIState {
        data object Loading : UIState
        data object PermissionRequired : UIState
        data class Success(val mediaData: MediaDataList) : UIState
        data class Failed(val error: String) : UIState
        data object NoDataFound : UIState
    }

    fun retryLoading() {
        checkPermission()
    }

}