package com.example.patrick.ui.components

import com.example.patrick.model.Carte
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.patrick.model.Famille
import androidx.compose.foundation.clickable
@Composable
fun CarteVisuelle(carte: Carte, selectionnee: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 90.dp)
            .border(
                width = if (selectionnee) 3.dp else 1.dp,
                color = if (selectionnee) Color.Blue else Color.Black
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = "${carte.valeur.ordinal + 1}${carte.famille.symbole}",
            color = couleurDeLaFamille(carte.famille)
        )
    }
}

fun couleurDeLaFamille(famille: Famille): Color {
    return when (famille) {
        Famille.COEUR, Famille.CARREAU -> Color.Red
        Famille.PIQUE, Famille.TREFLE -> Color.Black
    }
}