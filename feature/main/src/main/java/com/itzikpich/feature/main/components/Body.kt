package com.itzikpich.feature.main.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun Body(text: String) =
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )