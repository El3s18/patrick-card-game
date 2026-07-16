package com.example.patrick.model

enum class Famille {
    PIQUE, COEUR, CARREAU, TREFLE
}
enum class Valeur(val point: Int) {
    AS(1),
    DEUX(2),
    TROIS(3),
    QUATRE(4),
    CINQ(5),
    SIX(6),
    SEPT(7),
    HUIT(8),
    NEUF(9),
    DIX(10),
    VALET(10),
    DAME(10),
    ROI(10)
}

data class Carte(val famille: Famille, val valeur: Valeur)

fun creerPaquet (): List<Carte> {
    val cartes = mutableListOf<Carte>()
    for (famille in Famille.entries) {
        for (valeur in Valeur.entries) {
            cartes.add(Carte(famille, valeur))
        }
    }
    return cartes
}


