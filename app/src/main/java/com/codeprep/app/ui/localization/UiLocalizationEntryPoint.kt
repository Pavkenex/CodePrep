package com.codeprep.app.ui.localization

import com.codeprep.app.data.settings.AppSettingsStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UiLocalizationEntryPoint {
    fun appSettingsStore(): AppSettingsStore
}
