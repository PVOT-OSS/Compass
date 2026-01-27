package org.prauga.compass.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.prauga.compass.components.CompassDial
import org.prauga.compass.viewmodel.CompassViewModel

@Composable
fun CompassScreen(viewModel: CompassViewModel) {

    val heading by viewModel.heading.collectAsState()
    val cumulativeHeading by viewModel.cumulativeHeading.collectAsState()
    val altitude by viewModel.altitude.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val placeName by viewModel.placeName.collectAsState()
    val animatedHeading by animateFloatAsState(
        targetValue = cumulativeHeading,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )

    // Location permission
    var locationGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
        if (granted) viewModel.startLocationUpdates()
    }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current
    val currentSlot = (heading / 30f).toInt()
    var lastSlot by remember { mutableIntStateOf(currentSlot) }
    LaunchedEffect(currentSlot) {
        if (currentSlot != lastSlot) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastSlot = currentSlot
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start()
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stop() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        CompassDial(
            heading = animatedHeading,
            modifier = Modifier.size(340.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "${heading.toInt()}° ${direction(heading)}",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (locationGranted) {
            // Coordinates in DMS
            val lat = latitude
            val lng = longitude
            if (lat != null && lng != null) {
                Text(
                    text = "${toDms(lat, "N", "S")} ${toDms(lng, "E", "W")}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
            // Place name
            placeName?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
            // Sea level
            val altText =
                altitude?.let { "${it.toInt()}m Elevation" } ?: "Acquiring altitude…"
            Text(
                text = altText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun direction(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5f) / 45f).toInt() % 8]
}

private fun toDms(decimal: Double, positive: String, negative: String): String {
    val dir = if (decimal >= 0) positive else negative
    val abs = kotlin.math.abs(decimal)
    val degrees = abs.toInt()
    val minutesFull = (abs - degrees) * 60
    val minutes = minutesFull.toInt()
    val seconds = ((minutesFull - minutes) * 60).toInt()
    return "$degrees°$minutes'$seconds\"$dir"
}
