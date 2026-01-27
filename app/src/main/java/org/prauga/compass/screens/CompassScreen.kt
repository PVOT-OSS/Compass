package org.prauga.compass.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.prauga.compass.components.CompassDial
import org.prauga.compass.viewmodel.CompassViewModel

@Composable
fun CompassScreen(viewModel: CompassViewModel) {

    val heading by viewModel.heading.collectAsState()
    val cumulativeHeading by viewModel.cumulativeHeading.collectAsState()
    val altitude by viewModel.altitude.collectAsState()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CompassDial(
            heading = animatedHeading,
            modifier = Modifier.size(340.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${heading.toInt()}° ${direction(heading)}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (locationGranted) {
                val altText =
                    altitude?.let { "${it.toInt()} m above sea level" } ?: "Acquiring altitude…"
                Text(
                    text = altText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun direction(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5f) / 45f).toInt() % 8]
}
