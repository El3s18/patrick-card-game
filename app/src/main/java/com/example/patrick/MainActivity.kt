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
import com.example.patrick.model.calculerScoreMain
import com.example.patrick.model.distribuerCartes
import com.example.patrick.model.piocherBourrer
import com.example.patrick.model.terminerManche
import com.example.patrick.model.trouverGagnant
import com.example.patrick.model.trouverPerdant

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
    var joueurs by remember {
        val j1 = Joueur(nom = "Moi", main = mutableListOf())
        val j2 = Joueur(nom = "Bot", main = mutableListOf())
        val liste = listOf(j1, j2)
        distribuerCartes(liste, paquet)
        mutableStateOf(liste)
    }
    var bourrer by remember { mutableStateOf(mutableListOf<Carte>()) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var aJoueCeTour by remember { mutableStateOf(false) }
    var partieTerminee by remember { mutableStateOf(false) }

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
                joueurs = joueurs.toMutableList().also {
                    it[0] = it[0].copy(main = (it[0].main + carte).toMutableList())
                }
                aJoueCeTour = false
                message = "Carte piochée ! Tour suivant : joue ou défausse."
            },
            enabled = aJoueCeTour
        ) {
            Text("Piocher")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            for (carte in joueurs[0].main) {
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
                    val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
                    val reussiCombinaison = defausserCombinaison(partie, joueurs[0], selection)
                    if (reussiCombinaison) {
                        joueurs = joueurs.toMutableList().also { it[0] = it[0].copy(main = it[0].main.toMutableList()) }
                        message = "Combinaison posée ! Pioche pour continuer."
                        selection = listOf()
                        aJoueCeTour = true
                    } else if (selection.size == 1) {
                        defausserCarteUnique(partie, joueurs[0], selection[0])
                        joueurs = joueurs.toMutableList().also { it[0] = it[0].copy(main = it[0].main.toMutableList()) }
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

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
                    terminerManche(partie, joueurs[0])

                    val perdant = trouverPerdant(partie)
                    if (perdant != null) {
                        val gagnant = trouverGagnant(partie)
                        message = "Partie terminée ! ${perdant.nom} a perdu (${perdant.score} pts). ${gagnant.nom} gagne avec ${gagnant.score} pts !"
                        partieTerminee = true
                    } else {
                        val nouveauPaquet = melangerPaquet().toMutableList()
                        val nouveauxJoueurs = joueurs.map { it.copy(main = mutableListOf()) }
                        distribuerCartes(nouveauxJoueurs, nouveauPaquet)

                        paquet = nouveauPaquet
                        joueurs = nouveauxJoueurs
                        bourrer = mutableListOf()
                        aJoueCeTour = false
                        message = "Manche terminée ! Scores : " + joueurs.joinToString { "${it.nom}=${it.score}" } + " — Nouvelle manche distribuée."
                    }
                },
                enabled = !aJoueCeTour && !partieTerminee && calculerScoreMain(joueurs[0].main) <= 11
            ) {
                Text("Crier Patrick !")
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
                        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
                        piocherBourrer(partie, joueurs[0])
                        joueurs = joueurs.toMutableList().also { it[0] = it[0].copy(main = it[0].main.toMutableList()) }
                        bourrer = bourrer.toMutableList()
                        aJoueCeTour = false
                        message = "Carte du bourrer récupérée !"
                    }
                }
            )
        } else {
            Text("Vide")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}