package ca.etsmtl.etsmobile.data.model.signets

import android.arch.persistence.room.Entity
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@Entity
@JsonSerializable
data class ListeHoraireExamensFinaux(
    @Json(name = "__type") var type: String? = "",
    @Json(name = "listeHoraire") var listeHoraire: List<HoraireExamenFinal?>? = listOf(),
    @Json(name = "erreur") var erreur: String? = ""
) : SignetsData() {
    override fun getError(): String? {
        return erreur
    }
}