package com.github.fjbaldon.attendex.capture.di

import android.content.Context
import androidx.room.Room
import com.github.fjbaldon.attendex.capture.core.data.local.AppDatabase
import com.github.fjbaldon.attendex.capture.core.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EntryDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EventDao
import com.github.fjbaldon.attendex.capture.core.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.data.auth.AuthInterceptor
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.auth.SessionManager
import com.github.fjbaldon.attendex.capture.data.auth.UnauthorizedInterceptor
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://192.168.45.180:8080/"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "attendex"
        )
            // FIXED: Removed fallbackToDestructiveMigration(true)
            // Now, if schema changes without a migration plan, the app will crash
            // instead of silently wiping user data. This is safer for production.
            .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(appDatabase: AppDatabase) = appDatabase.eventDao()

    @Provides
    @Singleton
    fun provideAttendeeDao(appDatabase: AppDatabase) = appDatabase.attendeeDao()

    @Provides
    @Singleton
    fun provideEntryDao(appDatabase: AppDatabase) = appDatabase.entryDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepository(apiService, sessionManager)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        appDatabase: AppDatabase,
        eventDao: EventDao,
        attendeeDao: AttendeeDao,
        entryDao: EntryDao
    ): EventRepository {
        return EventRepository(apiService, appDatabase, eventDao, attendeeDao, entryDao)
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
