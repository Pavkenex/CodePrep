package com.codeprep.app.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FunFactWidgetEntryPoint {
    fun funFactWidgetUpdater(): FunFactWidgetUpdater
    fun workScheduler(): com.codeprep.app.work.WorkScheduler
}
