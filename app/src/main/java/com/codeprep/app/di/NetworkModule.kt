package com.codeprep.app.di

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
    // Retrofit requires a valid absolute URL at construction time. Every call
    // passes its real URL via @Url, so this base is only a stub.
    private const val RETROFIT_PLACEHOLDER_BASE_URL = "https://placeholder.invalid/"

    @Provides
    @Singleton
    fun provideOkHttpClient(aiSettingsStore: AiSettingsStore): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")

                // Only send the Authorization header when a key is configured.
                // The logging interceptor below redacts it explicitly via
                // redactHeader("Authorization") — OkHttp does NOT redact any
                // header by default, so an unredacted key would reach logcat.
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
            .addInterceptor(createHttpLoggingInterceptor())
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

    /**
     * Logging interceptor for the AI network calls. Runs at Level.BODY and
     * explicitly redacts the Authorization header: OkHttp's
     * HttpLoggingInterceptor redacts nothing by default, so without
     * [redactHeader] the user's API key would be written to logcat on every
     * request.
     */
    internal fun createHttpLoggingInterceptor(
        logger: HttpLoggingInterceptor.Logger? = null
    ): HttpLoggingInterceptor =
        (if (logger != null) HttpLoggingInterceptor(logger) else HttpLoggingInterceptor()).apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }
}
