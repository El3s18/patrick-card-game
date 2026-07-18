package com.example.patrick.model

fun estUnePaire(cartes: List<Carte>): Boolean {
    if (cartes.size != 2) return false
    return cartes[0].valeur == cartes[1].valeur
}

fun estUnBrelan(cartes: List<Carte>): Boolean {
    if (cartes.size != 3) return false
    return cartes[0].valeur == cartes[1].valeur && cartes[1].valeur == cartes[2].valeur
}

fun estUnCarre(cartes: List<Carte>): Boolean {
    if (cartes.size != 4) return false
    return cartes[0].valeur == cartes[1].valeur && cartes[1].valeur == cartes[2].valeur && cartes[2].valeur == cartes[3].valeur
}

fun estUnZobi(cartes: List<Carte>): Boolean {
    if (cartes.size != 3) return false
    val cartesTriees = cartes.sortedBy { it.valeur.ordinal }
    return cartesTriees[1].valeur.ordinal == cartesTriees[0].valeur.ordinal + 1 &&
            cartesTriees[2].valeur.ordinal == cartesTriees[1].valeur.ordinal + 1
}

fun estUnZobiLaMouche(cartes: List<Carte>): Boolean {
    if (cartes.size != 5) return false
    val cartesTriees = cartes.sortedBy { it.valeur.ordinal }

    for (i in 0..3) {
        if (cartesTriees[i + 1].valeur.ordinal != cartesTriees[i].valeur.ordinal + 1) {
            return false
        }
    }
    return true
}

fun estUneCouleur(cartes: List<Carte>): Boolean {
    if (cartes.size != 5) return false

    for (i in 0..3) {
        if (cartes[i + 1].famille != cartes[i].famille) {
            return false
        }
    }
    return true
}

fun estUneCombinaisonValide(cartes: List<Carte>): Boolean {
    return estUnePaire(cartes) ||
            estUnBrelan(cartes) ||
            estUnCarre(cartes) ||
            estUnZobi(cartes) ||
            estUnZobiLaMouche(cartes) ||
            estUneCouleur(cartes)
}

fun combinaisons(cartes: List<Carte>, taille: Int): List<List<Carte>> {
    if (taille == 0) return listOf(listOf())
    if (cartes.isEmpty()) return listOf()

    val premiere = cartes.first()
    val reste = cartes.drop(1)

    val avecPremiere = combinaisons(reste, taille - 1).map { listOf(premiere) + it }
    val sansPremiere = combinaisons(reste, taille)

    return avecPremiere + sansPremiere
}

fun trouverCombinaisonJouable(main: List<Carte>): List<Carte>? {
    for (taille in 2..5) {
        val sousGroupes = combinaisons(main, taille)
        for (groupe in sousGroupes) {
            if (estUneCombinaisonValide(groupe)) {
                return groupe
            }
        }
    }
    return null
}