package ca.etsmtl.repository.data.model

data class Seance(
    var dateDebut: String,
    var dateFin: String,
    var cours: String,
    var groupe: String,
    var nomActivite: String,
    var local: String,
    var descriptionActivite: String,
    var libelleCours: String
)