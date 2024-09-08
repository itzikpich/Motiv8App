package com.itzikpich.feature.main.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itzikpich.feature.main.R
import com.itzikpich.feature.main.contacts.model.ContactsList
import com.itzikpich.feature.main.contactsPermission
import com.itzikpich.model.Contact

@Composable
internal fun ContactsScreen(
    modifier: Modifier = Modifier,
    contactsViewModel: ContactsViewModel = hiltViewModel(),
) {
    val uiState = contactsViewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) contactsViewModel.onPermissionGranted()
        else contactsViewModel.onPermissionDenied()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState.value) {
            is ContactsViewModel.UIState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is ContactsViewModel.UIState.PermissionRequired -> {
                PermissionRequest(
                    onRequestPermission = {
                        permissionLauncher.launch(contactsPermission)
                    }
                )
            }

            is ContactsViewModel.UIState.Success -> {
                Contacts(state.contacts)
            }

            is ContactsViewModel.UIState.Failed -> {
                ErrorState(
                    error = state.error,
                    onRetry = { contactsViewModel.retryLoading() }
                )
            }

            ContactsViewModel.UIState.NoDataFound -> {
                Text(
                    text = stringResource(R.string.no_data_found)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.contacts_permission_required)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun Contacts(contacts: ContactsList) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(contacts) { item ->
            ContactItem(contact = item)
            HorizontalDivider()
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier
                .padding(8.dp)
        ) {
            Text("Name: ${contact.name}")
            Text("Phone: ${contact.phoneNumber}")
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}
