package com.neo.fbrules.main.di

import com.neo.fbrules.main.data.FirebaseRepository
import com.neo.fbrules.main.data.FirebaseRepositoryImpl
import com.neo.fbrules.main.data.api.FirebaseApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
sealed class DataModule {

    companion object {
        @Provides
        fun provideFirebaseApi(): FirebaseApi {
            return FirebaseApi.service
        }
    }

    @Singleton
    @Binds
    abstract fun bindFirebaseRepository(
        firebaseRepositoryImpl: FirebaseRepositoryImpl
    ): FirebaseRepository
}