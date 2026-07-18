package com.example.patrick.model

fun calculerScoreMain(main: List<Carte>): Int {
    return main.sumOf { it.valeur.point }
}
