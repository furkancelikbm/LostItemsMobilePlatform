package com.example.mycompose.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) // Singleton component scope, available throughout the app
object AppModule {

    @Provides
    fun provideContext(application: Application): Context = application
}
