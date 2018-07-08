package ca.etsmtl.repository.data.api.response.signets

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ApiListeDesActivitesEtProf(
    @Json(name = "__type") val type: String? = "",
    @Json(name = "listeActivites") val listeActivites: List<ApiActivite>? = listOf(),
    @Json(name = "listeEnseignants") val listeEnseignants: List<ApiEnseignant>? = listOf(),
    @Json(name = "erreur") val erreur: String? = ""
) : ApiSignetsData() {
    override fun getError() = erreur
}