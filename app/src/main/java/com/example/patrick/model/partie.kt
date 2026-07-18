package com.example.patrick.model

data class Partie(
    val joueurs: List<Joueur>,
    val canaillou: MutableList<Carte>,
    val bourrer: MutableList<Carte>
)

fun piocherCanaillou(partie: Partie, joueur: Joueur) {
    val carte = partie.canaillou.removeAt(0)
    joueur.main.add(carte)
}

fun piocherBourrer(partie: Partie, joueur: Joueur) {
    val carte = partie.bourrer.removeAt(partie.bourrer.size - 1)
    joueur.main.add(carte)
}

fun defausserCombinaison(partie: Partie, joueur: Joueur, combinaison: List<Carte>): Boolean {
    if (!estUneCombinaisonValide(combinaison)) {
        return false
    }
    joueur.main.removeAll(combinaison)
    partie.bourrer.addAll(combinaison)
    return true
}

fun defausserCarteUnique(partie: Partie, joueur: Joueur, carte: Carte) {
    joueur.main.remove(carte)
    partie.bourrer.add(carte)
}

fun piocherCarteDuBourrer(partie: Partie, joueur: Joueur, carte: Carte) {
    partie.bourrer.remove(carte)
    joueur.main.add(carte)
}