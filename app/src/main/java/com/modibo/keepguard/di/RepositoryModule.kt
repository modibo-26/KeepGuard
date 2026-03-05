package com.modibo.keepguard.di

import com.modibo.keepguard.data.repository.AssetRepositoryImpl
import com.modibo.keepguard.data.repository.AuthRepositoryImpl
import com.modibo.keepguard.data.repository.DocumentRepositoryImpl
import com.modibo.keepguard.data.repository.MaintenanceRepositoryImpl
import com.modibo.keepguard.data.repository.WarrantyRepositoryImpl
import com.modibo.keepguard.domain.repository.AssetRepository
import com.modibo.keepguard.domain.repository.AuthRepository
import com.modibo.keepguard.domain.repository.DocumentRepository
import com.modibo.keepguard.domain.repository.MaintenanceRepository
import com.modibo.keepguard.domain.repository.WarrantyRepository
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

    @Binds
    @Singleton
    abstract fun bindWarrantyRepository(impl: WarrantyRepositoryImpl): WarrantyRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository
}