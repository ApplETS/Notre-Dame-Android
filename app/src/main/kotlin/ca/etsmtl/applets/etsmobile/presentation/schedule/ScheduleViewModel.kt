package ca.etsmtl.applets.etsmobile.presentation.schedule

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ca.etsmtl.applets.etsmobile.domain.FetchSessionsSeancesUseCase
import ca.etsmtl.applets.etsmobile.presentation.App
import ca.etsmtl.applets.etsmobile.util.Event
import ca.etsmtl.applets.etsmobile.util.getGenericErrorMessage
import ca.etsmtl.applets.repository.data.model.Resource
import ca.etsmtl.applets.repository.data.model.Seance
import javax.inject.Inject

/**
 * Created by mykaelll87 on 2018-10-24
 */
class ScheduleViewModel @Inject constructor(
    private val fetchSessionsSeancesUseCase: FetchSessionsSeancesUseCase,
    private val app: App
) : ViewModel(), LifecycleObserver {
    private val seancesMediatorLiveData: MediatorLiveData<Resource<List<Seance>>> by lazy {
        MediatorLiveData<Resource<List<Seance>>>()
    }
    private var seancesLiveData: LiveData<Resource<List<Seance>>>? = null

    val errorMessage: LiveData<Event<String?>> by lazy {
        Transformations.map(seancesMediatorLiveData) {
            it.getGenericErrorMessage(app)
        }
    }
    val seances: LiveData<List<Seance>> = Transformations.map(seancesMediatorLiveData) {
        it.data
    }

    val loading: LiveData<Boolean> = Transformations.map(seancesMediatorLiveData) {
        it.status == Resource.Status.LOADING
    }

    val showEmptyView: LiveData<Boolean> = Transformations.map(seancesMediatorLiveData) {
        it.status != Resource.Status.LOADING && (it?.data == null || it.data?.isEmpty() == true)
    }

    private fun load() {
        seancesLiveData = fetchSessionsSeancesUseCase().apply {
            seancesMediatorLiveData.addSource(this) {
                seancesMediatorLiveData.value = it
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun refresh() {
        seancesLiveData?.let { seancesMediatorLiveData.removeSource(it) }
        seancesLiveData = null
        load()
    }
}