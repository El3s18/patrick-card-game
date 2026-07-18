package com.example.patrick.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.patrick.R
import com.example.patrick.ui.theme.BleuSelection
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapis

@Composable
fun EcranMenuPrincipal(
    onJouerContreIA: () -> Unit,
    onJouerEnLocal: () -> Unit,
    onVoirRegles: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.patrickwallpaper),
            contentDescription = "Fond du menu principal",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            BoutonMenu(texte = "🤖 Jouer contre l'IA", onClick = onJouerContreIA)
            Spacer(modifier = Modifier.height(16.dp))
            BoutonMenu(texte = "👥 Jouer en local", onClick = onJouerEnLocal)
            Spacer(modifier = Modifier.height(16.dp))
            BoutonMenu(texte = "📖 Règles du jeu", onClick = onVoirRegles)
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun EcranRegles(onRetour: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VertTapis)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Règles du jeu", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OrAccent)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Le but : ne pas être le premier joueur à atteindre 111 points.\n\n" +
                    "Chaque joueur a 5 cartes en main. À votre tour, sélectionnez une combinaison " +
                    "(paire, brelan, carré, suite) ou une carte seule, puis piochez en cliquant sur " +
                    "le Canaillou ou le bourrer.\n\n" +
                    "Si votre main totalise 11 points ou moins, criez Patrick ! Attention : vous ne " +
                    "savez jamais si un adversaire a moins que vous...",
            fontSize = 16.sp,
            color = CremeCarteFond
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetour) {
            Text("Retour")
        }
    }
}

@Composable
fun BoutonMenu(texte: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(listOf(OrAccent, BleuSelection)),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Text(text = texte, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}