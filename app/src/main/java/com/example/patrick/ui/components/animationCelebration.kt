package com.example.patrick.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.patrick.ui.theme.OrAccent
import kotlinx.coroutines.delay


@Composable
fun AnimationCelebration(visible: Boolean, onFini: () -> Unit) {
    if (visible) {
        val echelle = remember { Animatable(0f) }

        LaunchedEffect(visible) {
            echelle.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            delay(1200)
            onFini()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔥 PATRICK ! 🔥",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = OrAccent,
                modifier = Modifier.graphicsLayer(
                    scaleX = echelle.value,
                    scaleY = echelle.value
                )
            )
        }
    }
}