package org.prauga.compass.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.prauga.compass.components.CompassDial
import org.prauga.compass.viewmodel.CompassViewModel

@Composable
fun CompassScreen(viewModel: CompassViewModel) {

    val heading by viewModel.heading.collectAsState()
    val cumulativeHeading by viewModel.cumulativeHeading.collectAsState()
    val animatedHeading by animateFloatAsState(
        targetValue = cumulativeHeading,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(Unit) { viewModel.start() }
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

        Text(
            text = "${heading.toInt()}° ${direction(heading)}",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        )
    }
}

private fun direction(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5f) / 45f).toInt() % 8]
}
