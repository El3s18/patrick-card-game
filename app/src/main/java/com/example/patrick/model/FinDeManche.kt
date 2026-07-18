package com.example.patrick.model

fun terminerManche(partie: Partie, joueurQuiCrie: Joueur) {
    val scoreAppelant = calculerScoreMain(joueurQuiCrie.main)
    val joueursAvecMoins = partie.joueurs.filter {
        it != joueurQuiCrie && calculerScoreMain(it.main) < scoreAppelant
    }

    if (joueursAvecMoins.isEmpty()) {
        // Personne n'a moins : tout le monde marque sa propre main
        for (joueur in partie.joueurs) {
            joueur.score += calculerScoreMain(joueur.main)
        }
    } else {
        // Quelqu'un a moins : seul l'appelant marque, avec bonus
        joueurQuiCrie.score += scoreAppelant + 10 * joueursAvecMoins.size
    }
}

fun trouverPerdant(partie: Partie): Joueur? {
    return partie.joueurs.find { it.score >= 111 }
}

fun trouverGagnant(partie: Partie): Joueur {
    return partie.joueurs.minBy { it.score }
}

