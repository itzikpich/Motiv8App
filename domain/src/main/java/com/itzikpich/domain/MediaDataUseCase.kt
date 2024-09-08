package com.itzikpich.domain

import com.itzikpich.model.MediaData
import com.itzikpich.motiv8sdk.DataExtractor
import com.itzikpich.motiv8sdk.model.MediaMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaDataUseCase @Inject constructor(
    private val dataExtractor: DataExtractor
) {

    suspend operator fun invoke(): Flow<MediaData> =
        dataExtractor.getMediaMetadata().map { media ->
            when (media) {
                is MediaMetadata.ImageMetadata -> media.toImageData()
                is MediaMetadata.VideoMetadata -> media.toVideoData()
            }
        }

    private fun MediaMetadata.ImageMetadata.toImageData(): MediaData.ImageData =
        MediaData.ImageData(
            fileName = this.name,
            dateCreated = this.dateCreated,
            fileSize = this.size,
            width = this.width,
            height = this.height,
            path = this.path
        )

    private fun MediaMetadata.VideoMetadata.toVideoData(): MediaData.VideoData =
        MediaData.VideoData(
            fileName = this.name,
            dateCreated = this.dateCreated,
            fileSize = this.size,
            width = this.width,
            height = this.height,
            path = this.path,
            duration = this.duration
        )

}
