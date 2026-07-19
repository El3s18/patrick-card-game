package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.patrick.data.creerPartieEnLigne
import com.example.patrick.data.rejoindrePartieEnLigne
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.NoirCarte
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.RougeCarte
import com.example.patrick.ui.theme.VertTapis
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EcranLobbyEnLigne(
    onPartieCreee: (code: String, nomJoueur: String) -> Unit,
    onPartieRejointe: (code: String, nomJoueur: String) -> Unit,
    onRetour: () -> Unit
) {
    var nomJoueur by remember { mutableStateOf("") }
    var codeSaisi by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var chargement by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    fun assurerConnexion(onPret: () -> Unit) {
        if (auth.currentUser != null) {
            onPret()
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { onPret() }
                .addOnFailureListener { message = "Erreur de connexion, réessaie." }
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
        Text(text = "Jouer en ligne", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrAccent)
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = nomJoueur,
            onValueChange = { nomJoueur = it },
            label = { Text("Ton pseudo") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CremeCarteFond,
                unfocusedContainerColor = CremeCarteFond,
                focusedTextColor = NoirCarte,
                unfocusedTextColor = NoirCarte
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        BoutonMenu(texte = "Créer une partie", onClick = {
            if (nomJoueur.isBlank()) {
                message = "Choisis un pseudo d'abord."
            } else {
                chargement = true
                assurerConnexion {
                    creerPartieEnLigne(
                        nomJoueur = nomJoueur,
                        onSucces = { code ->
                            chargement = false
                            onPartieCreee(code, nomJoueur)
                        },
                        onEchec = {
                            chargement = false
                            message = "Erreur lors de la création."
                        }
                    )
                }
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "— ou —", color = CremeCarteFond, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = codeSaisi,
            onValueChange = { codeSaisi = it.uppercase() },
            label = { Text("Code de la partie") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CremeCarteFond,
                unfocusedContainerColor = CremeCarteFond,
                focusedTextColor = NoirCarte,
                unfocusedTextColor = NoirCarte
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        BoutonMenu(texte = "Rejoindre", onClick = {
            if (nomJoueur.isBlank() || codeSaisi.isBlank()) {
                message = "Renseigne ton pseudo et le code."
            } else {
                chargement = true
                assurerConnexion {
                    rejoindrePartieEnLigne(
                        code = codeSaisi,
                        nomJoueur = nomJoueur,
                        onSucces = {
                            chargement = false
                            onPartieRejointe(codeSaisi, nomJoueur)
                        },
                        onEchec = { e ->
                            chargement = false
                            message = e.message ?: "Erreur pour rejoindre."
                        }
                    )
                }
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        if (message.isNotEmpty()) {
            Text(text = message, color = RougeCarte, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetour) {
            Text("Retour")
        }
    }
}

