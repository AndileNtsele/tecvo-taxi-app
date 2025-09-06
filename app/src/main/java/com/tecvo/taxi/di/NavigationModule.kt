package com.tecvo.taxi.di

import com.tecvo.taxi.navigation.NavHostManager
import com.tecvo.taxi.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides navigation-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    @Provides
    @Singleton
    fun provideNavHostManager(authRepository: AuthRepository): NavHostManager {
        return NavHostManager(authRepository)
    }
}