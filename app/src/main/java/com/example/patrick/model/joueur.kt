package com.example.patrick.model

data class Joueur(
    val nom: String,
    val main: MutableList<Carte>,
    var score: Int = 0
)


