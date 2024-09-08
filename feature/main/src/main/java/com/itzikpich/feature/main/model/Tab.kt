package com.itzikpich.feature.main.model

import androidx.compose.runtime.Immutable

internal data class Tab(
    val id: Int,
    val title: String
)

@Immutable
internal data class TabsList(
    val tabs: List<Tab>
) : List<Tab> by tabs