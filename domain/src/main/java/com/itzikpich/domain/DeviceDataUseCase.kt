package com.itzikpich.domain

import com.itzikpich.model.DeviceData
import com.itzikpich.motiv8sdk.DataExtractor
import com.itzikpich.motiv8sdk.model.DeviceInfo
import javax.inject.Inject

class DeviceDataUseCase @Inject constructor(
    private val dataExtractor: DataExtractor
) {

    suspend operator fun invoke(): DeviceData = dataExtractor.getDeviceInfo().toDeviceData()

    private fun DeviceInfo.toDeviceData(): DeviceData =
        DeviceData(
            deviceModel = this.model,
            osVersion = this.appVersion,
            manufacturer = this.manufacturer,
            screenResolution = this.hardware
        )

}
