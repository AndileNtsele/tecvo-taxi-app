// D:\TAXI\app\src\main\java\com\example\taxi\di\RepositoryModule.kt
package com.tecvo.taxi.di
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.services.ErrorHandlingService
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        errorHandlingService: ErrorHandlingService
    ): AuthRepository {
        return AuthRepository(firebaseAuth, errorHandlingService)
    }
}