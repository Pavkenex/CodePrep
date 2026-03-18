package com.codeprep.app.ui.secretfact

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

class SecretFactSensorController(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp = 0L

    fun start() {
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val normalizedX = event.values[0] / SensorManager.GRAVITY_EARTH
        val normalizedY = event.values[1] / SensorManager.GRAVITY_EARTH
        val normalizedZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(
            normalizedX * normalizedX +
                normalizedY * normalizedY +
                normalizedZ * normalizedZ
        )

        val now = SystemClock.elapsedRealtime()
        if (gForce > SHAKE_THRESHOLD_GRAVITY && now - lastShakeTimestamp > SHAKE_DEBOUNCE_MS) {
            lastShakeTimestamp = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_DEBOUNCE_MS = 900L
    }
}
