package com.example.tpandroidlistedecontacts

import java.io.Serializable

/**
 * Modèle d'un contact. Serializable permet de le transmettre entre
 * MainActivity et DetailActivity via un Intent explicite.
 */
data class Contact(
    val id: Long,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoPath: String? = null
) : Serializable {

    val nomComplet: String
        get() = "$prenom $nom".trim()
}
