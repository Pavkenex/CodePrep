package com.codeprep.app.ui.secretfact

internal fun remainingSignalDelayMillis(
    signalStartedAt: Long,
    now: Long,
    minimumDurationMillis: Long
): Long {
    val elapsed = now - signalStartedAt
    return (minimumDurationMillis - elapsed).coerceAtLeast(0L)
}
