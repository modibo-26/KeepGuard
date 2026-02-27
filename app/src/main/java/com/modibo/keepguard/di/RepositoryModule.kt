package com.modibo.keepguard.di

import com.modibo.keepguard.data.repository.AssetRepositoryImpl
import com.modibo.keepguard.data.repository.AuthRepositoryImpl
import com.modibo.keepguard.domain.repository.AssetRepository
import com.modibo.keepguard.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository
}