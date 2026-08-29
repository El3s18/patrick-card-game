package com.example.patrick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.patrick.data.PartieEnLigne
import com.example.patrick.data.calculerScoresApresRevelationEnLigne
import com.example.patrick.data.carteVersMap
import com.example.patrick.data.crierPatrickEnLigne
import com.example.patrick.data.ecouterPartie
import com.example.patrick.data.ecouterMaMain
import com.example.patrick.data.jouerCombinaisonEnLigne
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
import com.example.patrick.mettreAJourNombreCartesEnLigne
import com.example.patrick.ui.components.PileDosDeCarteVisuelle


@Composable
fun EcranDeJeuEnLigne(
    code: String,
    modifier: Modifier = Modifier
) {
    var partie by remember { mutableStateOf(PartieEnLigne()) }
    var maMain by remember { mutableStateOf(listOf<Carte>()) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }
    var message by remember { mutableStateOf("") }
    var afficherResume by remember { mutableStateOf(false) }
    var scoresAvant by remember { mutableStateOf(mapOf<String, Int>()) }
    val monUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var mainsReveleees by remember { mutableStateOf(mapOf<String, List<Carte>>()) }

    LaunchedEffect(code) {
        ecouterPartie(code) { partieMiseAJour -> partie = partieMiseAJour }
        ecouterMaMain(code) { mainMiseAJour -> maMain = mainMiseAJour }
    }

    // Dès que la partie passe en "revelation", SEUL l'hôte calcule les scores
    LaunchedEffect(partie.statut) {
        if (partie.statut == "revelation") {
            scoresAvant = partie.joueurs.associate { it.uid to it.score }
            afficherResume = true
            recupererToutesLesMainsEnLigne(code, partie.joueurs) { mains ->
                mainsReveleees = mains
            }
            if (monUid == partie.hoteUid) {
                calculerScoresApresRevelationEnLigne(
                    code = code,
                    joueurs = partie.joueurs,
                    uidQuiCrie = partie.quiCrie,
                    onSucces = { perdantUid ->
                        val db = FirebaseFirestore.getInstance()
                        val refPartie = db.collection("parties").document(code)
                        if (perdantUid != null) {
                            refPartie.update("statut", "terminee")
                        } else {
                            val nouveauPaquet = melangerPaquet().toMutableList()
                            val mains = mutableMapOf<String, List<Map<String, String>>>()
                            for (j in partie.joueurs) {
                                val main = mutableListOf<Map<String, String>>()
                                repeat(5) {
                                    val carte = nouveauPaquet.removeAt(0)
                                    mains[j.uid] = (mains[j.uid] ?: emptyList()) + carteVersMap(carte)
                                }
                            }
                            refPartie.update(
                                mapOf(
                                    "statut" to "en_cours",
                                    "canaillou" to nouveauPaquet.map { carteVersMap(it) },
                                    "bourrer" to emptyList<Map<String, String>>(),
                                    "indexJoueurActif" to 0
                                )
                            )
                            for ((uid, main) in mains) {
                                refPartie.collection("mains").document(uid).set(mapOf("cartes" to main))
                            }
                        }
                    },
                    onEchec = { }
                )
            }
        }
    }
    LaunchedEffect(maMain.size) {
        if (maMain.isNotEmpty()) {
            mettreAJourNombreCartesEnLigne(code, monUid, maMain.size)
        }
    }

    val moi = partie.joueurs.find { it.uid == monUid }
    val joueurActifUid = partie.joueurs.getOrNull(partie.indexJoueurActif)?.uid
    val cEstMonTour = joueurActifUid == monUid
    val scoreMain = calculerScoreMain(maMain)
    val peutCrierPatrick = cEstMonTour && scoreMain <= 11 && partie.statut == "en_cours"

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

    Column(
        modifier = modifier.fillMaxSize().background(VertTapis).padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (afficherResume) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Fin de manche !",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrAccent
                )
                Spacer(modifier = Modifier.height(24.dp))
                for (j in partie.joueurs) {
                    val avant = scoresAvant[j.uid] ?: 0
                    Text(
                        text = "${j.nom} : $avant → ${j.score}",
                        fontSize = 18.sp,
                        color = CremeCarteFond,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { afficherResume = false }) {
                    Text("Suivant")
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                for (j in partie.joueurs) {
                    if (j.uid != monUid) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = j.nom, color = CremeCarteFond, fontSize = 12.sp)
                            PileDosDeCarteVisuelle(nombreCartes = j.nbCartes)
                            Text(text = "Score : ${j.score}", color = OrAccent, fontSize = 11.sp)
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