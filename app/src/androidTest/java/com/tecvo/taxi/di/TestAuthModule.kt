package com.tecvo.taxi.di

import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.navigation.NavHostManager
import com.tecvo.taxi.di.RepositoryModule
import com.tecvo.taxi.di.NavigationModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

/**
 * Test module that provides mocked dependencies for navigation and auth.
 * Only replaces RepositoryModule and NavigationModule to avoid binding conflicts.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class, NavigationModule::class]
)
object TestAuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return mock(AuthRepository::class.java)
    }


    @Provides
    @Singleton
    fun provideNavHostManager(): NavHostManager {
        return mock(NavHostManager::class.java)
    }
}