package com.example.patrick.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.patrick.model.Valeur
import com.example.patrick.ui.theme.BleuSelection
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapisClair
import com.example.patrick.R

@Composable
fun CarteVisuelle(carte: Carte, selectionnee: Boolean, onClick: () -> Unit) {
    val decalage = if (selectionnee) (-12).dp else 0.dp

    Box(
        modifier = Modifier
            .size(width = 70.dp, height = 100.dp)
            .offset(y = decalage)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(CremeCarteFond)
            .border(
                width = if (selectionnee) 3.dp else 1.dp,
                color = if (selectionnee) BleuSelection else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = "${afficherValeur(carte.valeur)}${carte.famille.symbole}",
            color = couleurDeLaFamille(carte.famille),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Text(
            text = carte.famille.symbole,
            color = couleurDeLaFamille(carte.famille),
            fontSize = 28.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

fun afficherValeur(valeur: Valeur): String {
    return when (valeur) {
        Valeur.AS -> "A"
        Valeur.VALET -> "V"
        Valeur.DAME -> "D"
        Valeur.ROI -> "R"
        else -> (valeur.ordinal + 1).toString()
    }
}

fun couleurDeLaFamille(famille: Famille): Color {
    return when (famille) {
        Famille.COEUR, Famille.CARREAU -> Color.Red
        Famille.PIQUE, Famille.TREFLE -> Color.Black
    }
}

@Composable
fun DosDeCarteVisuelle(onClick: () -> Unit = {}) {
    Image(
        painter = painterResource(id = R.drawable.carteverso),
        contentDescription = "Dos de carte",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = 70.dp, height = 100.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    )
}