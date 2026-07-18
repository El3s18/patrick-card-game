package com.example.patrick.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Combien de joueurs ?", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                if (nombreJoueurs > 2) {
                    nombreJoueurs--
                    ajusterListeNoms(nombreJoueurs)
                }
            }) {
                Text("-")
            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "$nombreJoueurs", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                if (nombreJoueurs < 6) {
                    nombreJoueurs++
                    ajusterListeNoms(nombreJoueurs)
                }
            }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        for (i in noms.indices) {
            TextField(
                value = noms[i],
                onValueChange = { nouveauNom ->
                    noms = noms.toMutableList().also { it[i] = nouveauNom }
                },
                label = { Text("Nom du joueur ${i + 1}") }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onConfirmer(noms) }) {
            Text("Confirmer")
        }
    }
}