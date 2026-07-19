package com.example.patrick.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    val hoteUid: String = ""
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

            val partie = PartieEnLigne(
                joueurs = joueurs,
                statut = snapshot.getString("statut") ?: "en_attente",
                hoteUid = snapshot.getString("hoteUid") ?: ""
            )

            onMiseAJour(partie)
        }
}