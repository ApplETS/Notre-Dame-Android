package ca.etsmtl.repository.data.model.signets

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@Entity
@JsonSerializable
data class Etudiant(
    @Json(name = "__type")
    var type: String,
    @Json(name = "nom")
    var untrimmedNom: String,
    @Json(name = "prenom")
    var untrimmedPrenom: String,
    @PrimaryKey
    @Json(name = "codePerm")
    var codePerm: String,
    @Json(name = "soldeTotal")
    var soldeTotal: String,
    @Json(name = "masculin")
    var masculin: Boolean,
    @Json(name = "erreur")
    var erreur: String? = null
) : SignetsData() {
    val nom: String
                get() = untrimmedNom.trim()
    val prenom: String
                get() = untrimmedPrenom.trim()

    override fun getError() = erreur
}
