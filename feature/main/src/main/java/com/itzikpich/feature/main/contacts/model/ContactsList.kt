package com.itzikpich.feature.main.contacts.model

import androidx.compose.runtime.Immutable
import com.itzikpich.model.Contact

@Immutable
internal data class ContactsList(
    val contacts: List<Contact>
) : List<Contact> by contacts {
    companion object {
        val EMPTY = ContactsList(emptyList())
    }
}
