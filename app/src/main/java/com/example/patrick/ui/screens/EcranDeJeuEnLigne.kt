package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.patrick.data.JoueurEnLigne
import com.example.patrick.data.PartieEnLigne
import com.example.patrick.data.abandonnerPartieEnLigne
import com.example.patrick.data.calculerScoresApresRevelationEnLigne
import com.example.patrick.data.carteVersMap
import com.example.patrick.data.crierPatrickEnLigne
import com.example.patrick.data.ecouterPartie
import com.example.patrick.data.ecouterMaMain
import com.example.patrick.data.jouerCombinaisonEnLigne
import com.example.patrick.data.mettreAJourNombreCartesEnLigne
import com.example.patrick.data.piocherEtPasserAuSuivantEnLigne
import com.example.patrick.model.Carte
import com.example.patrick.model.calculerScoreMain
import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.components.CarteVisuelle
import com.example.patrick.ui.components.DosDeCarteVisuelle
import com.example.patrick.ui.theme.CremeCarteFond
import com.example.patrick.ui.theme.OrAccent
import com.example.patrick.ui.theme.VertTapis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.patrick.data.recupererToutesLesMainsEnLigne
import com.example.patrick.data.validerMancheSuivante
import com.example.patrick.ui.components.BoutonOvale
import com.example.patrick.ui.components.MenuOptionsPartie
import com.example.patrick.ui.components.PileDosDeCarteVisuelle
import com.example.patrick.ui.theme.NoirCarte


