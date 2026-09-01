package com.example.patrick.ui.components


import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.patrick.ui.theme.BleuCiel

@Composable
fun BoutonOvale(
    texte: String,
    onClick: () -> Unit,
    couleur: Color = BleuCiel,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = couleur),
        modifier = modifier.height(48.dp)
    ) {
        Text(text = texte, color = Color.White, fontWeight = FontWeight.Bold)
    }
}