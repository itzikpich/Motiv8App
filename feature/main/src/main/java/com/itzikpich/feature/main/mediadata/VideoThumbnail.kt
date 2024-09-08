package com.itzikpich.feature.main.mediadata

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.itzikpich.model.MediaData

@Composable
fun VideoThumbnail(videoData: MediaData.VideoData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val contentUri = remember(videoData.path) {
        Uri.parse(videoData.path)
    }
    val thumbnailBitmap = remember(contentUri) {
        getThumbnailBitmap(context, contentUri)
    }

    if (thumbnailBitmap != null) {
        Image(
            bitmap = thumbnailBitmap.asImageBitmap(),
            contentDescription = "Video Thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(videoData.path)
                .crossfade(true)
                .build(),
            contentDescription = "Video Thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun getThumbnailBitmap(context: Context, contentUri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(contentUri, Size(640, 480), null)
        } else {
            val projection = arrayOf(MediaStore.Video.Media._ID)
            context.contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver,
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                } else null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}