@Composable
fun EcranDeJeuEnLigne(
    code: String,
    modifier: Modifier = Modifier,
    onRetourMenu: () -> Unit = {}
) {
    var partie by remember { mutableStateOf(PartieEnLigne()) }
    var maMain by remember { mutableStateOf(listOf<Carte>()) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var afficherResume by remember { mutableStateOf(false) }
    var scoresAvant by remember { mutableStateOf(mapOf<String, Int>()) }
    val monUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var mainsReveleees by remember { mutableStateOf(mapOf<String, List<Carte>>()) }
    var historiqueScoresEnLigne by remember { mutableStateOf(listOf<List<Int>>()) }
    var afficherTableauScoresEnCours by remember { mutableStateOf(false) }

    LaunchedEffect(code) {
        ecouterPartie(code) { partieMiseAJour -> partie = partieMiseAJour }
        ecouterMaMain(code) { mainMiseAJour -> maMain = mainMiseAJour }
    }

    var erreurCalculScore by remember { mutableStateOf(false) }

    fun lancerCalculScores() {
        erreurCalculScore = false
        calculerScoresApresRevelationEnLigne(
            code = code,
            joueurs = partie.joueurs,
            uidQuiCrie = partie.quiCrie,
            // Les scores, le statut et le drapeau scoreCalcule sont maintenant tous
            // écrits atomiquement à l'intérieur de calculerScoresApresRevelationEnLigne :
            // il n'y a plus rien à réécrire ici en cas de succès.
            onSucces = { _, _ -> },
            onEchec = {
                erreurCalculScore = true
                message = "Erreur lors du calcul du score, réessaie."
            }
        )
    }

    // Dès que la partie passe en "revelation", SEUL l'hôte calcule les scores.
    // On garde aussi partie.scoreCalcule dans la clé : ça évite de relancer le calcul
    // une deuxième fois si l'hôte revient d'arrière-plan pendant la révélation.
    LaunchedEffect(partie.statut, partie.scoreCalcule) {
        if (partie.statut == "revelation") {
            scoresAvant = partie.joueurs.associate { it.uid to it.score }
            afficherResume = true
            recupererToutesLesMainsEnLigne(code, partie.joueurs) { mains ->
                mainsReveleees = mains
            }
            if (monUid == partie.hoteUid && !partie.scoreCalcule) {
                lancerCalculScores()
            }
        } else if (partie.statut == "en_cours") {
            if (afficherResume) {
                historiqueScoresEnLigne =
                    historiqueScoresEnLigne + listOf(partie.joueurs.map { it.score })
            }
            afficherResume = false
        }
    }
    LaunchedEffect(maMain.size) {
        if (maMain.isNotEmpty()) {
            mettreAJourNombreCartesEnLigne(code, monUid, maMain.size)
        }
    }
    LaunchedEffect(partie.joueursPrets) {
        if (partie.statut == "revelation" &&
            partie.joueurs.isNotEmpty() &&
            partie.joueursPrets.size == partie.joueurs.size &&
            monUid == partie.hoteUid
        ) {
            val db = FirebaseFirestore.getInstance()
            val refPartie = db.collection("parties").document(code)
            val nouveauPaquet = melangerPaquet().toMutableList()
            val mains = mutableMapOf<String, List<Map<String, String>>>()
            for (j in partie.joueurs) {
                val main = mutableListOf<Map<String, String>>()
                repeat(5) {
                    val carte = nouveauPaquet.removeAt(0)
                    mains[j.uid] = (mains[j.uid] ?: emptyList()) + carteVersMap(carte)
                }
            }
            val indexPerdant = partie.joueurs.indexOfFirst { it.uid == partie.perdantManche }
                .let { if (it == -1) 0 else it }
            // Un seul batch atomique pour le document de la partie ET toutes les mains :
            // avant, ces écritures étaient séparées, ce qui laissait une fenêtre où le
            // statut passait déjà à "en_cours" (et un joueur pouvait donc jouer) alors
            // que sa main n'avait pas encore été redistribuée côté serveur — c'est ce
            // qui pouvait produire des mains à 6 cartes après un bug de manche précédente.
            val batch = db.batch()
            batch.update(
                refPartie,
                mapOf(
                    "statut" to "en_cours",
                    "canaillou" to nouveauPaquet.map { carteVersMap(it) },
                    "bourrer" to emptyList<Map<String, String>>(),
                    "indexJoueurActif" to indexPerdant,
                    "joueursPrets" to emptyList<String>(),
                    "perdantManche" to "",
                    "scoreCalcule" to false
                )
            )
            for ((uid, main) in mains) {
                batch.set(refPartie.collection("mains").document(uid), mapOf("cartes" to main))
            }
            batch.commit()
        }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observateur = androidx.lifecycle.LifecycleEventObserver { _, evenement ->
            if (evenement == androidx.lifecycle.Lifecycle.Event.ON_STOP && partie.statut != "terminee") {
                abandonnerPartieEnLigne(code = code, uid = monUid, onSucces = { }, onEchec = { })
            }
        }
        lifecycleOwner.lifecycle.addObserver(observateur)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observateur)
        }
    }
    val moi = partie.joueurs.find { it.uid == monUid }
    val joueurActifUid = partie.joueurs.getOrNull(partie.indexJoueurActif)?.uid
    val cEstMonTour = joueurActifUid == monUid
    val scoreMain = calculerScoreMain(maMain)
    val peutCrierPatrick = cEstMonTour && scoreMain <= 11 && partie.statut == "en_cours"
    val toutesMainsChargees = partie.joueurs.isNotEmpty() &&
            partie.joueurs.all { mainsReveleees.containsKey(it.uid) }
    val nomAppelant = partie.joueurs.find { it.uid == partie.quiCrie }?.nom ?: ""
    val scoreMainAppelant = calculerScoreMain(mainsReveleees[partie.quiCrie] ?: emptyList())
    val joueursAvecMoins = if (toutesMainsChargees) {
        partie.joueurs.filter {
            it.uid != partie.quiCrie &&
                    calculerScoreMain(mainsReveleees[it.uid] ?: emptyList()) < scoreMainAppelant
        }
    } else emptyList()
    val patrickReussi = toutesMainsChargees && joueursAvecMoins.isEmpty()
    val bonusAppelant = scoreMainAppelant + 10 * joueursAvecMoins.size

    val messageResultatPatrick: String = if (!afficherResume || !toutesMainsChargees) {
        ""
    } else if (monUid == partie.quiCrie) {
        when {
            patrickReussi -> "🎉 Patrick réussi ! Tu ne marques aucun point ce tour."
            (moi?.score ?: 0) >= 111 -> "💥 Patrick raté, et tu dépasses les 111 points..."
            else -> "😬 Patrick raté... tu marques $bonusAppelant points."
        }
    } else if (patrickReussi) {
        "Tu marques ${calculerScoreMain(mainsReveleees[monUid] ?: emptyList())} points (Patrick de $nomAppelant)."
    } else if (joueursAvecMoins.any { it.uid == monUid }) {
        "Tu avais moins que $nomAppelant, tu ne marques rien !"
    } else {
        "Tu ne marques rien sur cette manche."
    }

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) selection - carte else selection + carte
    }

    fun jouerPuisPiocher(pileEstBourrer: Boolean) {
        if (selection.isEmpty()) {
            message = "Sélectionne une combinaison ou une carte."
            return
        }
        val carteCibleeAvant = partie.bourrer.lastOrNull()
        jouerCombinaisonEnLigne(
            code = code,
            maMainActuelle = maMain,
            bourrerActuel = partie.bourrer,
            selection = selection,
            onSucces = {
                selection = listOf()
                piocherEtPasserAuSuivantEnLigne(
                    code = code,
                    maMainActuelle = maMain - selection.toSet(),
                    canaillouActuel = partie.canaillou,
                    bourrerActuel = partie.bourrer + selection,
                    indexJoueurActifActuel = partie.indexJoueurActif,
                    nombreDeJoueurs = partie.joueurs.size,
                    pileEstBourrer = pileEstBourrer,
                    carteCiblee = if (pileEstBourrer) carteCibleeAvant else null,
                    onSucces = { message = "Action effectuée !" },
                    onEchec = { erreur -> message = erreur }
                )
            },
            onEchec = { erreur -> message = erreur }
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        if (partie.statut == "terminee") {
            val nomAbandonnant = partie.joueurs.find { it.uid == partie.uidAbandon }?.nom
            Column(
                modifier = Modifier.fillMaxSize().background(VertTapis).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (nomAbandonnant != null) {
                        "$nomAbandonnant a quitté la partie."
                    } else {
                        "La partie est terminée."
                    },
                    color = CremeCarteFond,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                BoutonOvale(texte = "Retour au menu", onClick = onRetourMenu)
            }
        } else {
            Column(
                modifier = modifier.fillMaxSize().background(VertTapis).padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (afficherResume) {
                    val adversaires = partie.joueurs.filter { it.uid != monUid }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Premier adversaire : en haut, centré (face à moi)
                        adversaires.getOrNull(0)?.let { j ->
                            Box(
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp)
                            ) {
                                BlocMainRevelee(
                                    joueur = j,
                                    cartes = mainsReveleees[j.uid] ?: emptyList()
                                )
                            }
                        }

                        // Deuxième adversaire : à droite, centré verticalement
                        adversaires.getOrNull(1)?.let { j ->
                            Box(
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                            ) {
                                BlocMainRevelee(
                                    joueur = j,
                                    cartes = mainsReveleees[j.uid] ?: emptyList()
                                )
                            }
                        }

                        // Troisième adversaire : à gauche, centré verticalement
                        adversaires.getOrNull(2)?.let { j ->
                            Box(
                                modifier = Modifier.align(Alignment.CenterStart)
                                    .padding(start = 8.dp)
                            ) {
                                BlocMainRevelee(
                                    joueur = j,
                                    cartes = mainsReveleees[j.uid] ?: emptyList()
                                )
                            }
                        }

                        // Tableau des scores : au centre de l'écran
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp)
                                .background(CremeCarteFond, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            TableauScores(
                                joueurs = partie.joueurs,
                                historique = historiqueScoresEnLigne
                            )
                        }

                        // Ma main révélée + message + bouton : en bas, comme en jeu normal
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row {
                                for (carte in mainsReveleees[monUid] ?: emptyList()) {
                                    CarteVisuelle(
                                        carte = carte,
                                        selectionnee = false,
                                        onClick = { })
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (erreurCalculScore && monUid == partie.hoteUid) {
                                Text(
                                    text = "Erreur lors du calcul du score.",
                                    color = CremeCarteFond,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                BoutonOvale(
                                    texte = "Réessayer",
                                    onClick = { lancerCalculScores() }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            if (messageResultatPatrick.isNotEmpty()) {
                                Text(
                                    text = messageResultatPatrick,
                                    color = CremeCarteFond,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            val dejaValide = partie.joueursPrets.contains(monUid)
                            if (dejaValide) {
                                Text(
                                    text = "En attente des autres joueurs... (${partie.joueursPrets.size}/${partie.joueurs.size})",
                                    color = CremeCarteFond,
                                    fontSize = 13.sp
                                )
                            } else {
                                BoutonOvale(
                                    texte = "Manche suivante (${partie.joueursPrets.size}/${partie.joueurs.size})",
                                    onClick = {
                                        validerMancheSuivante(
                                            code = code,
                                            uid = monUid,
                                            onSucces = { },
                                            onEchec = { erreur -> message = erreur }
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val adversaires = partie.joueurs.filter { it.uid != monUid }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            adversaires.getOrNull(0)?.let { j ->
                                BlocJoueurAdverse(
                                    joueur = j,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                )
                            }
                            adversaires.getOrNull(1)?.let { j ->
                                BlocJoueurAdverse(
                                    joueur = j,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }
                            adversaires.getOrNull(2)?.let { j ->
                                BlocJoueurAdverse(
                                    joueur = j,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val carteBourrer = partie.bourrer.lastOrNull()
                            if (carteBourrer != null) {
                                CarteVisuelle(
                                    carte = carteBourrer,
                                    selectionnee = false,
                                    onClick = { if (cEstMonTour) jouerPuisPiocher(true) }
                                )
                            } else {
                                Box(modifier = Modifier.size(width = 70.dp, height = 100.dp))
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            DosDeCarteVisuelle(onClick = { if (cEstMonTour) jouerPuisPiocher(false) })
                        }

                        Column {
                            Text(
                                text = "${moi?.nom ?: "Moi"} — Score : ${moi?.score ?: 0}",
                                color = OrAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (carte in maMain) {
                                    CarteVisuelle(
                                        carte = carte,
                                        selectionnee = selection.contains(carte),
                                        onClick = { toggleSelection(carte) }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (peutCrierPatrick) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        crierPatrickEnLigne(
                                            code = code,
                                            onSucces = { },
                                            onEchec = { erreur -> message = erreur })
                                    }) {
                                        Text("🔥 PATRICK !")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (cEstMonTour) "C'est ton tour !" else "En attente de ${
                                    partie.joueurs.getOrNull(
                                        partie.indexJoueurActif
                                    )?.nom ?: "..."
                                }",
                                color = CremeCarteFond,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = message, color = CremeCarteFond, fontSize = 12.sp)
                        }
                    }
                }
            }
            MenuOptionsPartie(
                onAbandonner = {
                    abandonnerPartieEnLigne(
                        code = code,
                        uid = monUid,
                        onSucces = { },
                        onEchec = { erreur -> message = erreur })
                },
                onRetourMenu = {
                    abandonnerPartieEnLigne(
                        code = code,
                        uid = monUid,
                        onSucces = { },
                        onEchec = { erreur -> message = erreur })
                },
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            )
        }

    if (!afficherResume) {
        BoutonOvale(
            texte = "📊",
            onClick = { afficherTableauScoresEnCours = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        )
    }

    if (afficherTableauScoresEnCours) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .background(CremeCarteFond, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✕",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NoirCarte,
                        modifier = Modifier.clickable { afficherTableauScoresEnCours = false }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TableauScores(joueurs = partie.joueurs, historique = historiqueScoresEnLigne)
            }
        }
    }
        MenuOptionsPartie(
            onAbandonner = {
                abandonnerPartieEnLigne(code = code, uid = monUid, onSucces = { }, onEchec = { erreur -> message = erreur })
            },
            onRetourMenu = {
                abandonnerPartieEnLigne(code = code, uid = monUid, onSucces = { }, onEchec = { erreur -> message = erreur })
            },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        )
    }
}
@Composable
private fun BlocMainRevelee(joueur: JoueurEnLigne, cartes: List<Carte>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${joueur.nom} — ${calculerScoreMain(cartes)} pts",
            color = CremeCarteFond,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.widthIn(max = 140.dp).horizontalScroll(rememberScrollState())
        ) {
            for (carte in cartes) {
                CarteVisuelle(carte = carte, selectionnee = false, onClick = { })
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}
@Composable
private fun BlocJoueurAdverse(joueur: JoueurEnLigne, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = joueur.nom, color = CremeCarteFond, fontSize = 12.sp)
        PileDosDeCarteVisuelle(nombreCartes = joueur.nbCartes)
        Text(text = "Score : ${joueur.score}", color = OrAccent, fontSize = 11.sp)
    }
}
@Composable
private fun TableauScores(
    joueurs: List<JoueurEnLigne>,
    historique: List<List<Int>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Manche",
                fontWeight = FontWeight.Bold,
                color = NoirCarte,
                maxLines = 1,
                modifier = Modifier.width(70.dp)
            )
            for (j in joueurs) {
                Text(
                    text = j.nom,
                    fontWeight = FontWeight.Bold,
                    color = NoirCarte,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = NoirCarte)
        Spacer(modifier = Modifier.height(4.dp))

        val toutesLesLignes = historique + listOf(joueurs.map { it.score })
        Column(
            modifier = Modifier
                .heightIn(max = 100.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for ((index, scoresManche) in toutesLesLignes.withIndex()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "${index + 1}", color = NoirCarte, modifier = Modifier.width(70.dp))
                    for (score in scoresManche) {
                        Text(text = "$score", color = NoirCarte, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
