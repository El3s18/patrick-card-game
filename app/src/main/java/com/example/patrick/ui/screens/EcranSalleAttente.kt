package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.patrick.data.PartieEnLigne
import com.example.patrick.data.ecouterPartie
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapis
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun EcranSalleAttente(
    code: String,
    onDemarrer: () -> Unit,
    onRetour: () -> Unit
) {
    var partie by remember { mutableStateOf(PartieEnLigne()) }
    val monUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(code) {
        ecouterPartie(code) { partieMiseAJour ->
            partie = partieMiseAJour
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VertTapis)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Salle d'attente", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrAccent)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Code de la partie :", fontSize = 14.sp, color = CremeCarteFond)
        Text(text = code, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CremeCarteFond)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Joueurs (${partie.joueurs.size}) :", fontSize = 16.sp, color = OrAccent)
        Spacer(modifier = Modifier.height(8.dp))

        for (joueur in partie.joueurs) {
            Text(text = "• ${joueur.nom}", fontSize = 16.sp, color = CremeCarteFond)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (monUid == partie.hoteUid) {
            BoutonMenu(texte = "Démarrer la partie", onClick = onDemarrer)
        } else {
            Text(text = "En attente que l'hôte démarre...", fontSize = 14.sp, color = CremeCarteFond)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetour) {
            Text("Quitter")
        }
    }
}