package com.itzikpich.domain

import com.itzikpich.model.Contact
import com.itzikpich.motiv8sdk.DataExtractor
import com.itzikpich.motiv8sdk.model.ContactData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val dataExtractor: DataExtractor
) {
    suspend operator fun invoke(): Flow<Contact> =
        dataExtractor.getContactsData().map { it.toContact() }

    private fun ContactData.toContact() = Contact(
        id = this.id,
        name = this.name,
        phoneNumber = this.phoneNumber
    )
}