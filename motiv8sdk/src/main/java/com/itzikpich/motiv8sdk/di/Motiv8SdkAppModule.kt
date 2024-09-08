package com.itzikpich.motiv8sdk.di

import com.itzikpich.motiv8sdk.DataExtractor
import com.itzikpich.motiv8sdk.DataExtractorImpl
import com.itzikpich.motiv8sdk.common.Dispatcher
import com.itzikpich.motiv8sdk.common.Dispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class Motiv8SdkAppModule {

    @Provides
    @Singleton
    fun provideDataExtractor(dataExtractor: DataExtractorImpl): DataExtractor = dataExtractor

    @Provides
    @Dispatcher(Dispatchers.IO)
    fun provideIODispatcher(): CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
}