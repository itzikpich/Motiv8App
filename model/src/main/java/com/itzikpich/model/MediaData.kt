package com.itzikpich.model

sealed interface MediaData {
    val fileName: String
    val dateCreated: Long
    val fileSize: Long
    val width: Int
    val height: Int
    val path: String

    data class ImageData(
        override val fileName: String,
        override val dateCreated: Long,
        override val fileSize: Long,
        override val width: Int,
        override val height: Int,
        override val path: String,
    ) : MediaData

    data class VideoData(
        override val fileName: String,
        override val dateCreated: Long,
        override val fileSize: Long,
        override val width: Int,
        override val height: Int,
        override val path: String,
        val duration: Long,
    ) : MediaData
}
