package ca.etsmtl.applets.etsmobile.presentation.profile

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.domain.FetchEtudiantUseCase
import ca.etsmtl.applets.etsmobile.domain.FetchProgrammesUseCase
import ca.etsmtl.applets.etsmobile.presentation.App
import ca.etsmtl.applets.repository.data.model.Etudiant
import ca.etsmtl.applets.repository.data.model.Programme
import ca.etsmtl.applets.repository.data.model.Resource
import ca.etsmtl.applets.repository.util.zipResourceTo
import com.xwray.groupie.Section
import javax.inject.Inject

/**
 * Created by Sonphil on 15-03-18.
 */
class ProfileViewModel @Inject constructor(
    private val fetchEtudiantUseCase: FetchEtudiantUseCase,
    private val fetchProgrammesUseCase: FetchProgrammesUseCase,
    private val app: App
) : ViewModel(), LifecycleObserver {

    private val profileMediatorLiveData: MediatorLiveData<Resource<List<Section>>> by lazy {
        MediatorLiveData<Resource<List<Section>>>()
    }
    val profile: LiveData<List<Section>> = Transformations.map(profileMediatorLiveData) {
        it.data
    }
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    val errorMessage: LiveData<String> = Transformations.map(profileMediatorLiveData) {
        it.message
    }
    private var etudiantProgrammesPair: LiveData<Resource<Pair<Etudiant?, List<Programme>?>>>? = null

    private fun load() {
        etudiantProgrammesPair = (fetchEtudiantUseCase { true } zipResourceTo fetchProgrammesUseCase()).apply {
            profileMediatorLiveData.addSource(this) { res ->
                _loading.value = res.status == Resource.Status.LOADING

                val sections = mutableListOf<Section>()

                if (res.status != Resource.Status.LOADING) {
                    val etudiant = res.data?.first
                    val programmes = res.data?.second

                    etudiant?.let { it.toSections().forEach { sections.add(it) } }
                    programmes?.let { it.toSections().forEach { sections.add(it) } }

                    profileMediatorLiveData.value = res.copyStatusAndMessage(sections)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun refresh() {
        etudiantProgrammesPair?.let { profileMediatorLiveData.removeSource(it) }
        etudiantProgrammesPair = null
        load()
    }

    private fun Etudiant.toSections() = mutableListOf<Section>().apply {
        add(
                Section().apply {
                    setHeader(ProfileHeaderItem(app.getString(R.string.title_student_status_profile)))
                    add(ProfileItem(app.getString(R.string.label_balance_profile), soldeTotal))
                }
        )

        add(
                Section().apply {
                    setHeader(ProfileHeaderItem(app.getString(R.string.title_personal_information_profile)))
                    add(ProfileItem(app.getString(R.string.label_first_name_profile), prenom))
                    add(ProfileItem(app.getString(R.string.label_last_name_profile), nom))
                    add(ProfileItem(app.getString(R.string.label_permanent_code_profile), codePerm))
                }
        )
    }

    private fun List<Programme>.toSections() = mutableListOf<Section>().apply {
        this@toSections.reversed().forEach {
            add(
                    Section().apply {
                        setHeader(ProfileHeaderItem(it.libelle))
                        add(ProfileItem(app.getString(R.string.label_code_program_profile), it.code))
                        add(ProfileItem(app.getString(R.string.label_average_program_profile), it.moyenne))
                        add(ProfileItem(app.getString(R.string.label_number_accumulated_credits_program_profile), it.nbCrsReussis.toString()))
                        add(ProfileItem(app.getString(R.string.label_number_registered_credits_program_profile), it.nbCreditsInscrits.toString()))
                        add(ProfileItem(app.getString(R.string.label_number_completed_courses_program_profile), it.nbCreditsCompletes.toString()))
                        add(ProfileItem(app.getString(R.string.label_number_failed_courses_program_profile), it.nbCrsEchoues.toString()))
                        add(ProfileItem(app.getString(R.string.label_number_equivalent_courses_program_profile), it.nbEquivalences.toString()))
                        add(ProfileItem(app.getString(R.string.label_status_program_profile), it.statut))
                    }
            )
        }
    }
}