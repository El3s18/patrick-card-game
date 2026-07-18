package com.example.patrick.model

fun jouerTourBot(partie: Partie, bot: Joueur): Boolean {
    val combinaison = trouverCombinaisonJouable(bot.main)

    if (combinaison != null) {
        defausserCombinaison(partie, bot, combinaison)
    } else {
        val carteAJeter = bot.main.random()
        defausserCarteUnique(partie, bot, carteAJeter)
    }

    piocherCanaillou(partie, bot)

    return calculerScoreMain(bot.main) <= 11
}