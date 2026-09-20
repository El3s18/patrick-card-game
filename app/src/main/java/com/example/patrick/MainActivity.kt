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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import com.example.patrick.model.Joueur
import com.example.patrick.model.Partie
import com.example.patrick.model.defausserCombinaison
import com.example.patrick.model.defausserCarteUnique
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.patrick.ui.components.AnimationCelebration
import com.example.patrick.ui.components.BoutonOvale
import com.example.patrick.ui.components.DosDeCarteVisuelle
import com.example.patrick.ui.components.MenuOptionsPartie
import com.example.patrick.ui.components.PileDosDeCarteVisuelle
import com.example.patrick.ui.screens.EcranChoixNombreJoueurs
import com.example.patrick.ui.screens.EcranDeJeuEnLigne
import com.example.patrick.ui.screens.EcranLobbyEnLigne
import com.example.patrick.ui.screens.EcranMenuPrincipal
import com.example.patrick.ui.screens.EcranRegles
import com.example.patrick.ui.screens.EcranSalleAttente
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.NoirCarte
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.RougeCarte
import com.example.patrick.ui.theme.VertTapis

enum class Ecran {
    MENU_PRINCIPAL,
    CHOIX_NOMBRE_JOUEURS,
    REGLES,
    LOBBY_EN_LIGNE,
    SALLE_ATTENTE,
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
                    var codePartieEnLigne by remember { mutableStateOf("") }
                    var modeEnLigne by remember { mutableStateOf(false) }
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
                            onJouerEnLigne = {
                                ecranActuel = Ecran.LOBBY_EN_LIGNE },
                            onVoirRegles = {
                                ecranActuel = Ecran.REGLES
                            }
                        )
                        Ecran.REGLES -> EcranRegles(onRetour = { ecranActuel = Ecran.MENU_PRINCIPAL })
                        Ecran.CHOIX_NOMBRE_JOUEURS -> EcranChoixNombreJoueurs(
                            onConfirmer = { noms ->
                                nomsJoueursChoisis = noms
                                ecranActuel = Ecran.JEU
                            },
                                    onRetour = { ecranActuel = Ecran.MENU_PRINCIPAL }
                        )
                        Ecran.JEU -> {
                            if (modeEnLigne) {
                                EcranDeJeuEnLigne(
                                    code = codePartieEnLigne,
                                    modifier = Modifier.padding(innerPadding),
                                    onRetourMenu = { ecranActuel = Ecran.MENU_PRINCIPAL; modeEnLigne = false }
                                )
                            } else {
                                EcranDeTest(
                                    modifier = Modifier.padding(innerPadding),
                                    nomsJoueurs = if (modeContreIA) listOf("Moi") else nomsJoueursChoisis,
                                    contreIA = modeContreIA,
                                    onRetourMenu = { ecranActuel = Ecran.MENU_PRINCIPAL }
                                )
                            }
                        }
                        Ecran.LOBBY_EN_LIGNE -> EcranLobbyEnLigne(
                            onPartieCreee = { code, nom ->
                                codePartieEnLigne = code
                                ecranActuel = Ecran.SALLE_ATTENTE
                            },
                            onPartieRejointe = { code, nom ->
                                codePartieEnLigne = code
                                ecranActuel = Ecran.SALLE_ATTENTE
                            },
                            onRetour = { ecranActuel = Ecran.MENU_PRINCIPAL }
                        )
                        Ecran.SALLE_ATTENTE -> EcranSalleAttente(
                            code = codePartieEnLigne,
                            onDemarrer = { },
                            onPartieDemarree = {
                                modeEnLigne = true
                                ecranActuel = Ecran.JEU
                            },
                            onRetour = { ecranActuel = Ecran.MENU_PRINCIPAL }
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
    onRetourMenu: () -> Unit = {},

) {
    var paquet by remember { mutableStateOf(melangerPaquet().toMutableList()) }
    var joueurs by remember {
        val liste = if (contreIA) {
            listOf(
                Joueur(nom = nomsJoueurs[0], main = mutableListOf()),
                Joueur(nom = "Bot", main = mutableListOf())
            )
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
    var afficherCelebration by remember { mutableStateOf(false) }
    var afficherResume by remember { mutableStateOf(false) }
    var historiqueScores by remember { mutableStateOf(listOf<List<Int>>()) }
    var codePartieEnLigne by remember { mutableStateOf("") }
    var modeEnLigne by remember { mutableStateOf(false) }

    val joueurActif = joueurs[indexJoueurActif]

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) selection - carte else selection + carte
    }

    fun demarrerNouvelleManche() {
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
        afficherResume = false
        message = "Nouvelle manche distribuée."
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
        historiqueScores = listOf()

    }

    fun gererFinDeManche(quiCrie: Joueur) {
        afficherCelebration = true
        val partie = Partie(joueurs = joueurs, canaillou = paquet, bourrer = bourrer)
        terminerManche(partie, quiCrie)
        joueurs = joueurs.toMutableList()
        historiqueScores = historiqueScores + listOf(joueurs.map { it.score })

        val perdant = trouverPerdant(partie)
        if (perdant != null) {
            val gagnant = trouverGagnant(partie)
            message =
                "${quiCrie.nom} a crié Patrick ! ${gagnant.nom} gagne (perdant : ${perdant.nom}, ${perdant.score} pts) !"
            partieTerminee = true
        } else {
            message = "${quiCrie.nom} a crié Patrick !"
            afficherResume = true
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
        joueurs =
            joueurs.toMutableList().also { it[1] = it[1].copy(main = it[1].main.toMutableList()) }
        bourrer = bourrer.toMutableList()
        paquet = paquet.toMutableList()
        carteDisponiblePourPioche = bourrer.lastOrNull()
        if (botPeutCrierPatrick) gererFinDeManche(joueurs[1]) else message =
            "Le Bot a joué son tour. À toi !"
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
            it[indexJoueurActif] =
                it[indexJoueurActif].copy(main = joueurCourant.main.toMutableList())
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
    Box(modifier = modifier.fillMaxSize()) {
        fun abandonnerLaPartie() {
            joueurActif.score = 111
            joueurs = joueurs.toMutableList()
            val gagnant = joueurs.filter { it != joueurActif }.minByOrNull { it.score }
            message = "${joueurActif.nom} a abandonné. ${gagnant?.nom ?: ""} gagne !"
            partieTerminee = true
        }
        Column(
            modifier = Modifier.fillMaxSize().background(VertTapis).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (afficherResume) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Fin de manche !", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OrAccent)
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(CremeCarteFond, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Manche", fontWeight = FontWeight.Bold, color = NoirCarte, modifier = Modifier.weight(0.6f))
                                for (j in joueurs) {
                                    Text(text = j.nom, fontWeight = FontWeight.Bold, color = NoirCarte, modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = NoirCarte)
                            Spacer(modifier = Modifier.height(8.dp))

                            for ((index, scoresManche) in historiqueScores.withIndex()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(text = "${index + 1}", color = NoirCarte, modifier = Modifier.weight(0.6f))
                                    for (score in scoresManche) {
                                        Text(text = "$score", color = NoirCarte, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    BoutonOvale(texte = "Suivant", onClick = { afficherResume = false })
                }
            } else if (partieTerminee) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Partie terminée !",
                        fontSize = 28.sp,
                        color = CremeCarteFond,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message, fontSize = 16.sp, color = CremeCarteFond)
                    Spacer(modifier = Modifier.height(32.dp))
                    BoutonOvale(texte = "Rejouer", onClick = { recommencerPartie() })
                    Spacer(modifier = Modifier.height(12.dp))
                    BoutonOvale(texte = "Retour au menu", onClick = onRetourMenu, couleur = NoirCarte)
                }
            } else if (enTransition) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Au tour de ${joueurActif.nom}",
                        fontSize = 24.sp,
                        color = CremeCarteFond
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BoutonOvale(texte = "Je suis prêt", onClick = { enTransition = false })
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (j in joueurs) {
                        if (j != joueurActif) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = j.nom, color = CremeCarteFond, fontSize = 12.sp)
                                Text(
                                    text = "Score : ${j.score}",
                                    color = OrAccent,
                                    fontSize = 11.sp
                                )
                                PileDosDeCarteVisuelle(nombreCartes = j.main.size)
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

                    DosDeCarteVisuelle(onClick = {
                        if (!aPioche && !partieTerminee) jouerEtPiocherPuis(
                            false
                        )
                    })
                }

                Column {
                    Text(
                        text = "${joueurActif.nom} — Score : ${joueurActif.score}",
                        color = OrAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    OrAccent,
                                                    RougeCarte
                                                )
                                            ),
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
                    BoutonOvale(texte = "Suivant", onClick = { aPioche = false; passerAuJoueurSuivant() })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = CremeCarteFond, fontSize = 12.sp)
        }

        AnimationCelebration(
            visible = afficherCelebration,
            onFini = { afficherCelebration = false }
        )
        MenuOptionsPartie(
            onAbandonner = { abandonnerLaPartie() },
            onRetourMenu = onRetourMenu,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        )
    }
    fun abandonnerLaPartie() {
        joueurActif.score = 111
        joueurs = joueurs.toMutableList()
        val gagnant = joueurs.filter { it != joueurActif }.minByOrNull { it.score }
        message = "${joueurActif.nom} a abandonné. ${gagnant?.nom ?: ""} gagne !"
        partieTerminee = true
    }
}

