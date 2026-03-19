package com.codeprep.app.ui.localization

import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.data.settings.AppSettingsStore
import dagger.hilt.android.EntryPointAccessors
import java.util.Locale

@Composable
fun localizedStringResource(
    @StringRes resId: Int,
    vararg formatArgs: Any
): String {
    val resources = localizedResources()
    return if (formatArgs.isEmpty()) {
        resources.getString(resId)
    } else {
        resources.getString(resId, *formatArgs)
    }
}

@Composable
fun localizedPluralStringResource(
    @PluralsRes resId: Int,
    quantity: Int,
    vararg formatArgs: Any
): String {
    val resources = localizedResources()
    return if (formatArgs.isEmpty()) {
        resources.getQuantityString(resId, quantity)
    } else {
        resources.getQuantityString(resId, quantity, *formatArgs)
    }
}

@Composable
private fun localizedResources(): Resources {
    val context = LocalContext.current
    val appSettingsStore = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            UiLocalizationEntryPoint::class.java
        ).appSettingsStore()
    }
    val languageCode by appSettingsStore.selectedLanguage().collectAsStateWithLifecycle(
        initialValue = AppSettingsStore.DEFAULT_LANGUAGE
    )

    return remember(context, languageCode) {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageCode))
        }
        context.createConfigurationContext(configuration).resources
    }
}
