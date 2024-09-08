package com.itzikpich.motiv8sdk

import com.itzikpich.motiv8sdk.model.ContactData
import com.itzikpich.motiv8sdk.model.DeviceInfo
import com.itzikpich.motiv8sdk.model.MediaMetadata
import kotlinx.coroutines.flow.Flow

interface DataExtractor {
    suspend fun getDeviceInfo(): DeviceInfo
    suspend fun getMediaMetadata(): Flow<MediaMetadata>
    suspend fun getContactsData(): Flow<ContactData>
}