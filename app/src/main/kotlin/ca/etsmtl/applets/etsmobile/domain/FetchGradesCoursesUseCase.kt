package ca.etsmtl.applets.etsmobile.domain

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.presentation.App
import ca.etsmtl.applets.repository.data.model.Cours
import ca.etsmtl.applets.repository.data.model.Resource
import ca.etsmtl.applets.repository.data.model.SignetsUserCredentials
import ca.etsmtl.applets.repository.data.repository.signets.CoursRepository
import ca.etsmtl.applets.repository.data.repository.signets.EvaluationRepository
import javax.inject.Inject

/**
 * Created by Sonphil on 10-10-18.
 */

class FetchGradesCoursesUseCase @Inject constructor(
    private var userCredentials: SignetsUserCredentials,
    private val coursRepository: CoursRepository,
    private val evaluationRepository: EvaluationRepository,
    private val app: App
) {
    fun fetchGradesCourses(): LiveData<Resource<Map<String, List<Cours>>>> {
        return Transformations.switchMap(coursRepository.getCours(userCredentials, true)) {
            val mediatorLiveData = MediatorLiveData<Resource<List<Cours>>>()
            val courses = it.data.orEmpty()
            val coursesToFetchGradesFor = courses.filter {
                it.hasValidSession() && it.cote.isNullOrEmpty() && it.noteSur100.isNullOrBlank()
            }.toMutableList()

            coursesToFetchGradesFor.forEach { cours ->
                if (cours.cote.isNullOrBlank() && cours.noteSur100.isNullOrBlank()) {
                    mediatorLiveData.addSource(evaluationRepository.getEvaluationsSummary(userCredentials, cours, true)) {
                        if (it == null) {
                            mediatorLiveData.value = Resource.error(app.getString(R.string.error), courses)
                            coursesToFetchGradesFor.clear()
                        } else {
                            if (it.status == Resource.Status.LOADING) {
                                Resource.loading(courses)
                            } else {
                                coursesToFetchGradesFor.remove(cours)

                                if (coursesToFetchGradesFor.isEmpty()) {
                                    mediatorLiveData.value = Resource.success(courses)
                                }
                            }
                        }
                    }
                }
            }

            mediatorLiveData.groupBySession()
        }
    }

    private fun LiveData<Resource<List<Cours>>>.groupBySession() = Transformations.map(this) {
        val map = it.groupBySession()

        when {
            it.status == Resource.Status.LOADING -> Resource.loading(map)
            it.status == Resource.Status.SUCCESS && map != null -> Resource.success(map)
            else -> Resource.error(it.message ?: "", map)
        }
    }

    private fun Resource<List<Cours>>.groupBySession() = data?.asReversed()?.groupBy {
        it.run {
            when {
                it.session.startsWith("A") -> it.session.replaceFirst("A", app.getString(R.string.session_fall) + " ")
                it.session.startsWith("H") -> it.session.replaceFirst("H", app.getString(R.string.session_winter) + " ")
                it.session.startsWith("É") -> it.session.replaceFirst("É", app.getString(R.string.session_summer) + " ")
                else -> app.getString(R.string.session_without)
            }
        }
    }
}