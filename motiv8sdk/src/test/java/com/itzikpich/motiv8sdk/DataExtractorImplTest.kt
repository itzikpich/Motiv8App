package com.itzikpich.motiv8sdk

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.WindowManager
import com.itzikpich.motiv8sdk.model.ContactData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DataExtractorImplTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var windowManager: WindowManager
    private lateinit var packageManager: PackageManager
    private lateinit var dataExtractor: DataExtractorImpl

    @Before
    fun setup() {
        context = mockk()
        contentResolver = mockk()
        packageManager = mockk()
        windowManager = mockk()

        every { context.contentResolver } returns contentResolver
        every { context.getSystemService(Context.WINDOW_SERVICE) } returns windowManager

        every { context.packageManager } returns packageManager

        dataExtractor = DataExtractorImpl(
            context = context, ioDispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `getContactsData returns correct contact information`() = runTest {
        // Given
        val contactsCursor: Cursor = mockk(relaxed = true)
        val phoneCursor: Cursor = mockk(relaxed = true)

        every {
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, any(), null, null, any()
            )
        } returns contactsCursor

        every {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, any(), any(), null
            )
        } returns phoneCursor

        every { contactsCursor.moveToNext() } returnsMany listOf(true, false)
        every { contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID) } returns 0
        every { contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME) } returns 1
        every { contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER) } returns 2
        every { contactsCursor.getString(0) } returns "1"
        every { contactsCursor.getString(1) } returns "John Doe"
        every { contactsCursor.getInt(2) } returns 1

        every { phoneCursor.moveToFirst() } returns true
        every { phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER) } returns 0
        every { phoneCursor.getString(0) } returns "1234567890"

        // When
        val contacts = dataExtractor.getContactsData().toList()

        // Then
        assertEquals(1, contacts.size)
        assertEquals(ContactData("1", "John Doe", "1234567890"), contacts[0])

        verify { contactsCursor.close() }
        verify { phoneCursor.close() }
    }

    @Test
    fun `getMediaMetadata returns expected media information`() = runTest {
        // Given
        val mediaCursor: Cursor = mockk(relaxed = true)
        val mockUri: Uri = mockk()
        val mockContentUri: Uri = mockk()

        mockkStatic(MediaStore.Files::class)
        every { MediaStore.Files.getContentUri(any()) } returns mockUri

        mockkStatic(ContentUris::class)
        every { ContentUris.withAppendedId(any(), any()) } returns mockContentUri

        every {
            contentResolver.query(
                any(), any(), any(), any(), any()
            )
        } returns mediaCursor

        every { mediaCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mediaCursor.getColumnIndexOrThrow(any()) } returns 0
        every { mediaCursor.getLong(0) } returnsMany listOf(
            1L, 1000000L, 2L, 2000000L, 1000L, 0L, 2000L, 10000L
        )
        every { mediaCursor.getString(0) } returnsMany listOf(
            "image.jpg",
            "video/mp4",
            "video.mp4",
            "/path/to/image.jpg",
            "image/jpeg",
            "/path/to/video.mp4"
        )
        every { mediaCursor.getInt(0) } returnsMany listOf(1080, 1920, 1920, 1080)

        // When
        val mediaItems = dataExtractor.getMediaMetadata().toList()

        // Basic assertions
        assertTrue(mediaItems.isNotEmpty())

        // Verify that cursor is closed
        verify { mediaCursor.close() }

        // Verify that the static methods were called
        verify { MediaStore.Files.getContentUri(any()) }
        verify(atLeast = 1) { ContentUris.withAppendedId(any(), any()) }
    }

}