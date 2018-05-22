package ca.etsmtl.etsmobile.data.repository.signets

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.annotation.VisibleForTesting
import ca.etsmtl.etsmobile.AppExecutors
import ca.etsmtl.etsmobile.data.api.ApiResponse
import ca.etsmtl.etsmobile.data.model.signets.ListeProgrammes
import ca.etsmtl.etsmobile.data.model.signets.SignetsData
import ca.etsmtl.etsmobile.data.model.signets.SignetsModel

/**
 * On a request, Signets's web service will return a 200 status code. If an error has occurred, it's
 * pushed down to the HTTP response. This abstract class provides a handy way to get the error
 * whether it's a network error or an error from Signets.
 *
 * Created by Sonphil on 17-03-18.
 */
abstract class SignetsRepository(protected val appExecutors: AppExecutors) {

    /**
     * Returns the [ApiResponse] error. If a network error occurred, the network error
     * will be returned. If not, the error inside the payload will be returned. If the request was
     * successful, the [String] returned will be null.
     *
     * @return The [ApiResponse] error
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun getError(apiResponse: ApiResponse<out SignetsModel<out SignetsData>>?): String? {
        if (apiResponse == null)
            return "No Response"

        val error = !apiResponse.isSuccessful || apiResponse.body == null

        return when (error) {
            true -> apiResponse.errorMessage
            false -> getErrorInsideData(apiResponse.body)
        }
    }

    /**
     * Returns the value of the error's field
     *
     * @return The value of the error's field
     */
    private fun getErrorInsideData(signetsModel: SignetsModel<out SignetsData>?): String? {
        return when (signetsModel?.data == null) {
            true -> "No Data"
            false -> signetsModel!!.data!!.getError()
        }
    }
    
    protected inline fun <reified T: SignetsData> transformsApiLiveData(apiLiveData: LiveData<ApiResponse<SignetsModel<T>>>): LiveData<ApiResponse<SignetsModel<T>>> {
        return Transformations.switchMap(apiLiveData) { apiResponse ->
            val resultLiveData = MutableLiveData<ApiResponse<SignetsModel<T>>>()
            val errorStr = getError(apiResponse)

            if (errorStr.isNullOrEmpty()) {
                resultLiveData.value = apiResponse
            } else {
                resultLiveData.value = ApiResponse(Throwable(errorStr))
            }

            resultLiveData
        }
    }
}