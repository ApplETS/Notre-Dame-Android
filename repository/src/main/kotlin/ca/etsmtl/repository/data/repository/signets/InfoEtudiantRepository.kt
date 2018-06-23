package ca.etsmtl.repository.data.repository.signets

import android.arch.lifecycle.LiveData
import ca.etsmtl.repository.AppExecutors
import ca.etsmtl.repository.data.api.ApiResponse
import ca.etsmtl.repository.data.api.SignetsApi
import ca.etsmtl.repository.data.db.dao.signets.EtudiantDao
import ca.etsmtl.repository.data.model.Resource
import ca.etsmtl.repository.data.model.signets.Etudiant
import ca.etsmtl.repository.data.model.signets.SignetsModel
import ca.etsmtl.repository.data.model.signets.SignetsUserCredentials
import ca.etsmtl.repository.data.repository.NetworkBoundResource
import javax.inject.Inject

/**
 * This repository provides access to the user's information.
 *
 * Created by Sonphil on 02-03-18.
 */

class InfoEtudiantRepository @Inject constructor(
    appExecutors: AppExecutors,
    private val api: SignetsApi,
    private val dao: EtudiantDao
) : SignetsRepository(appExecutors) {

    /**
     * Returns the user's information
     *
     * @param userCredentials The user's credentials
     * @param shouldFetch True if the data should be fetched from the network. False if the the data
     * should only be fetched from the DB.
     */
    fun getInfoEtudiant(userCredentials: SignetsUserCredentials, shouldFetch: Boolean): LiveData<Resource<Etudiant>> {

        return object : NetworkBoundResource<Etudiant, SignetsModel<Etudiant>>(appExecutors) {
            override fun saveCallResult(item: SignetsModel<Etudiant>) {
                item.data?.let { dao.insert(it) }
            }

            override fun shouldFetch(data: Etudiant?): Boolean {
                return shouldFetch
            }

            override fun loadFromDb(): LiveData<Etudiant> = getFirstItemLiveData(dao.getAll())

            override fun createCall(): LiveData<ApiResponse<SignetsModel<Etudiant>>> {
                return transformApiLiveData(api.infoEtudiant(userCredentials))
            }
        }.asLiveData()
    }
}
