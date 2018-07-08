package ca.etsmtl.repository.data.api.response.signets

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ApiEnseignant(
    @Json(name = "localBureau") var localBureau: String?,
    @Json(name = "telephone") var telephone: String?,
    @Json(name = "enseignantPrincipal") var enseignantPrincipal: String?,
    @Json(name = "nom") var nom: String?,
    @Json(name = "prenom") var prenom: String?,
    @Json(name = "courriel") var courriel: String
)