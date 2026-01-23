package com.example.primarydetailcompose.di

import com.example.primarydetailcompose.services.ApiService
import com.example.primarydetailcompose.util.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Dagger module for Network related dependencies.
 *
 * Configures [OkHttpClient] and [Retrofit] for API communication.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured [OkHttpClient] with logging.
     *
     * @return The [OkHttpClient] instance.
     */
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient().newBuilder()
            .addInterceptor(interceptor = logging)
            .build()
    }

    /**
     * Provides the [Retrofit] instance configured with kotlinx.serialization.
     *
     * @param okHttpClient The HTTP client to use for requests.
     * @return The configured [Retrofit] instance.
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Creates the [ApiService] implementation.
     *
     * @param retrofit The [Retrofit] instance.
     * @return The [ApiService] interface implementation.
     */
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
