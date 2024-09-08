package com.itzikpich.feature.main.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itzikpich.domain.CheckPermissionUseCase
import com.itzikpich.domain.GetContactsUseCase
import com.itzikpich.feature.main.contacts.model.ContactsList
import com.itzikpich.feature.main.contactsPermission
import com.itzikpich.model.Contact
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
 * @param getContactsUseCase Use case for retrieving contacts on the device.
 * @param checkPermissionUseCase Use case for checking contacts permissions.
 */
@HiltViewModel
internal class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val checkPermissionUseCase: CheckPermissionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        checkPermission()
    }

    private fun checkPermission() {
        when {
            checkPermissionUseCase(arrayOf(contactsPermission)) -> onPermissionGranted()
            else -> onPermissionDenied()
        }
    }

    internal fun onPermissionGranted() {
        loadContacts()
    }

    internal fun onPermissionDenied() {
        _uiState.value = UIState.PermissionRequired
    }

    /**
     * Loads contacts from the device using the [getContactsUseCase].
     * Updates the UI state with loading progress and results.
     * Handles errors during data loading.
     */
    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            runCatching {
                val tempList = mutableListOf<Contact>()
                getContactsUseCase()
                    .buffer(capacity = 100)
                    .onCompletion {
                        if (_uiState.value is UIState.Loading) {
                            _uiState.update { UIState.NoDataFound }
                        }
                    }
                    .collect { contact ->
                        tempList.add(contact)
                        if (tempList.size == 20) {
                            val currentList = (_uiState.value as? UIState.Success)?.contacts
                                ?: ContactsList.EMPTY
                            _uiState.update { UIState.Success(ContactsList(currentList + tempList)) }
                            tempList.clear()
                        }
                    }
                // Add any remaining items
                if (tempList.isNotEmpty()) {
                    val currentList = (_uiState.value as? UIState.Success)?.contacts
                        ?: ContactsList.EMPTY
                    _uiState.value = UIState.Success(ContactsList(currentList + tempList))
                }
            }.onFailure { error ->
                _uiState.value = UIState.Failed(error.message ?: "Unknown error occurred")
            }
        }
    }

    sealed interface UIState {
        data object Loading : UIState
        data object PermissionRequired : UIState
        data class Success(val contacts: ContactsList) : UIState
        data class Failed(val error: String) : UIState
        data object NoDataFound : UIState
    }

    fun retryLoading() {
        checkPermission()
    }
}