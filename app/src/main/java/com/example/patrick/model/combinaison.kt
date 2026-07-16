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

