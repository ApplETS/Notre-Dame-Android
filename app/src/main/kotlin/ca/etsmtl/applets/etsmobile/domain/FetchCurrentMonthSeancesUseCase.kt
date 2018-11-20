package ca.etsmtl.applets.etsmobile.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.presentation.App
import ca.etsmtl.applets.repository.data.model.Resource
import ca.etsmtl.applets.repository.data.model.Seance
import ca.etsmtl.applets.repository.data.model.SignetsUserCredentials
import ca.etsmtl.applets.repository.data.repository.signets.SeanceRepository
import java.util.Date
import javax.inject.Inject

/**
Created by mykaelll87 on 17/11/18
 */
class FetchCurrentMonthSeancesUseCase @Inject constructor(
    private val usercredentials: SignetsUserCredentials,
    private val fetchCurrentMonthSessionsUseCase: FetchCurrentMonthSessionsUseCase,
    private val seanceRepository: SeanceRepository,
    private val app: App
) {
    operator fun invoke(monthTimestamp: Long): LiveData<Resource<List<Seance>>> {
        return Transformations.switchMap(fetchCurrentMonthSessionsUseCase(monthTimestamp)) { res ->
            val sessions = res.data
            val mediatorLiveData = MediatorLiveData<Resource<List<Seance>>>()

            fun fetchSeances() {
                if (sessions == null) {
                    mediatorLiveData.value = Resource.error(res.message ?: app.getString(R.string.error), sessions)
                } else {
                    sessions.forEach { session ->
                        mediatorLiveData.addSource(
                            seanceRepository.getSeancesSession(
                                usercredentials,
                                null,
                                session,
                                true
                            )
                        ) {
                            val seances = it.data

                            when (it.status) {
                                Resource.Status.LOADING ->
                                    mediatorLiveData.value = Resource.loading(seances)
                                Resource.Status.ERROR ->
                                    mediatorLiveData.value = Resource.error(app.getString(R.string.error), seances)
                                Resource.Status.SUCCESS -> {
                                    if (seances == null) {
                                        mediatorLiveData.value = Resource.error(app.getString(R.string.error), seances)
                                    } else {
                                        val nextmonthTime = Date(monthTimestamp)
                                        nextmonthTime.month++
                                        val nextMonthTimestamp = nextmonthTime.time
                                        val filteredList = seances.orEmpty().filter { seance -> (
                                            seance.dateDebut.time in monthTimestamp..nextMonthTimestamp ||
                                                seance.dateFin.time in monthTimestamp..nextMonthTimestamp)
                                        }
                                        mediatorLiveData.value = Resource.success(filteredList)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (res.status) {
                Resource.Status.ERROR -> mediatorLiveData.value =
                    Resource.error(res.message ?: app.getString(R.string.error), null)
                Resource.Status.LOADING -> mediatorLiveData.value = Resource.loading(null)
                Resource.Status.SUCCESS -> fetchSeances()
            }
            mediatorLiveData
        }
    }
}