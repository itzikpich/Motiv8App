package com.itzikpich.feature.main

import android.Manifest
import android.os.Build

internal val mediaPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
    )
} else {
    arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
    )
}

internal const val contactsPermission = Manifest.permission.READ_CONTACTS