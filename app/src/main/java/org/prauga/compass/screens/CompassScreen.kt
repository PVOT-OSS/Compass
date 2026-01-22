package org.prauga.compass.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.prauga.compass.R
import org.prauga.compass.viewmodel.CompassViewModel

@Composable
fun CompassScreen(viewModel: CompassViewModel) {

    val heading by viewModel.heading.collectAsState()

    LaunchedEffect(Unit) { viewModel.start() }
    DisposableEffect(Unit) {
        onDispose { viewModel.stop() }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.compass),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer {
                    rotationZ = -heading
                }
        )

        Text(
            text = "${heading.toInt()}° ${direction(heading)}",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
        )
    }
}

private fun direction(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5f) / 45f).toInt() % 8]
}