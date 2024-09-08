package com.itzikpich.motiv8sdk.model

sealed interface MediaMetadata {
    val id: Long
    val name: String
    val dateCreated: Long
    val size: Long
    val width: Int
    val height: Int
    val path: String
    val mimeType: String
    val data: String

    data class ImageMetadata(
        override val id: Long,
        override val name: String,
        override val dateCreated: Long,
        override val size: Long,
        override val width: Int,
        override val height: Int,
        override val path: String,
        override val mimeType: String,
        override val data: String,
    ) : MediaMetadata


    data class VideoMetadata(
        override val id: Long,
        override val name: String,
        override val dateCreated: Long,
        override val size: Long,
        override val width: Int,
        override val height: Int,
        override val path: String,
        override val mimeType: String,
        val duration: Long,
        override val data: String,
    ) : MediaMetadata
}