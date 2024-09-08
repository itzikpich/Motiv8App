package com.itzikpich.domain

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CheckPermissionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    operator fun invoke(permissions: Array<String>): Boolean {
        return permissions.any { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}