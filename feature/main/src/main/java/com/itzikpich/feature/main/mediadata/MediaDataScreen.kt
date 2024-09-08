package com.itzikpich.feature.main.mediadata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.itzikpich.feature.main.R
import com.itzikpich.feature.main.mediaPermissions
import com.itzikpich.feature.main.mediadata.model.MediaDataList
import com.itzikpich.model.MediaData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MediaDataScreen(
    modifier: Modifier = Modifier,
    mediaDataViewModel: MediaDataViewModel = hiltViewModel(),
) {
    val uiState by mediaDataViewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        if (result.values.any { it }) {
            mediaDataViewModel.onPermissionGranted()
        } else {
            mediaDataViewModel.onPermissionDenied()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is MediaDataViewModel.UIState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is MediaDataViewModel.UIState.PermissionRequired -> {
                PermissionRequest(
                    onRequestPermission = {
                        permissionLauncher.launch(mediaPermissions)
                    }
                )
            }

            is MediaDataViewModel.UIState.Success -> {
                val mediaData = (uiState as MediaDataViewModel.UIState.Success).mediaData
                MediaList(media = mediaData)
            }

            is MediaDataViewModel.UIState.Failed -> {
                val error = (uiState as MediaDataViewModel.UIState.Failed).error
                ErrorState(error = error, onRetry = { mediaDataViewModel.retryLoading() })
            }

            MediaDataViewModel.UIState.NoDataFound -> {
                Text(
                    text = stringResource(R.string.no_data_found)
                )
            }
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

@Composable
private fun MediaList(media: MediaDataList) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(media) { item ->
            when (item) {
                is MediaData.ImageData -> ImageItem(imageData = item)
                is MediaData.VideoData -> VideoItem(videoData = item)
            }
            HorizontalDivider()
        }
    }
}


@Composable
fun ImageItem(
    imageData: MediaData.ImageData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = imageData.path,
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Text("File Name: ${imageData.fileName}")
            Text("Date Created: ${formatDate(imageData.dateCreated)}")
            Text("File Size: ${imageData.fileSize} bytes")
            Text("Dimensions: ${imageData.width} x ${imageData.height}")
        }
    }
}

@Composable
fun VideoItem(videoData: MediaData.VideoData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                VideoThumbnail(
                    videoData = videoData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .size(60.dp),
                    contentScale = ContentScale.Fit,
                    painter = painterResource(id = R.drawable.baseline_video_file_24),
                    contentDescription = "null"
                )
            }
            Text("File Name: ${videoData.fileName}")
            Text("Date Created: ${formatDate(videoData.dateCreated)}")
            Text("File Size: ${videoData.fileSize} bytes")
            Text("Dimensions: ${videoData.width} x ${videoData.height}")
            Text("Duration: ${formatDuration(videoData.duration)}")
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
            text = stringResource(R.string.media_permission_required)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val date = Date(timestamp * 1000)
    return sdf.format(date)
}

private fun formatDuration(duration: Long): String {
    val seconds = duration / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
}