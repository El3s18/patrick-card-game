package com.example.patrick.data

import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.screens.BoutonMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.patrick.model.Carte
import com.example.patrick.model.Famille
import com.example.patrick.model.Valeur
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
data class JoueurEnLigne(val uid: String = "", val nom: String = "", val score: Int = 0)

data class PartieEnLigne(
    val joueurs: List<JoueurEnLigne> = emptyList(),
    val statut: String = "en_attente",
    val hoteUid: String = "",
    val canaillou: List<Carte> = emptyList(),
    val bourrer: List<Carte> = emptyList(),
    val indexJoueurActif: Int = 0
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
                    score = (it["score"] as? Long)?.toInt() ?: 0
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
                indexJoueurActif = (snapshot.getLong("indexJoueurActif"))?.toInt() ?: 0
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