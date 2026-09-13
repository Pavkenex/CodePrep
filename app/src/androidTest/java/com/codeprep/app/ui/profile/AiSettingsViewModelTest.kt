package com.codeprep.app.ui.profile

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codeprep.app.data.remote.api.AiApiResponse
import com.codeprep.app.data.remote.api.AiRequest
import com.codeprep.app.data.remote.api.OpenRouterApi
import com.codeprep.app.data.settings.AiSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AiSettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var store: AiSettingsStore

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        store = AiSettingsStore(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        store.setApiKey(AiSettingsStore.DEFAULT_API_KEY)
        store.setModelId(AiSettingsStore.DEFAULT_MODEL_ID)
        store.setBaseUrl(AiSettingsStore.DEFAULT_BASE_URL)
    }

    @Test
    fun testConnection_returnsSuccessResult_whenProviderAnswers() = runTest(dispatcher.scheduler) {
        val viewModel = AiSettingsViewModel(store, FakeOpenRouterApi())

        val result = viewModel.testConnection("test-key", "test-model", "https://example.com")

        assertEquals(AiConnectionTestResult.Success, result)
        assertEquals(AiConnectionTestResult.Success, viewModel.connectionTestResult.value)
        assertFalse(viewModel.isTestingConnection.value)
    }

    @Test
    fun testConnection_returnsInvalidKeyResult_whenProviderRejectsKey() = runTest(dispatcher.scheduler) {
        val unauthorized = HttpException(
            Response.error<Any>(401, "unauthorized".toResponseBody(null))
        )
        val viewModel = AiSettingsViewModel(store, FakeOpenRouterApi(error = unauthorized))

        val result = viewModel.testConnection("bad-key", "test-model", "https://example.com")

        assertEquals(AiConnectionTestResult.InvalidKey, result)
        assertEquals(AiConnectionTestResult.InvalidKey, viewModel.connectionTestResult.value)
        assertFalse(viewModel.isTestingConnection.value)
    }

    private class FakeOpenRouterApi(
        private val error: Throwable? = null
    ) : OpenRouterApi {
        override suspend fun askQuestion(
            url: String,
            request: AiRequest,
            sessionId: String?,
            userAgent: String?
        ): AiApiResponse {
            error?.let { throw it }
            return AiApiResponse(choices = emptyList())
        }
    }
}
