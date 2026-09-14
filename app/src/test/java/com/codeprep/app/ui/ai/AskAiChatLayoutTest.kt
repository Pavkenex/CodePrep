package com.codeprep.app.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AskAiChatLayoutTest {

    @Test
    fun portraitHeight_keepsTheExistingComposerStack() {
        val layout = askAiChatLayoutFor(screenHeightDp = 891)

        assertFalse(layout.compact)
        assertEquals(14, layout.headerVerticalPaddingDp)
        assertEquals(96, layout.inputMinHeightDp)
        assertEquals(144, layout.inputMaxHeightDp)
        assertEquals(2, layout.inputMinLines)
        assertEquals(4, layout.inputMaxLines)
        assertFalse(layout.inlineSendButton)
        assertEquals(0, layout.sendButtonWidthDp)
    }

    @Test
    fun landscapePhoneHeight_usesCompactComposerWithInlineSend() {
        val layout = askAiChatLayoutFor(screenHeightDp = 412)

        assertTrue(layout.compact)
        assertEquals(6, layout.headerVerticalPaddingDp)
        assertEquals(56, layout.inputMinHeightDp)
        assertEquals(88, layout.inputMaxHeightDp)
        assertEquals(1, layout.inputMinLines)
        assertEquals(2, layout.inputMaxLines)
        assertTrue(layout.inlineSendButton)
        assertEquals(132, layout.sendButtonWidthDp)
    }
}
