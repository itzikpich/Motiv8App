package com.itzikpich.motiv8sdk.model

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val device: String,
    val product: String,
    val brand: String,
    val hardware: String,
    val appVersion: String,
    val osVersion: String,
    val screenResolution: String
)