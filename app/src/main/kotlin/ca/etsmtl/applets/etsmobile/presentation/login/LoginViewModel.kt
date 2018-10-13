package ca.etsmtl.applets.etsmobile.presentation.login

import android.app.Activity
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.Transformations
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.domain.CheckUserCredentialsValidUseCase
import ca.etsmtl.applets.etsmobile.domain.FetchSavedSignetsUserCredentialsUserCase
import ca.etsmtl.applets.etsmobile.domain.SaveSignetsUserCredentialsUseCase
import ca.etsmtl.applets.etsmobile.presentation.App
import ca.etsmtl.applets.etsmobile.presentation.about.AboutActivity
import ca.etsmtl.applets.etsmobile.presentation.main.MainActivity
import ca.etsmtl.applets.etsmobile.util.Event
import ca.etsmtl.applets.etsmobile.util.call
import ca.etsmtl.applets.repository.data.model.Resource
import ca.etsmtl.applets.repository.data.model.SignetsUserCredentials
import javax.inject.Inject

/**
 * Created by Sonphil on 28-02-18.
 */

class LoginViewModel @Inject constructor(
    private val app: App,
    private val fetchSavedSignetsUserCredentialsUserCase: FetchSavedSignetsUserCredentialsUserCase,
    private val checkUserCredentialsValidUseCase: CheckUserCredentialsValidUseCase,
    private val saveSignetsUserCredentialsUseCase: SaveSignetsUserCredentialsUseCase
) : AndroidViewModel(app), LifecycleObserver {

    private val universalCode: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val password: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val userCredentials: MutableLiveData<SignetsUserCredentials> by lazy {
        MutableLiveData<SignetsUserCredentials>()
    }
    private val _activityToGoTo by lazy { MutableLiveData<Class<out Activity>>() }
    private val showLoginFragmentMediator by lazy {
        MediatorLiveData<Void>().apply {
            addSource(userCredentialsValid) {
                it?.let {
                    if (it.status != Resource.Status.LOADING && it.data == false) {
                        this.call()
                    }
                }
            }
        }
    }
    /** An error message to be displayed to the user **/
    val errorMessage: LiveData<Event<String>> by lazy {
        MediatorLiveData<Event<String>>().apply {
            this.addSource(userCredentialsValid) {
                if (it != null && it.status == Resource.Status.ERROR) {
                    this.value = Event(it.message ?: app.getString(R.string.error))
                }
            }
        }
    }

    private val displayUniversalCodeDialogMediator: MediatorLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>()
    }

    /**
     * This [LiveData] indicates whether the user credentials are valid or not. It's a
     * [Transformations.switchMap] which is triggered when [userCredentials] is called. If the
     * response doesn't contain an error, the [SignetsUserCredentials] are considered valid and are
     * saved. Moreover, a navigation to [MainActivity] will be triggered.
     */
    private val userCredentialsValid: LiveData<Resource<Boolean>> by lazy {
        Transformations.switchMap(userCredentials) { userCredentials ->
            Transformations.map(checkUserCredentialsValidUseCase.fetchAreUserCredentialsValid(userCredentials)) {
                if (it.status != Resource.Status.LOADING && it.data == true) {
                    saveSignetsUserCredentialsUseCase.saveSignetsUserCredentials(userCredentials)
                    _activityToGoTo.value = MainActivity::class.java
                }

                it
            }
        }
    }

    /**
     * A [LiveData] which is called when the [LoginFragment] needs to be displayed
     *
     * The [LoginFragment] needs to be displayed when the user needs to login for the first time or
     * if his credentials are no longer valid.
     */
    val showLoginFragment: LiveData<Void> = showLoginFragmentMediator

    /**
     * A [LiveData] with a [Boolean] which would be set to true a loading animation should
     * be displayed
     *
     * This is triggered after the user's credentials have been submitted.
     */
    val showLoading: LiveData<Boolean> = Transformations.map(userCredentialsValid) {
        it.status == Resource.Status.SUCCESS || it.status == Resource.Status.LOADING
    }

    /**
     * A [LiveData] containing an error message related to the universal code field. The
     * error message is null if there is not error in the universal code.
     *
     * This is triggered after the universal code has been submitted.
     */
    val universalCodeError: LiveData<String> = Transformations.map(universalCode) {
        when {
            it.isEmpty() -> app.getString(R.string.error_field_required)
            !it.matches(Regex("[a-zA-Z]{2}\\d{5}")) -> app.getString(R.string.error_invalid_universal_code)
            else -> null
        }
    }

    /**
     * A [LiveData] containing an error message related to the password field. The
     * error message is null if there is not error in the password.
     *
     * This is triggered after the password has been submitted.
     */
    val passwordError: LiveData<String> = Transformations.map(password) {
        when {
            it.isEmpty() -> app.getString(R.string.error_field_required)
            else -> null
        }
    }

    /**
     * A [LiveData] containing the class of an [Activity] to navigate to.
     */
    val activityToGoTo: LiveData<Class<out Activity>> = _activityToGoTo

    /**
     * A [LiveData] which is called when the keyboard needs to be hidden
     *
     * The keyboard needs to be hidden when the user's credentials are submitted.
     */
    val hideKeyboard: LiveData<Void> = Transformations.map(userCredentials) { null }

    /**
     * A [LiveData] indicating whether the universal code info dialog should be displayed or
     * not
     */
    val displayUniversalCodeDialog: LiveData<Boolean> = displayUniversalCodeDialogMediator

    /**
     * Verifies that a given resource is not null and that his status is [Resource.SUCCESS]
     *
     * @param blnResource The [Resource] to verify
     * @return true if the resource is not null and that his status is [Resource.SUCCESS]
     */
    private fun userCredentialsValid(blnResource: Resource<Boolean>?): Boolean {
        if (blnResource != null && blnResource.status == Resource.Status.SUCCESS) {
            return blnResource.data!!
        }

        return false
    }

    /**
     * Set the user's universal code
     *
     * This will trigger a validity check for the given universal code.
     *
     * @param universalCode
     */
    fun setUniversalCode(universalCode: String) {
        this.universalCode.value = universalCode
    }

    /**
     * Set the user's password
     *
     * This will trigger a validity check for the given password.
     *
     * @param password
     */
    fun setPassword(password: String) {
        this.password.value = password
    }

    /**
     * Submits the user's credentials
     *
     * If the universal code and the password are not null, this will trigger a validity check of
     * the user's credentials.
     */
    fun submitCredentials() {
        if (universalCode.value != null && password.value != null) {
            userCredentials.value = SignetsUserCredentials(universalCode.value!!, password.value!!)
        }
    }

    /**
     * Submits the saved user's credentials on [Lifecycle.Event.ON_START]
     *
     * Calls [submitCredentials] with the saved user's credentials if they are not null
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun submitSavedCredentials() {
        with(fetchSavedSignetsUserCredentialsUserCase.fetchSavedUserCredentials()) {
            if (this == null) {
                showLoginFragmentMediator.call()
            } else {
                universalCode.value = this.codeAccesUniversel
                password.value = this.motPasse
                submitCredentials()
            }
        }
    }

    /**
     * Triggers a navigation to [AboutActivity]
     */
    fun clickOnAppletsLogo() {
        _activityToGoTo.value = AboutActivity::class.java
    }

    /**
     * Displays the information about the universal code or hides the information
     *
     * @param shouldShow True if the information should be shown or false is the information should
     * be hidden
     */
    fun displayUniversalCodeInfo(shouldShow: Boolean) {
        displayUniversalCodeDialogMediator.value = shouldShow
    }
}
