package com.itzikpich.feature.main.model

sealed class PermissionState {
    data object Initial : PermissionState()
    data object Granted : PermissionState()
    data object NotGranted : PermissionState()
    data object Requesting : PermissionState()
}