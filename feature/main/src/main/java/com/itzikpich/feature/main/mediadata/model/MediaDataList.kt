package com.itzikpich.feature.main.mediadata.model

import androidx.compose.runtime.Immutable
import com.itzikpich.model.MediaData

@Immutable
internal data class MediaDataList(
    val mediaDataList: List<MediaData>
) : List<MediaData> by mediaDataList {
    companion object {
        val EMPTY = MediaDataList(emptyList())
    }
}
