package com.example.patrick.model

fun jouerTourBot(partie: Partie, bot: Joueur, difficile: Boolean = false): Boolean {
    val combinaison = if (difficile) {
        trouverMeilleureCombinaisonJouable(bot.main)
    } else {
        trouverCombinaisonJouable(bot.main)
    }

    if (combinaison != null) {
        defausserCombinaison(partie, bot, combinaison)
    } else {
        val carteAJeter = if (difficile) {
            bot.main.maxByOrNull { calculerScoreMain(listOf(it)) } ?: bot.main.random()
        } else {
            bot.main.random()
        }
        defausserCarteUnique(partie, bot, carteAJeter)
    }

    piocherCanaillou(partie, bot)

    return calculerScoreMain(bot.main) <= 11
}