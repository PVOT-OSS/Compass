package org.prauga.compass.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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

    // Altitude
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _altitude = MutableStateFlow<Double?>(null)
    val altitude: StateFlow<Double?> = _altitude

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                if (location.hasAltitude()) {
                    _altitude.value = location.altitude
                }
            }
        }
    }

    fun start() {
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateIntervalMillis(2000L).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
