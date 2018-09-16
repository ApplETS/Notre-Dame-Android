package ca.etsmtl.repository.data.api.response.mapper

import ca.etsmtl.repository.data.api.response.signets.ApiActivite
import ca.etsmtl.repository.data.api.response.signets.ApiCours
import ca.etsmtl.repository.data.api.response.signets.ApiEnseignant
import ca.etsmtl.repository.data.api.response.signets.ApiEtudiant
import ca.etsmtl.repository.data.api.response.signets.ApiEvaluation
import ca.etsmtl.repository.data.api.response.signets.ApiHoraireExamenFinal
import ca.etsmtl.repository.data.api.response.signets.ApiJourRemplace
import ca.etsmtl.repository.data.api.response.signets.ApiListeDesElementsEvaluation
import ca.etsmtl.repository.data.api.response.signets.ApiListeDesSeances
import ca.etsmtl.repository.data.api.response.signets.ApiListeHoraireExamensFinaux
import ca.etsmtl.repository.data.api.response.signets.ApiListeJoursRemplaces
import ca.etsmtl.repository.data.api.response.signets.ApiSeance
import ca.etsmtl.repository.data.db.entity.signets.ActiviteEntity
import ca.etsmtl.repository.data.db.entity.signets.CoursEntity
import ca.etsmtl.repository.data.db.entity.signets.EnseignantEntity
import ca.etsmtl.repository.data.db.entity.signets.EtudiantEntity
import ca.etsmtl.repository.data.db.entity.signets.EvaluationEntity
import ca.etsmtl.repository.data.db.entity.signets.HoraireExamenFinalEntity
import ca.etsmtl.repository.data.db.entity.signets.JourRemplaceEntity
import ca.etsmtl.repository.data.db.entity.signets.SeanceEntity
import ca.etsmtl.repository.data.db.entity.signets.SommaireElementsEvaluationEntity
import ca.etsmtl.repository.data.model.Cours
import ca.etsmtl.repository.data.model.Session
import ca.etsmtl.repository.util.replaceCommaAndParseToDouble
import ca.etsmtl.repository.util.replaceCommaAndParseToFloat
import ca.etsmtl.repository.util.toLocaleDate
import java.text.NumberFormat
import java.util.Locale

/**
 * Created by Sonphil on 08-07-18.
 */

fun ApiActivite.toActiviteEntity() = ActiviteEntity(
        this.sigle,
        this.groupe,
        this.jour,
        this.journee,
        this.codeActivite,
        this.nomActivite,
        this.activitePrincipale,
        this.heureDebut,
        this.heureFin,
        this.local,
        this.titreCours
)

fun ApiCours.toCoursEntity(scoreFinalSur100: String) = CoursEntity(
        this.sigle,
        this.groupe,
        this.session,
        this.programmeEtudes,
        this.cote,
        scoreFinalSur100,
        this.nbCredits,
        this.titreCours
)

fun ApiEnseignant.toEnseignantEntity() = EnseignantEntity(
        this.localBureau,
        this.telephone,
        this.enseignantPrincipal,
        this.nom,
        this.prenom,
        this.courriel
)

fun ApiEtudiant.toEtudiantEntity() = EtudiantEntity(
        this.type,
        untrimmedNom.trim(),
        untrimmedPrenom.trim(),
        this.codePerm,
        this.soldeTotal,
        this.masculin
)

fun ApiEvaluation.toEvaluationEntity(cours: Cours): EvaluationEntity {
    val note = this.note.replaceCommaAndParseToDouble()
    val moyenne = this.moyenne.replaceCommaAndParseToDouble()
    var notePourcentage = 0.0
    var moyennePourcentage = 0.0

    this.corrigeSur.substringBefore("+").replace(",", ".").toDoubleOrNull()?.let {
        if (it == 0.0) {
            notePourcentage = 0.0
            moyennePourcentage = 0.0
        } else {
            notePourcentage = note / it * 100
            moyennePourcentage = moyenne / it * 100
        }
    }

    val formatter = formatter()

    return EvaluationEntity(
            cours.sigle,
            cours.groupe,
            cours.session,
            this.nom,
            this.equipe,
            dateCible.toLocaleDate(),
            formatter.format(note),
            formatter.format(corrigeSur.replaceCommaAndParseToFloat()),
            formatter.format(notePourcentage),
            formatter.format(ponderation.replaceCommaAndParseToFloat()),
            formatter.format(moyenne),
            formatter.format(moyennePourcentage),
            formatter.format(ecartType.replaceCommaAndParseToFloat()),
            formatter.format(mediane.replaceCommaAndParseToFloat()),
            formatter.format(rangCentile.replaceCommaAndParseToFloat()),
            this.publie == "Oui",
            this.messageDuProf,
            this.ignoreDuCalcul == "Oui"
    )
}

