package com.example.patrick


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.patrick.model.Carte
import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.components.CarteVisuelle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import com.example.patrick.model.Joueur
import com.example.patrick.model.Partie
import com.example.patrick.model.defausserCombinaison
import com.example.patrick.model.defausserCarteUnique
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.patrick.model.calculerScoreMain
import com.example.patrick.model.distribuerCartes
import com.example.patrick.model.jouerTourBot
import com.example.patrick.model.piocherCarteDuBourrer
import com.example.patrick.model.terminerManche
import com.example.patrick.model.trouverGagnant
import com.example.patrick.model.trouverPerdant
import com.example.patrick.ui.components.DosDeCarteVisuelle
import com.example.patrick.ui.screens.EcranChoixNombreJoueurs
import com.example.patrick.ui.screens.EcranMenuPrincipal
import com.example.patrick.ui.screens.EcranRegles
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.NoirCarte
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.RougeCarte
import com.example.patrick.ui.theme.VertTapis

enum class Ecran {
    MENU_PRINCIPAL,
    CHOIX_NOMBRE_JOUEURS,
    REGLES,
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
                            } ,
                            onVoirRegles = {
                                ecranActuel = Ecran.REGLES
                            }
                        )
                        Ecran.REGLES -> EcranRegles(onRetour = { ecranActuel = Ecran.MENU_PRINCIPAL })
                        Ecran.CHOIX_NOMBRE_JOUEURS -> EcranChoixNombreJoueurs(
                            onConfirmer = { noms ->
                                nomsJoueursChoisis = noms
                                ecranActuel = Ecran.JEU
                            }
                        )
                        Ecran.JEU -> EcranDeTest(
                            modifier = Modifier.padding(innerPadding),
                            nomsJoueurs = if (modeContreIA) listOf("Moi") else nomsJoueursChoisis,
                            contreIA = modeContreIA,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EcranDeTest(
    modifier: Modifier = Modifier,
    nomsJoueurs: List<String> = listOf("Moi"),
    contreIA: Boolean = true,
    onRetourMenu: () -> Unit = {}
) {
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
    var aPioche by remember { mutableStateOf(false) }
    var bourrer by remember { mutableStateOf(mutableListOf<Carte>()) }
    var carteDisponiblePourPioche by remember { mutableStateOf<Carte?>(null) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var partieTerminee by remember { mutableStateOf(false) }

    val joueurActif = joueurs[indexJoueurActif]

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) selection - carte else selection + carte
    }
    fun recommencerPartie() {
        val nouveauPaquet = melangerPaquet().toMutableList()
        val nouveauxJoueurs = joueurs.map { it.copy(main = mutableListOf(), score = 0) }
        distribuerCartes(nouveauxJoueurs, nouveauPaquet)

        paquet = nouveauPaquet
        joueurs = nouveauxJoueurs
        bourrer = mutableListOf()
        carteDisponiblePourPioche = null
        selection = listOf()
        aPioche = false
        indexJoueurActif = 0
        partieTerminee = false
        enTransition = !contreIA
        message = "Nouvelle partie !"
    }
    fun gererFinDeManche(quiCrie: Joueur) {
        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        terminerManche(partie, quiCrie)
        val perdant = trouverPerdant(partie)
        if (perdant != null) {
            val gagnant = trouverGagnant(partie)
            message = "${quiCrie.nom} a crié Patrick ! ${gagnant.nom} gagne (perdant : ${perdant.nom}, ${perdant.score} pts) !"
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
            aPioche = false
            indexJoueurActif = 0
            enTransition = !contreIA
            message = "${quiCrie.nom} a crié Patrick ! Nouvelle manche distribuée."
        }
    }

    fun jouerBotAutomatiquement() {
        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        val botPeutCrierPatrick = jouerTourBot(partie, joueurs[1])
        joueurs = joueurs.toMutableList().also { it[1] = it[1].copy(main = it[1].main.toMutableList()) }
        bourrer = bourrer.toMutableList()
        paquet = paquet.toMutableList()
        carteDisponiblePourPioche = bourrer.lastOrNull()
        if (botPeutCrierPatrick) gererFinDeManche(joueurs[1]) else message = "Le Bot a joué son tour. À toi !"
    }

    fun passerAuJoueurSuivant() {
        carteDisponiblePourPioche = bourrer.lastOrNull()
        indexJoueurActif = (indexJoueurActif + 1) % joueurs.size
        aPioche = false
        selection = listOf()
        enTransition = true
    }

    fun jouerEtPiocherPuis(pileEstBourrer: Boolean) {
        if (selection.isEmpty()) {
            message = "Sélectionne une combinaison ou une carte à jeter d'abord."
            return
        }

        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        val joueurCourant = joueurs[indexJoueurActif]
        val reussiCombinaison = defausserCombinaison(partie, joueurCourant, selection)
        val reussi = if (reussiCombinaison) {
            true
        } else if (selection.size == 1) {
            defausserCarteUnique(partie, joueurCourant, selection[0])
            true
        } else {
            false
        }

        if (!reussi) {
            message = "Sélection invalide"
            return
        }

        if (pileEstBourrer) {
            val carteDispo = carteDisponiblePourPioche
            if (carteDispo != null) {
                piocherCarteDuBourrer(partie, joueurCourant, carteDispo)
                carteDisponiblePourPioche = null
            }
        } else {
            val carte = paquet.removeAt(0)
            joueurCourant.main.add(carte)
        }

        joueurs = joueurs.toMutableList().also {
            it[indexJoueurActif] = it[indexJoueurActif].copy(main = joueurCourant.main.toMutableList())
        }
        bourrer = bourrer.toMutableList()
        paquet = paquet.toMutableList()
        selection = listOf()
        message = "Action effectuée !"

        if (contreIA) {
            jouerBotAutomatiquement()
        } else {
            aPioche = true
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(VertTapis).padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (partieTerminee) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Partie terminée !", fontSize = 28.sp, color = CremeCarteFond, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, fontSize = 16.sp, color = CremeCarteFond)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { recommencerPartie() }) {
                    Text("Rejouer")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onRetourMenu() }) {
                    Text("Retour au menu")
                }
            }
        } else if (enTransition) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Au tour de ${joueurActif.nom}", fontSize = 24.sp, color = CremeCarteFond)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { enTransition = false }) {
                    Text("Je suis prêt")
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                for (j in joueurs) {
                    if (j != joueurActif) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = j.nom, color = CremeCarteFond, fontSize = 12.sp)
                            Box {
                                DosDeCarteVisuelle()
                                Text(
                                    text = "${j.main.size}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(NoirCarte, shape = RoundedCornerShape(50))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val carteDispo = carteDisponiblePourPioche
                if (carteDispo != null) {
                    CarteVisuelle(
                        carte = carteDispo,
                        selectionnee = false,
                        onClick = { if (!aPioche && !partieTerminee) jouerEtPiocherPuis(true) }
                    )
                } else {
                    Box(modifier = Modifier.size(width = 70.dp, height = 100.dp))
                }

                Spacer(modifier = Modifier.width(24.dp))

                DosDeCarteVisuelle(onClick = { if (!aPioche && !partieTerminee) jouerEtPiocherPuis(false) })
            }

            Column {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (carte in joueurActif.main) {
                        CarteVisuelle(
                            carte = carte,
                            selectionnee = selection.contains(carte),
                            onClick = { toggleSelection(carte) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    if (!aPioche && !partieTerminee && calculerScoreMain(joueurActif.main) <= 11) {
                        Button(
                            onClick = { gererFinDeManche(joueurActif) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        brush = Brush.horizontalGradient(listOf(OrAccent, RougeCarte)),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    text = "🔥 PATRICK !",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                        }
                    }

                    if (aPioche && !contreIA) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { aPioche = false; passerAuJoueurSuivant() }) {
                            Text("Suivant")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, color = CremeCarteFond, fontSize = 12.sp)
            }
        }
