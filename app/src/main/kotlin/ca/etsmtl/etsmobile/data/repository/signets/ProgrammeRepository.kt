package ca.etsmtl.etsmobile.data.repository.signets

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import ca.etsmtl.etsmobile.AppExecutors
import ca.etsmtl.etsmobile.data.api.ApiResponse
import ca.etsmtl.etsmobile.data.api.SignetsApi
import ca.etsmtl.etsmobile.data.db.dao.ProgrammeDao
import ca.etsmtl.etsmobile.data.model.Resource
import ca.etsmtl.etsmobile.data.model.signets.ListeProgrammes
import ca.etsmtl.etsmobile.data.model.signets.Programme
import ca.etsmtl.etsmobile.data.model.signets.SignetsModel
import ca.etsmtl.etsmobile.data.model.signets.SignetsUserCredentials
import ca.etsmtl.etsmobile.data.repository.NetworkBoundResource
import javax.inject.Inject

/**
 * Created by Sonphil on 17-05-18.
 */

class ProgrammeRepository @Inject constructor(
    appExecutors: AppExecutors,
    private val api: SignetsApi,
    private val dao: ProgrammeDao
) : SignetsRepository(appExecutors) {
    fun getProgrammes(userCredentials: SignetsUserCredentials, shouldFetch: Boolean): LiveData<Resource<List<Programme>>> {

        return object : NetworkBoundResource<List<Programme>, SignetsModel<ListeProgrammes>>(appExecutors) {
            override fun saveCallResult(item: SignetsModel<ListeProgrammes>) {
                item.data?.liste?.let { dao.insertAll(*it.toTypedArray()) }
            }

            override fun shouldFetch(data: List<Programme>?): Boolean {
                return shouldFetch
            }

            override fun loadFromDb(): LiveData<List<Programme>> {
                return dao.getAll()
            }

            override fun createCall(): LiveData<ApiResponse<SignetsModel<ListeProgrammes>>> {
                return Transformations.switchMap(api.listeProgrammes(userCredentials)) { apiResponse ->
                    val resultLiveData = MutableLiveData<ApiResponse<SignetsModel<ListeProgrammes>>>()
                    val errorStr = getError(apiResponse)

                    if (errorStr.isNullOrEmpty()) {
                        resultLiveData.value = apiResponse
                    } else {
                        resultLiveData.value = ApiResponse(Throwable(errorStr))
                    }

                    resultLiveData
                }
            }
        }.asLiveData()
    }
}