fun ApiListeDesElementsEvaluation.toEvaluationEntities(cours: Cours) = ArrayList<EvaluationEntity>().apply {
    this@toEvaluationEntities.liste.forEach {
        add(it.toEvaluationEntity(cours))
    }
}

fun ApiListeDesElementsEvaluation.toSommaireEvaluationEntity(cours: Cours): SommaireElementsEvaluationEntity {
    val formatter = formatter()

    val noteSur = liste.asSequence()
            .filter { it.note.isNotBlank() && it.ignoreDuCalcul == "Non" }
            .map { it.ponderation.replaceCommaAndParseToFloat() }
            .sum()
            .coerceAtMost(100f)

    val moyenneClassePourcentage = this.moyenneClasse.replaceCommaAndParseToFloat() / noteSur * 100

    return SommaireElementsEvaluationEntity(
            cours.sigle,
            cours.session,
            formatter.format(scoreFinalSur100.replaceCommaAndParseToFloat()),
            formatter.format(noteSur),
            formatter.format(noteACeJour.replaceCommaAndParseToFloat()),
            formatter.format(moyenneClasse.replaceCommaAndParseToFloat()),
            formatter.format(moyenneClassePourcentage),
            formatter.format(ecartTypeClasse.replaceCommaAndParseToFloat()),
            formatter.format(medianeClasse.replaceCommaAndParseToFloat()),
            formatter.format(rangCentileClasse.replaceCommaAndParseToFloat()),
            formatter.format(noteACeJourElementsIndividuels.replaceCommaAndParseToFloat()),
            formatter.format(noteSur100PourElementsIndividuels.replaceCommaAndParseToFloat())
    )
}

fun ApiHoraireExamenFinal.toHoraireExemanFinalEntity(session: Session) = HoraireExamenFinalEntity(
        this.sigle,
        this.groupe,
        this.dateExamen,
        this.heureDebut,
        this.heureFin,
        this.local,
        session.abrege
)

fun ApiListeHoraireExamensFinaux.toHoraireExamensFinauxEntities(session: Session) = ArrayList<HoraireExamenFinalEntity>().apply {
    this@toHoraireExamensFinauxEntities.listeHoraire?.forEach {
        add(it.toHoraireExemanFinalEntity(session))
    }
}

fun ApiJourRemplace.toJourRemplaceEntity(session: Session) = JourRemplaceEntity(
        this.dateOrigine,
        this.dateRemplacement,
        this.description,
        session.abrege
)

fun ApiListeJoursRemplaces.toJourRemplaceEntities(session: Session): List<JourRemplaceEntity>? {
    this.listeJours?.let {
        return ArrayList<JourRemplaceEntity>().apply {
            it.forEach {
                this.add(it.toJourRemplaceEntity(session))
            }
        }
    }

    return null
}

fun ApiSeance.toSeanceEntity(cours: Cours) = SeanceEntity(
        this.dateDebut,
        this.dateFin,
        this.nomActivite,
        this.local,
        this.descriptionActivite,
        this.libelleCours,
        cours.sigle,
        cours.session
)

fun ApiListeDesSeances.toSeancesEntities(cours: Cours): List<SeanceEntity> = ArrayList<SeanceEntity>().apply {
    this@toSeancesEntities.liste.forEach {
        add(it.toSeanceEntity(cours))
    }
}

fun formatter() = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
    maximumFractionDigits = 1
}