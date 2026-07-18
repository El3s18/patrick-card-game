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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EcranChoixNombreJoueurs(
    onConfirmer: (Int) -> Unit
) {
    var nombreJoueurs by remember { mutableStateOf(2) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Combien de joueurs ?", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { if (nombreJoueurs > 2) nombreJoueurs-- }
            ) {
                Text("-")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = "$nombreJoueurs", fontSize = 24.sp)

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { if (nombreJoueurs < 6) nombreJoueurs++ }
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { onConfirmer(nombreJoueurs) }) {
            Text("Confirmer")
        }
    }
}