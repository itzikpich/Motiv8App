package com.itzikpich.motiv8sdk.common

import javax.inject.Qualifier

@Qualifier
@Retention
annotation class Dispatcher(val dispatcher: Dispatchers)

enum class Dispatchers {
    Default,
    IO,
    Main,
    MainImmediate
}