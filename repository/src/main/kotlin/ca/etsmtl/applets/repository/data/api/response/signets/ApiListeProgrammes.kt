package ca.etsmtl.repository.data.api.response.signets

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ApiListeProgrammes(
    @Json(name = "__type") var type: String? = "",
    @Json(name = "liste") var liste: List<ApiProgramme> = listOf(),
    @Json(name = "erreur") var erreur: String? = ""
) : ApiSignetsData() {
    override fun getError() = erreur
}