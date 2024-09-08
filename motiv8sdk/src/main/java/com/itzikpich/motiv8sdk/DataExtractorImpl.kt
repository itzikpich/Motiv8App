package com.itzikpich.motiv8sdk

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import com.itzikpich.motiv8sdk.common.Dispatcher
import com.itzikpich.motiv8sdk.common.Dispatchers
import com.itzikpich.motiv8sdk.model.ContactData
import com.itzikpich.motiv8sdk.model.DeviceInfo
import com.itzikpich.motiv8sdk.model.MediaMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val UNKNOWN = "Unknown"

/**
 * Implementation of the [DataExtractor] interface responsible for extracting device, media, and contact information.
 *
 * This class utilizes content resolvers and system services to gather data about the device,
 * including media files (images and videos) and contact details.
 *
 * @param context Application context.
 * @param ioDispatcher Coroutine dispatcher for performing I/O operations.
 */
class DataExtractorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(Dispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : DataExtractor {

    private val contentResolver: ContentResolver = context.contentResolver
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /**
     * Retrieves device information such as model, manufacturer, OS version, screen resolution, etc.
     *
     * @return [DeviceInfo] object containing device information.
     */
    override suspend fun getDeviceInfo(): DeviceInfo = withContext(ioDispatcher) {
        DeviceInfo(model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            brand = Build.BRAND,
            hardware = Build.HARDWARE,
            appVersion = runCatching {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            }.getOrElse { UNKNOWN },
            osVersion = System.getProperty("os.version") ?: UNKNOWN,
            screenResolution = getScreenResolution()
        )
    }

    private fun getScreenResolution(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            "${bounds.width()} x ${bounds.height()}"
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION") windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels}"
        }
    }

    /**
     * Retrieves metadata for media files (images and videos) on the device.
     *
     * @return Flow of [MediaMetadata] objects representing media file metadata.
     */
    override suspend fun getMediaMetadata(): Flow<MediaMetadata> = channelFlow {
        withContext(ioDispatcher) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.WIDTH,
                MediaStore.Files.FileColumns.HEIGHT,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATA
            )
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            val queryUri = MediaStore.Files.getContentUri("external")

            contentResolver.query(
                queryUri, projection, selection, selectionArgs, sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val duration = cursor.getLong(durationColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val data = cursor.getString(dataColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id
                    )
                    val mediaData = if (mimeType.startsWith("image")) {
                        MediaMetadata.ImageMetadata(
                            id = id,
                            name = name,
                            dateCreated = dateAdded,
                            size = size,
                            width = width,
                            height = height,
                            mimeType = mimeType,
                            path = contentUri.toString(),
                            data = data,
                        )
                    } else {
                        MediaMetadata.VideoMetadata(
                            id = id,
                            name = name,
                            dateCreated = dateAdded,
                            size = size,
                            width = width,
                            height = height,
                            mimeType = mimeType,
                            duration = duration,
                            path = contentUri.toString(),
                            data = data,
                        )
                    }
                    send(mediaData)
                }
            }
        }
    }

    /**
     * Retrieves contact data from the device's contacts.
     *
     * @return Flow of [ContactData] objects representing contact information.
     */
    override suspend fun getContactsData(): Flow<ContactData> = channelFlow {
        withContext(ioDispatcher) {
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )

            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val hasPhoneNumber =
                        cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    var phoneNumber: String? = null

                    if (hasPhoneNumber > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use {
                            if (it.moveToFirst()) {
                                phoneNumber =
                                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            }
                        }
                    }
                    send(ContactData(id, name, phoneNumber))
                }
            }
        }
    }

}