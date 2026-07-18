package com.example.patrick


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.patrick.ui.theme.PatrickTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.patrick.model.Carte
import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.components.CarteVisuelle
import androidx.compose.foundation.layout.Row
import com.example.patrick.model.Joueur
import com.example.patrick.model.Partie
import com.example.patrick.model.defausserCombinaison
import com.example.patrick.model.defausserCarteUnique
import androidx.compose.foundation.layout.width
import com.example.patrick.model.distribuerCartes
import com.example.patrick.model.piocherBourrer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatrickTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EcranDeTest(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EcranDeTest(modifier: Modifier = Modifier) {
    var paquet by remember { mutableStateOf(melangerPaquet().toMutableList()) }
    var joueur by remember {
        val j = Joueur(nom = "Moi", main = mutableListOf())
        distribuerCartes(listOf(j), paquet)
        mutableStateOf(j)
    }
    var bourrer by remember { mutableStateOf(mutableListOf<Carte>()) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var aJoueCeTour by remember { mutableStateOf(false) }

    // ... reste du code inchangé

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) {
            selection - carte
        } else {
            selection + carte
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Button(
            onClick = {
                val carte = paquet.removeAt(0)
                joueur = joueur.copy(main = (joueur.main + carte).toMutableList())
                aJoueCeTour = false
                message = "Carte piochée ! Tour suivant : joue ou défausse."
            },
            enabled = aJoueCeTour
        ) {
            Text("Piocher")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            for (carte in joueur.main) {
                CarteVisuelle(
                    carte = carte,
                    selectionnee = selection.contains(carte),
                    onClick = { toggleSelection(carte) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    val partie = Partie(joueurs = listOf(joueur), canaillou = paquet, bourrer = bourrer)
                    val reussiCombinaison = defausserCombinaison(partie, joueur, selection)
                    if (reussiCombinaison) {
                        joueur = joueur.copy(main = joueur.main.toMutableList())
                        message = "Combinaison posée ! Pioche pour continuer."
                        selection = listOf()
                        aJoueCeTour = true
                    } else if (selection.size == 1) {
                        defausserCarteUnique(partie, joueur, selection[0])
                        joueur = joueur.copy(main = joueur.main.toMutableList())
                        message = "Carte défaussée ! Pioche pour continuer."
                        selection = listOf()
                        aJoueCeTour = true
                    } else {
                        message = "Sélection invalide"
                    }
                },
                enabled = !aJoueCeTour
            ) {
                Text("Jouer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bourrer (défausse) :")
        if (bourrer.isNotEmpty()) {
            CarteVisuelle(
                carte = bourrer.last(),
                selectionnee = false,
                onClick = {
                    if (aJoueCeTour) {
                        val partie = Partie(joueurs = listOf(joueur), canaillou = paquet, bourrer = bourrer)
                        piocherBourrer(partie, joueur)
                        joueur = joueur.copy(main = joueur.main.toMutableList())
                        bourrer = bourrer.toMutableList()
                        aJoueCeTour = false
                        message = "Carte du bourrer récupérée !"
                    }
                }
            )
        } else {
            Text("Vide")
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PatrickTheme {
        Greeting("Android")
    }
}
