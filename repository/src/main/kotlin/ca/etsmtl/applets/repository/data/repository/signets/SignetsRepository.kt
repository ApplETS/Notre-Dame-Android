package ca.etsmtl.applets.repository.data.repository.signets

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations
import android.support.annotation.VisibleForTesting
import ca.etsmtl.applets.repository.AppExecutors
import ca.etsmtl.applets.repository.data.api.ApiResponse
import ca.etsmtl.applets.repository.data.api.response.signets.ApiSignetsData
import ca.etsmtl.applets.repository.data.api.response.signets.ApiSignetsModel

/**
 * On a request, Signets's web service will return a 200 status code. If an error has occurred, it's
 * pushed down to the HTTP response. This abstract class provides a handy way to handle the error
 * whether it's a network error or an error from Signets.
 *
 * Created by Sonphil on 17-03-18.
 */
abstract class SignetsRepository(protected val appExecutors: AppExecutors) {
    /**
     * Transforms a [LiveData] created by SignetsApi into a new [LiveData] whose value is an
     * [ApiResponse] that exposes the error if the request failed.
     *
     * If the request succeeded, the value is the same [ApiResponse].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    inline fun <reified T : ApiSignetsData> transformApiLiveData(apiLiveData: LiveData<ApiResponse<ApiSignetsModel<T>>>): LiveData<ApiResponse<ApiSignetsModel<T>>> {
        return Transformations.map(apiLiveData) { apiResponse ->
            with (apiResponse.networkOrSignetsError) {
                when (isNullOrEmpty()) {
                    true -> apiResponse
                    false -> ApiResponse(Throwable(this))
                }
            }
        }
    }
}

/**
 * The [ApiResponse] error. If a network error occurred, the network error
 * will be returned. If not, the error inside the payload will be returned. If the request was
 * successful, the [String] will be null.
 */
@VisibleForTesting
val ApiResponse<out ApiSignetsModel<out ApiSignetsData>>?.networkOrSignetsError: String?
    get() {
        if (this == null)
            return "No Response"

        val error = !isSuccessful || body == null

        return when (error) {
            true -> errorMessage
            false -> body.errorInsideData
        }
    }

/**
 * The value of the error field
 */
private val ApiSignetsModel<out ApiSignetsData>?.errorInsideData: String?
    get() {
        this?.data?.let { return it.erreur }

        return "No Data"
    }

/**
 * Transforms a list [LiveData] to a single item [LiveData]. The item is the first element of
 * the list.
 *
 * @param listLiveData The list [LiveData] to transform
 * @return The transformed [LiveData] which will contain the first item. The item can be null
 * if the list was empty.
 */
inline fun <reified T> LiveData<List<T>>.transformToFirstItemLiveData(): LiveData<T> {
    val resultLiveData = MediatorLiveData<T>()
    resultLiveData.addSource(this) {
        resultLiveData.value = when {
            it != null && it.isNotEmpty() -> it[0]
            else -> null
        }

        resultLiveData.removeSource(this)
    }

    return resultLiveData
}