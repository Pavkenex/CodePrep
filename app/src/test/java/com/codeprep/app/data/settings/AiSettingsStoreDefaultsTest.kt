package com.codeprep.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AiSettingsStoreDefaultsTest {

    @Test
    fun `model id default is blank so the field seeds empty on first run`() {
        assertEquals("", AiSettingsStore.DEFAULT_MODEL_ID)
    }

    @Test
    fun `base url default is blank so the field seeds empty on first run`() {
        assertEquals("", AiSettingsStore.DEFAULT_BASE_URL)
    }
}
