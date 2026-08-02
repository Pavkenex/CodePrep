package com.codeprep.app.di

import com.codeprep.app.data.remote.api.EndpointBuilder
import com.codeprep.app.data.remote.api.OpenRouterApi
import com.codeprep.app.data.settings.AiSettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Placeholder only: the interceptor rewrites the URL per request using the
    // user-configured base URL (see EndpointBuilder). Retrofit needs a valid,
    // absolute URL at construction time.
    private const val RETROFIT_PLACEHOLDER_BASE_URL = "https://placeholder.invalid/"

    @Provides
    @Singleton
    fun provideOkHttpClient(aiSettingsStore: AiSettingsStore): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val endpointUrl = EndpointBuilder.build(
                    baseUrl = aiSettingsStore.getBaseUrl(),
                    path = originalRequest.url.encodedPath.removePrefix("/")
                )

                val requestBuilder = originalRequest.newBuilder()
                    .url(endpointUrl)
                    .addHeader("Content-Type", "application/json")

                // Only send the Authorization header when a key is configured;
                // OkHttp's logging interceptor redacts it by default.
                val apiKey = aiSettingsStore.getApiKey()
                if (apiKey.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                }

                val request = requestBuilder.build()

                var response = chain.proceed(request)
                var retryCount = 0

                while (!response.isSuccessful && response.code == 429 && retryCount < 3) {
                    response.close()
                    retryCount++
                    val waitTimeMs = (1 shl retryCount) * 1000L
                    try {
                        Thread.sleep(waitTimeMs)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                    response = chain.proceed(request)
                }

                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RETROFIT_PLACEHOLDER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenRouterApi(retrofit: Retrofit): OpenRouterApi {
        return retrofit.create(OpenRouterApi::class.java)
    }
}
