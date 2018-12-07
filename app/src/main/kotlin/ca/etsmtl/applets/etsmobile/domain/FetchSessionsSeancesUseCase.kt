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
import javax.inject.Inject

/**
Created by mykaelll87 on 17/11/18
 */
class FetchSessionsSeancesUseCase @Inject constructor(
    private val usercredentials: SignetsUserCredentials,
    private val fetchSessionsUseCase: FetchSessionsUseCase,
    private val seanceRepository: SeanceRepository,
    private val app: App
) {
    operator fun invoke(): LiveData<Resource<List<Seance>>> {
        return Transformations.switchMap(fetchSessionsUseCase()) { res ->
            val sessions = res.data
            val mediatorLiveData = MediatorLiveData<Resource<List<Seance>>>()

            fun fetchSeances() {
                when {
                    sessions == null -> mediatorLiveData.value = Resource.error(res.message ?: app.getString(R.string.error), sessions)
                    sessions.isEmpty() -> mediatorLiveData.value = Resource.success(emptyList())
                    else -> {
                        val onGoingRequests = sessions.toMutableSet()
                        val seanceList = mutableListOf<Seance>()
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
                                        mediatorLiveData.value = Resource.loading(null)
                                    Resource.Status.ERROR -> {
                                        if (it.message?.contains(app.applicationContext.getString(R.string.APIAucuneSeanceError)) == true) {
                                            onGoingRequests.remove(session)
                                            if (onGoingRequests.size == 0) {
                                                mediatorLiveData.value =
                                                    Resource.success(seanceList)
                                            } else {
                                                mediatorLiveData.value =
                                                    Resource.loading(null)
                                            }
                                        } else
                                            mediatorLiveData.value = Resource.error(app.getString(R.string.error), null)
                                    }
                                    Resource.Status.SUCCESS -> {
                                        if (seances == null) {
                                            mediatorLiveData.value = Resource.error(app.getString(R.string.error), null)
                                        } else {
                                            onGoingRequests.remove(session)
                                            seanceList.addAll(seances)
                                            if (onGoingRequests.size == 0) {
                                                mediatorLiveData.value =
                                                    Resource.success(seanceList)
                                            } else {
                                                mediatorLiveData.value =
                                                    Resource.loading(null)
                                            }
                                        }
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