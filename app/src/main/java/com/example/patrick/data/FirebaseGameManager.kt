package com.example.patrick.data

import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.screens.BoutonMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.patrick.model.Carte
import com.example.patrick.model.Famille
import com.example.patrick.model.Valeur
import com.example.patrick.model.estUneCombinaisonValide
import com.example.patrick.model.calculerScoreMain

fun genererCodePartie(): String {
    val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return (1..4).map { caracteres.random() }.joinToString("")
}

fun creerPartieEnLigne(
    nomJoueur: String,
    onSucces: (String) -> Unit,
    onEchec: (Exception) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val code = genererCodePartie()

    val donneesPartie = hashMapOf(
        "joueurs" to listOf(mapOf("uid" to uid, "nom" to nomJoueur, "score" to 0)),
        "statut" to "en_attente",
        "indexJoueurActif" to 0,
        "bourrer" to listOf<Map<String, String>>(),
        "hoteUid" to uid
    )

    db.collection("parties").document(code)
        .set(donneesPartie)
        .addOnSuccessListener { onSucces(code) }
        .addOnFailureListener { e -> onEchec(e) }
}

fun rejoindrePartieEnLigne(
    code: String,
    nomJoueur: String,
    onSucces: () -> Unit,
    onEchec: (Exception) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val refPartie = db.collection("parties").document(code)

    refPartie.get()
        .addOnSuccessListener { document ->
            if (!document.exists()) {
                onEchec(Exception("Cette partie n'existe pas"))
                return@addOnSuccessListener
            }

            val nouveauJoueur = mapOf("uid" to uid, "nom" to nomJoueur, "score" to 0)

            refPartie.update("joueurs", com.google.firebase.firestore.FieldValue.arrayUnion(nouveauJoueur))
                .addOnSuccessListener { onSucces() }
                .addOnFailureListener { e -> onEchec(e) }
        }
        .addOnFailureListener { e -> onEchec(e) }
}
data class JoueurEnLigne(val uid: String = "", val nom: String = "", val score: Int = 0, val nbCartes: Int = 5)

data class PartieEnLigne(
    val joueurs: List<JoueurEnLigne> = emptyList(),
    val statut: String = "en_attente",
    val hoteUid: String = "",
    val canaillou: List<Carte> = emptyList(),
    val bourrer: List<Carte> = emptyList(),
    val indexJoueurActif: Int = 0,
    val quiCrie: String = ""
)

