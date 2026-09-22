package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapis
import com.example.patrick.ui.theme.BleuCiel
import com.example.patrick.ui.theme.RougeCarte

@Composable
fun EcranChoixDifficulte(
    onDifficulteChoisie: (Boolean) -> Unit,
    onRetour: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VertTapis)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = "Choisis ta difficulté",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OrAccent
            )
            Spacer(modifier = Modifier.height(32.dp))
            BoutonMenu(texte = "😊 Facile", onClick = { onDifficulteChoisie(false) })
            Spacer(modifier = Modifier.height(16.dp))
            BoutonMenu(texte = "🔥 Difficile", onClick = { onDifficulteChoisie(true) })
        }
        androidx.compose.material3.Text(
            text = "← Retour",
            color = OrAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clickable { onRetour() }
        )
    }
}