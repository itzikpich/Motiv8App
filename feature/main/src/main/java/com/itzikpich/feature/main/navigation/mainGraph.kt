package com.itzikpich.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.itzikpich.feature.main.MainRoute

fun NavGraphBuilder.mainGraph() {
    composable("main") {
        MainRoute()
    }
}