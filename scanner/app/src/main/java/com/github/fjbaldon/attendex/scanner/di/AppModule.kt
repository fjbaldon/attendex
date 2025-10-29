package com.github.fjbaldon.attendex.scanner.di

import android.content.Context
import androidx.room.Room
import com.github.fjbaldon.attendex.scanner.data.auth.AuthInterceptor
import com.github.fjbaldon.attendex.scanner.data.auth.SessionManager
import com.github.fjbaldon.attendex.scanner.data.local.AppDatabase
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.EventDao
import com.github.fjbaldon.attendex.scanner.data.remote.ApiService
import com.github.fjbaldon.attendex.scanner.data.repository.AuthRepository
import com.github.fjbaldon.attendex.scanner.data.repository.EventRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL =
        "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "attendex"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEventDao(appDatabase: AppDatabase) = appDatabase.eventDao()

    @Provides
    @Singleton
    fun provideAttendeeDao(appDatabase: AppDatabase) = appDatabase.attendeeDao()

    @Provides
    @Singleton
    fun provideAttendanceRecordDao(appDatabase: AppDatabase) = appDatabase.attendanceRecordDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
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
        eventDao: EventDao,
        attendeeDao: AttendeeDao,
        attendanceRecordDao: AttendanceRecordDao
    ): EventRepository {
        return EventRepository(apiService, eventDao, attendeeDao, attendanceRecordDao)
    }
}
