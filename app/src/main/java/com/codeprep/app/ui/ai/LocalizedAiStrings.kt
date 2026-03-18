package com.codeprep.app.ui.ai

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun localizedAiString(
    languageCode: String,
    @StringRes resId: Int,
    vararg formatArgs: Any
): String {
    val context = LocalContext.current
    val resources = remember(context, languageCode) {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageCode))
        }
        context.createConfigurationContext(configuration).resources
    }
    return if (formatArgs.isEmpty()) {
        resources.getString(resId)
    } else {
        resources.getString(resId, *formatArgs)
    }
}
