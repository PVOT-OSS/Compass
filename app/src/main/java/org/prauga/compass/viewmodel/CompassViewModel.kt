package org.prauga.compass.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompassViewModel(
    context: Context
) : ViewModel(), SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading

    private var _cumulative = 0f
    private val _cumulativeHeading = MutableStateFlow(0f)
    val cumulativeHeading: StateFlow<Float> = _cumulativeHeading

    fun start() {
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthRad = orientationAngles[0]
        val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()) + 360) % 360

        val newHeading = azimuthDeg.toFloat()
        _heading.value = newHeading

        val current = ((_cumulative % 360f) + 360f) % 360f
        var delta = newHeading - current
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        _cumulative += delta
        _cumulativeHeading.value = _cumulative
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
