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
import androidx.compose.ui.unit.sp
import com.example.patrick.model.calculerScoreMain
import com.example.patrick.model.distribuerCartes
import com.example.patrick.model.jouerTourBot
import com.example.patrick.model.piocherCarteDuBourrer
import com.example.patrick.model.terminerManche
import com.example.patrick.model.trouverGagnant
import com.example.patrick.model.trouverPerdant
import com.example.patrick.ui.screens.EcranChoixNombreJoueurs
import com.example.patrick.ui.screens.EcranMenuPrincipal

enum class Ecran {
    MENU_PRINCIPAL,
    CHOIX_NOMBRE_JOUEURS,
    JEU
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatrickTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var ecranActuel by remember { mutableStateOf(Ecran.MENU_PRINCIPAL) }
                    var nomsJoueursChoisis by remember { mutableStateOf(listOf<String>()) }
                    var modeContreIA by remember { mutableStateOf(true) }

                    when (ecranActuel) {
                        Ecran.MENU_PRINCIPAL -> EcranMenuPrincipal(
                            onJouerContreIA = {
                                modeContreIA = true
                                ecranActuel = Ecran.JEU
                            },
                            onJouerEnLocal = {
                                modeContreIA = false
                                ecranActuel = Ecran.CHOIX_NOMBRE_JOUEURS
                            }
                        )
                        Ecran.CHOIX_NOMBRE_JOUEURS -> EcranChoixNombreJoueurs(
                            onConfirmer = { noms ->
                                nomsJoueursChoisis = noms
                                ecranActuel = Ecran.JEU
                            }
                        )
                        Ecran.JEU -> EcranDeTest(
                            modifier = Modifier.padding(innerPadding),
                            nomsJoueurs = if (modeContreIA) listOf("Moi") else nomsJoueursChoisis,
                            contreIA = modeContreIA
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EcranDeTest(modifier: Modifier = Modifier, nomsJoueurs: List<String> = listOf("Moi"), contreIA: Boolean = true) {
    var paquet by remember { mutableStateOf(melangerPaquet().toMutableList()) }
    var joueurs by remember {
        val liste = if (contreIA) {
            listOf(Joueur(nom = nomsJoueurs[0], main = mutableListOf()), Joueur(nom = "Bot", main = mutableListOf()))
        } else {
            nomsJoueurs.map { nom -> Joueur(nom = nom, main = mutableListOf()) }
        }
        distribuerCartes(liste, paquet)
        mutableStateOf(liste)
    }
    var indexJoueurActif by remember { mutableStateOf(0) }
    var enTransition by remember { mutableStateOf(false) }
    var bourrer by remember { mutableStateOf(mutableListOf<Carte>()) }
    var carteDisponiblePourPioche by remember { mutableStateOf<Carte?>(null) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var aJoueCeTour by remember { mutableStateOf(false) }
    var partieTerminee by remember { mutableStateOf(false) }
    var aPioche by remember { mutableStateOf(false) }

    val joueurActif = joueurs[indexJoueurActif]

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) {
            selection - carte
        } else {
            selection + carte
        }
    }

    fun gererFinDeManche(quiCrie: Joueur) {
        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        terminerManche(partie, quiCrie)

        val perdant = trouverPerdant(partie)
        if (perdant != null) {
            val gagnant = trouverGagnant(partie)
            message = "${quiCrie.nom} a crié Patrick ! Partie terminée : ${gagnant.nom} gagne (perdant : ${perdant.nom}, ${perdant.score} pts) !"
            partieTerminee = true
            joueurs = joueurs.toMutableList()
        } else {
            val nouveauPaquet = melangerPaquet().toMutableList()
            val nouveauxJoueurs = joueurs.map { it.copy(main = mutableListOf()) }
            distribuerCartes(nouveauxJoueurs, nouveauPaquet)

            paquet = nouveauPaquet
            joueurs = nouveauxJoueurs
            bourrer = mutableListOf()
            carteDisponiblePourPioche = null
            selection = listOf()
            aJoueCeTour = false
            indexJoueurActif = 0
            enTransition = !contreIA
            message = "${quiCrie.nom} a crié Patrick ! Scores : " +
                    joueurs.joinToString { "${it.nom}=${it.score}" } +
                    " — Nouvelle manche distribuée."
        }
    }

    fun jouerBotAutomatiquement() {
        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        val botPeutCrierPatrick = jouerTourBot(partie, joueurs[1])

        joueurs = joueurs.toMutableList().also {
            it[1] = it[1].copy(main = it[1].main.toMutableList())
        }
        bourrer = bourrer.toMutableList()
        paquet = paquet.toMutableList()
        carteDisponiblePourPioche = bourrer.lastOrNull()

        if (botPeutCrierPatrick) {
            gererFinDeManche(joueurs[1])
        } else {
            message = "Le Bot a joué son tour. À toi !"
        }
    }

    fun passerAuJoueurSuivant() {
        carteDisponiblePourPioche = bourrer.lastOrNull()
        indexJoueurActif = (indexJoueurActif + 1) % joueurs.size
        aJoueCeTour = false
        selection = listOf()
        enTransition = true
    }

    Column(modifier = modifier.padding(16.dp)) {
        if (enTransition) {
            Text(text = "Au tour de ${joueurActif.nom}", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { enTransition = false }) {
                Text("Je suis prêt")
            }
        } else {
            Button(
                onClick = {
                    val carte = paquet.removeAt(0)
                    joueurs = joueurs.toMutableList().also {
                        it[indexJoueurActif] = it[indexJoueurActif].copy(main = (it[indexJoueurActif].main + carte).toMutableList())
                    }
                    if (contreIA) {
                        aJoueCeTour = false
                        jouerBotAutomatiquement()
                    } else {
                        aPioche = true
                    }
                },
                enabled = aJoueCeTour && !aPioche && !partieTerminee
            ) {
                Text("Piocher")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                for (carte in joueurActif.main) {
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
                        val reussiCombinaison = defausserCombinaison(partie, joueurActif, selection)
                        if (reussiCombinaison) {
                            joueurs = joueurs.toMutableList().also { it[indexJoueurActif] = it[indexJoueurActif].copy(main = it[indexJoueurActif].main.toMutableList()) }
                            bourrer = bourrer.toMutableList()
                            message = "Combinaison posée ! Pioche pour continuer."
                            selection = listOf()
                            aJoueCeTour = true
                        } else if (selection.size == 1) {
                            defausserCarteUnique(partie, joueurActif, selection[0])
                            joueurs = joueurs.toMutableList().also { it[indexJoueurActif] = it[indexJoueurActif].copy(main = it[indexJoueurActif].main.toMutableList()) }
                            bourrer = bourrer.toMutableList()
                            message = "Carte défaussée ! Pioche pour continuer."
                            selection = listOf()
                            aJoueCeTour = true
                        } else {
                            message = "Sélection invalide"
                        }
                    },
                    enabled = !aJoueCeTour && !partieTerminee
                ) {
                    Text("Jouer")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { gererFinDeManche(joueurActif) },
                    enabled = !aJoueCeTour && !partieTerminee && calculerScoreMain(joueurActif.main) <= 11
                ) {
                    Text("Crier Patrick !")
                }
            }
            if (aPioche && !contreIA) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    aPioche = false
                    passerAuJoueurSuivant()
                }) {
                    Text("Joueur suivant")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Carte disponible à la pioche (bourrer) :")
            val carteDispo = carteDisponiblePourPioche
            if (carteDispo != null) {
                CarteVisuelle(
                    carte = carteDispo,
                    selectionnee = false,
                    onClick = {
                        if (aJoueCeTour && !partieTerminee) {
                            val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
                            piocherCarteDuBourrer(partie, joueurActif, carteDispo)
                            joueurs = joueurs.toMutableList().also { it[indexJoueurActif] = it[indexJoueurActif].copy(main = it[indexJoueurActif].main.toMutableList()) }
                            bourrer = bourrer.toMutableList()
                            carteDisponiblePourPioche = null
                            if (contreIA) {
                                aJoueCeTour = false
                                jouerBotAutomatiquement()
                            } else {
                                aPioche = true
                            }
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
}