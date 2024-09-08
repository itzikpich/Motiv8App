package com.itzikpich.feature.main.devicedata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itzikpich.feature.main.R
import com.itzikpich.feature.main.components.Body
import com.itzikpich.feature.main.components.Title

@Composable
internal fun DeviceDataScreen(
    modifier: Modifier = Modifier,
    deviceDataViewModel: DeviceDataViewModel = hiltViewModel(),
) {

    val uiState by deviceDataViewModel.uiState.collectAsStateWithLifecycle(initialValue = DeviceDataViewModel.UIState.Loading)

    uiState.let { state ->
        when (state) {
            DeviceDataViewModel.UIState.Failed -> Box(modifier = modifier) {
                Text(text = stringResource(R.string.oops_something_went_wrong))
            }

            DeviceDataViewModel.UIState.Loading -> CircularProgressIndicator()
            is DeviceDataViewModel.UIState.Success -> {
                LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
                    item {
                        Title(text = "Device model")
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Body(text = state.deviceData.deviceModel)
                    }
                    item {
                        Title(text = "Device operating system version")
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Body(text = state.deviceData.osVersion)
                    }
                    item {
                        Title(text = "Device manufacturer")
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Body(text = state.deviceData.manufacturer)
                    }
                    item {
                        Title(text = "Device screen resolution")
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Body(text = state.deviceData.screenResolution)
                    }
                }
            }
        }
    }
}