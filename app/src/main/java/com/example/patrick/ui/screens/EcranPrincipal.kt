package com.example.patrick.ui.screens

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

@Composable
fun EcranMenuPrincipal(
    onJouerContreIA: () -> Unit,
    onJouerEnLocal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Patrick", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onJouerContreIA) {
            Text("Jouer contre l'IA")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onJouerEnLocal) {
            Text("Jouer en local (entre amis)")
        }
    }
}