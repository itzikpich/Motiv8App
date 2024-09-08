package com.itzikpich.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itzikpich.feature.main.model.Tab
import com.itzikpich.feature.main.model.TabsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val tabs = TabsList(
        listOf(
            Tab(0, "Device Data"),
            Tab(1, "Media"),
            Tab(2, "Contacts"),
        )
    )

    private val tabIndex = MutableStateFlow(INITIAL_TAB_INDEX)

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val uiState: Flow<UIState> =
        tabIndex.mapLatest { index ->
            UIState.Success(
                tabsIndex = index,
                tabs = tabs
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UIState.Loading
        )

    internal fun onTabClick(tab: Tab) =
        tabIndex.tryEmit(tabs.first { it.id == tab.id }.id)

    companion object {
        private const val INITIAL_TAB_INDEX = 0
    }
}

internal sealed interface UIState {
    data object Loading : UIState
    data object Failed : UIState
    data class Success(
        val tabsIndex: Int,
        val tabs: TabsList
    ) : UIState
}