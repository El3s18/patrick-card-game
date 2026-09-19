package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.patrick.ui.theme.BleuSelection
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.NoirCarte
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapis

@Composable
fun EcranChoixNombreJoueurs(
    onConfirmer: (List<String>) -> Unit
) {
    var nombreJoueurs by remember { mutableStateOf(2) }
    var noms by remember { mutableStateOf(listOf("Joueur 1", "Joueur 2")) }

    fun ajusterListeNoms(nouveauNombre: Int) {
        noms = (1..nouveauNombre).map { i ->
            noms.getOrElse(i - 1) { "Joueur $i" }
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

        Text(
            text = "Combien de joueurs ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OrAccent
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BoutonRond(texte = "-", onClick = {
                if (nombreJoueurs > 2) {
                    nombreJoueurs--
                    ajusterListeNoms(nombreJoueurs)
                }
            })

            Text(
                text = "$nombreJoueurs",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CremeCarteFond,
                modifier = Modifier.width(64.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            BoutonRond(texte = "+", onClick = {
                if (nombreJoueurs < 4) {
                    nombreJoueurs++
                    ajusterListeNoms(nombreJoueurs)
                }
            })
        }

        Spacer(modifier = Modifier.height(32.dp))

        for (i in noms.indices) {
            TextField(
                value = noms[i],
                onValueChange = { nouveauNom ->
                    noms = noms.toMutableList().also { it[i] = nouveauNom }
                },
                label = { Text("Nom du joueur ${i + 1}") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CremeCarteFond,
                    unfocusedContainerColor = CremeCarteFond,
                    focusedTextColor = NoirCarte,
                    unfocusedTextColor = NoirCarte,
                    focusedIndicatorColor = OrAccent,
                    unfocusedIndicatorColor = BleuSelection
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        BoutonMenu(texte = "Confirmer", onClick = { onConfirmer(noms) })
    }
}

@Composable
fun BoutonRond(texte: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = OrAccent),
        modifier = Modifier.size(48.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(text = texte, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NoirCarte)
    }
}