fun ecouterPartie(
    code: String,
    onMiseAJour: (PartieEnLigne) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("parties").document(code)
        .addSnapshotListener { snapshot, erreur ->
            if (erreur != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val joueursRaw = snapshot.get("joueurs") as? List<Map<String, Any>> ?: emptyList()
            val joueurs = joueursRaw.map {
                JoueurEnLigne(
                    uid = it["uid"] as? String ?: "",
                    nom = it["nom"] as? String ?: "",
                    score = (it["score"] as? Long)?.toInt() ?: 0,
                    nbCartes = (it["nbCartes"] as? Long)?.toInt() ?: 5
                )
            }

            val canaillouRaw = snapshot.get("canaillou") as? List<Map<String, Any>> ?: emptyList()
            val canaillou = canaillouRaw.map { mapVersCarte(it) }

            val bourrerRaw = snapshot.get("bourrer") as? List<Map<String, Any>> ?: emptyList()
            val bourrer = bourrerRaw.map { mapVersCarte(it) }

            val partie = PartieEnLigne(
                joueurs = joueurs,
                statut = snapshot.getString("statut") ?: "en_attente",
                hoteUid = snapshot.getString("hoteUid") ?: "",
                canaillou = canaillou,
                bourrer = bourrer,
                indexJoueurActif = (snapshot.getLong("indexJoueurActif"))?.toInt() ?: 0,
                quiCrie = snapshot.getString("quiCrie") ?: ""

            )

            onMiseAJour(partie)
        }
}

fun demarrerPartieEnLigne(
    code: String,
    joueurs: List<JoueurEnLigne>,
    onSucces: () -> Unit,
    onEchec: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val paquet = melangerPaquet().toMutableList()

    val mains = mutableMapOf<String, List<Map<String, String>>>()
    for (joueur in joueurs) {
        val main = mutableListOf<Map<String, String>>()
        repeat(5) {
            val carte = paquet.removeAt(0)
            main.add(mapOf("famille" to carte.famille.name, "valeur" to carte.valeur.name))
        }
        mains[joueur.uid] = main
    }

    val paquetRestant = paquet.map { mapOf("famille" to it.famille.name, "valeur" to it.valeur.name) }

    val refPartie = db.collection("parties").document(code)

    refPartie.update(
        mapOf(
            "statut" to "en_cours",
            "canaillou" to paquetRestant,
            "bourrer" to emptyList<Map<String, String>>(),
            "indexJoueurActif" to 0
        )
    ).addOnSuccessListener {
        var compteur = 0
        var erreurSurvenue = false

        for ((uid, main) in mains) {
            refPartie.collection("mains").document(uid)
                .set(mapOf("cartes" to main))
                .addOnSuccessListener {
                    compteur++
                    if (compteur == mains.size && !erreurSurvenue) onSucces()
                }
                .addOnFailureListener { e ->
                    erreurSurvenue = true
                    onEchec(e)
                }
        }
    }.addOnFailureListener { e -> onEchec(e) }
}

fun mapVersCarte(map: Map<String, Any>): Carte {
    val familleStr = map["famille"] as? String ?: "PIQUE"
    val valeurStr = map["valeur"] as? String ?: "AS"
    return Carte(
        famille = Famille.valueOf(familleStr),
        valeur = Valeur.valueOf(valeurStr)
    )
}

fun carteVersMap(carte: Carte): Map<String, String> {
    return mapOf("famille" to carte.famille.name, "valeur" to carte.valeur.name)
}

fun ecouterMaMain(
    code: String,
    onMiseAJour: (List<Carte>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    db.collection("parties").document(code)
        .collection("mains").document(uid)
        .addSnapshotListener { snapshot, erreur ->
            if (erreur != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val cartesRaw = snapshot.get("cartes") as? List<Map<String, Any>> ?: emptyList()
            val main = cartesRaw.map { mapVersCarte(it) }

            onMiseAJour(main)
        }
}

fun jouerCombinaisonEnLigne(
    code: String,
    maMainActuelle: List<Carte>,
    bourrerActuel: List<Carte>,
    selection: List<Carte>,
    onSucces: () -> Unit,
    onEchec: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val estValide = estUneCombinaisonValide(selection) || selection.size == 1

    if (!estValide) {
        onEchec("Sélection invalide")
        return
    }

    val nouvelleMain = maMainActuelle.toMutableList()
    nouvelleMain.removeAll(selection)

    val nouveauBourrer = bourrerActuel + selection

    val refPartie = db.collection("parties").document(code)
    val refMain = refPartie.collection("mains").document(uid)

    refMain.update("cartes", nouvelleMain.map { carteVersMap(it) })
        .addOnSuccessListener {
            refPartie.update("bourrer", nouveauBourrer.map { carteVersMap(it) })
                .addOnSuccessListener { onSucces() }
                .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
        }
        .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
}

fun piocherEtPasserAuSuivantEnLigne(
    code: String,
    maMainActuelle: List<Carte>,
    canaillouActuel: List<Carte>,
    bourrerActuel: List<Carte>,
    indexJoueurActifActuel: Int,
    nombreDeJoueurs: Int,
    pileEstBourrer: Boolean,
    carteCiblee: Carte? = null,
    onSucces: () -> Unit,
    onEchec: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val carte: Carte
    val nouveauCanaillou: List<Carte>
    val nouveauBourrer: List<Carte>

    if (pileEstBourrer) {
        val cible = carteCiblee ?: bourrerActuel.lastOrNull()
        if (cible == null) {
            onEchec("Le bourrer est vide")
            return
        }
        carte = cible
        nouveauCanaillou = canaillouActuel
        nouveauBourrer = bourrerActuel.toMutableList().also { it.remove(cible) }
    } else {
        if (canaillouActuel.isEmpty()) {
            onEchec("Le canaillou est vide")
            return
        }
        carte = canaillouActuel.first()
        nouveauCanaillou = canaillouActuel.drop(1)
        nouveauBourrer = bourrerActuel
    }

    val nouvelleMain = maMainActuelle + carte
    val nouvelIndex = (indexJoueurActifActuel + 1) % nombreDeJoueurs

    val refPartie = db.collection("parties").document(code)
    val refMain = refPartie.collection("mains").document(uid)

    refMain.update("cartes", nouvelleMain.map { carteVersMap(it) })
        .addOnSuccessListener {
            refPartie.update(
                mapOf(
                    "canaillou" to nouveauCanaillou.map { carteVersMap(it) },
                    "bourrer" to nouveauBourrer.map { carteVersMap(it) },
                    "indexJoueurActif" to nouvelIndex
                )
            )
                .addOnSuccessListener { onSucces() }
                .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
        }
        .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
}

fun crierPatrickEnLigne(
    code: String,
    onSucces: () -> Unit,
    onEchec: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    db.collection("parties").document(code)
        .update(mapOf("statut" to "revelation", "quiCrie" to uid))
        .addOnSuccessListener { onSucces() }
        .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
}

fun calculerScoresApresRevelationEnLigne(
    code: String,
    joueurs: List<JoueurEnLigne>,
    uidQuiCrie: String,
    onSucces: (perdantUid: String?) -> Unit,
    onEchec: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val refPartie = db.collection("parties").document(code)

    var scoresCollectes = mutableMapOf<String, Int>()
    var compteur = 0

    for (joueur in joueurs) {
        refPartie.collection("mains").document(joueur.uid).get()
            .addOnSuccessListener { snapshot ->
                val cartesRaw = snapshot.get("cartes") as? List<Map<String, Any>> ?: emptyList()
                val main = cartesRaw.map { mapVersCarte(it) }
                scoresCollectes[joueur.uid] = calculerScoreMain(main)
                compteur++

                if (compteur == joueurs.size) {
                    val scoreAppelant = scoresCollectes[uidQuiCrie] ?: 0
                    val joueursAvecMoins = joueurs.filter {
                        it.uid != uidQuiCrie && (scoresCollectes[it.uid] ?: 0) < scoreAppelant
                    }

                    val nouveauxJoueurs = joueurs.map { j ->
                        val ajout = if (joueursAvecMoins.isEmpty()) {
                            scoresCollectes[j.uid] ?: 0
                        } else if (j.uid == uidQuiCrie) {
                            scoreAppelant + 10 * joueursAvecMoins.size
                        } else {
                            0
                        }
                        mapOf("uid" to j.uid, "nom" to j.nom, "score" to (j.score + ajout))
                    }

                    val perdant = nouveauxJoueurs.find { (it["score"] as Int) >= 111 }

                    refPartie.update("joueurs", nouveauxJoueurs)
                        .addOnSuccessListener { onSucces(perdant?.get("uid") as? String) }
                        .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
                }
            }
            .addOnFailureListener { e -> onEchec(e.message ?: "Erreur") }
    }
}

fun recupererToutesLesMainsEnLigne(
    code: String,
    joueurs: List<JoueurEnLigne>,
    onResultat: (Map<String, List<Carte>>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val refPartie = db.collection("parties").document(code)

    val resultats = mutableMapOf<String, List<Carte>>()
    var compteur = 0

    for (joueur in joueurs) {
        refPartie.collection("mains").document(joueur.uid).get()
            .addOnSuccessListener { snapshot ->
                val cartesRaw = snapshot.get("cartes") as? List<Map<String, Any>> ?: emptyList()
                resultats[joueur.uid] = cartesRaw.map { mapVersCarte(it) }
                compteur++
                if (compteur == joueurs.size) {
                    onResultat(resultats)
                }
            }
            .addOnFailureListener {
                compteur++
                if (compteur == joueurs.size) {
                    onResultat(resultats)
                }
            }
    }
}