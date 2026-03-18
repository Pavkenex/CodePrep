package com.codeprep.app.data.settings

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStringProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsStore: AppSettingsStore
) {
    fun get(@StringRes resId: Int, vararg formatArgs: Any): String {
        val languageCode = appSettingsStore.getSelectedLanguage()
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageCode))
        }
        val localizedResources = context.createConfigurationContext(configuration).resources
        return if (formatArgs.isEmpty()) {
            localizedResources.getString(resId)
        } else {
            localizedResources.getString(resId, *formatArgs)
        }
    }
}
