package com.itzikpich.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itzikpich.feature.main.contacts.ContactsScreen
import com.itzikpich.feature.main.devicedata.DeviceDataScreen
import com.itzikpich.feature.main.mediadata.MediaDataScreen
import com.itzikpich.feature.main.model.Tab
import com.itzikpich.feature.main.model.TabsList

@Composable
internal fun MainRoute(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle(initialValue = UIState.Loading)
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        MainScreen(
            uiState = uiState,
            onTabClick = mainViewModel::onTabClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun MainScreen(
    uiState: UIState,
    onTabClick: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        UIState.Failed -> Box(modifier = modifier) {
            Text(text = stringResource(R.string.oops_something_went_wrong))
        }

        UIState.Loading -> CircularProgressIndicator()
        is UIState.Success -> MainSuccessScreen(
            modifier = modifier,
            tabs = uiState.tabs,
            tabIndex = uiState.tabsIndex,
            onTabClick = onTabClick,
            content = {
                val selectedTab = uiState.tabs.first {
                    it.id == uiState.tabsIndex
                }
                when (selectedTab.id) {
                    0 -> DeviceDataScreen(modifier = Modifier.fillMaxSize())
                    1 -> MediaDataScreen(modifier = Modifier.fillMaxSize())
                    2 -> ContactsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        )
    }
}

@Composable
private fun MainSuccessScreen(
    tabs: TabsList,
    tabIndex: Int,
    onTabClick: (Tab) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ScrollableTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = tabIndex,
            edgePadding = 4.dp,
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    modifier = Modifier.padding(4.dp),
                    selected = tabIndex == index,
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { onTabClick(tab) }) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
        content()
    }
}