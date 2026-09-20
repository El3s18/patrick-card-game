package com.example.patrick.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.NoirCarte
import com.example.patrick.ui.theme.RougeCarte

@Composable
fun MenuOptionsPartie(
    onAbandonner: () -> Unit,
    onRetourMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ouvert by remember { mutableStateOf(false) }
    var demandeConfirmation by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { ouvert = true },
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(NoirCarte.copy(alpha = 0.5f), shape = CircleShape)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(CremeCarteFond)
                        )
                    }
                }
            }
        }
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            DropdownMenuItem(
                text = { Text("🤝 Abandonner") },
                onClick = { ouvert = false; demandeConfirmation = true }
            )
            DropdownMenuItem(
                text = { Text("🚫 Retour au menu") },
                onClick = { ouvert = false; onRetourMenu() }
            )
        }
    }

    if (demandeConfirmation) {
        AlertDialog(
            onDismissRequest = { demandeConfirmation = false },
            title = { Text("Abandonner la partie ?") },
            text = { Text("Tu seras déclaré perdant automatiquement. Confirmer ?") },
            confirmButton = {
                BoutonOvale(
                    texte = "Abandonner",
                    onClick = { demandeConfirmation = false; onAbandonner() },
                    couleur = RougeCarte
                )
            },
            dismissButton = {
                BoutonOvale(
                    texte = "Annuler",
                    onClick = { demandeConfirmation = false },
                    couleur = NoirCarte
                )
            }
        )
    }
}