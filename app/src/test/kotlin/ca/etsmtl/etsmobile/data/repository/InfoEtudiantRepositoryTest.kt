package ca.etsmtl.etsmobile.data.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import ca.etsmtl.etsmobile.InstantAppExecutors
import ca.etsmtl.etsmobile.data.api.ApiResponse
import ca.etsmtl.etsmobile.data.api.SignetsApi
import ca.etsmtl.etsmobile.data.db.dao.EtudiantDao
import ca.etsmtl.etsmobile.data.model.signets.Etudiant
import ca.etsmtl.etsmobile.data.model.Resource
import ca.etsmtl.etsmobile.data.model.signets.SignetsModel
import ca.etsmtl.etsmobile.data.model.signets.SignetsUserCredentials
import ca.etsmtl.etsmobile.data.repository.signets.InfoEtudiantRepository
import ca.etsmtl.etsmobile.util.ApiUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

/**
 * Created by Sonphil on 09-03-18.
 */
@RunWith(JUnit4::class)
class InfoEtudiantRepositoryTest {
    private lateinit var signetsApi: SignetsApi
    private lateinit var repo: InfoEtudiantRepository
    private lateinit var dao: EtudiantDao

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        signetsApi = mock(SignetsApi::class.java)
        dao = mock(EtudiantDao::class.java)
        repo = InfoEtudiantRepository(InstantAppExecutors(), signetsApi, dao)
    }

    @Test
    fun testLoadFromNetwork() {
        val dbData: MutableLiveData<Etudiant> = MutableLiveData()
        `when`(dao.getEtudiant()).thenReturn(dbData)

        val etudiant = Etudiant("testFoo", "foo", "foo", "foo", "0,00", true, "")
        val signetsModel = SignetsModel<Etudiant>()
        signetsModel.data = etudiant
        val call: LiveData<ApiResponse<SignetsModel<Etudiant>>> = ApiUtil.successCall(signetsModel)
        val userCredentials = SignetsUserCredentials("test", "foo")
        `when`(signetsApi.infoEtudiant(userCredentials)).thenReturn(call)

        val data: LiveData<Resource<Etudiant>> = repo.getInfoEtudiant(userCredentials, true)
        val observer = mock(Observer::class.java)
        // Start observing the LiveData returned by SignetsApi
        data.observeForever(observer as Observer<Resource<Etudiant>>)
        verifyNoMoreInteractions(signetsApi)
        // NeworkBoundResource should have posted an loading resource
        verify(observer).onChanged(Resource.loading(null))

        // Prepare fake updated db data.
        val updatedDbData: MutableLiveData<Etudiant> = MutableLiveData()
        // Return the fake updated db data when requested
        // After fetching from the network, the data will stored in the db and the db's data will be
        // returned.
        `when`(dao.getEtudiant()).thenReturn(updatedDbData)

        // Post null so that the data must be fetched from the network
        dbData.postValue(null)
        // Check if a request a send to get the updated data
        verify(signetsApi).infoEtudiant(userCredentials)
        // After fetching from the network, the data should be inserted into the db.
        verify(dao).insertEtudiant(etudiant)

        /*
         By now, the updated data has been stored in the db and NetworkBoundResource will get the
         updated the from the db.
         */
        updatedDbData.postValue(etudiant)
        verify(observer).onChanged(Resource.success(etudiant))
    }

    @Test
    fun testFailToLoadFromNetwork() {
        val dbData: MutableLiveData<Etudiant> = MutableLiveData()
        `when`(dao.getEtudiant()).thenReturn(dbData)

        val etudiant = Etudiant("testFoo", "foo", "foo", "foo", "0,00", true, "")
        val signetsModel = SignetsModel<Etudiant>()
        signetsModel.data = etudiant
        val errorMsg = "Test error"
        val call: LiveData<ApiResponse<SignetsModel<Etudiant>>> = ApiUtil.failCall(errorMsg)
        val userCredentials = SignetsUserCredentials("test", "foo")
        `when`(signetsApi.infoEtudiant(userCredentials)).thenReturn(call)

        val data: LiveData<Resource<Etudiant>> = repo.getInfoEtudiant(userCredentials, true)
        val observer = mock(Observer::class.java)
        // Start observing the LiveData returned by SignetsApi
        data.observeForever(observer as Observer<Resource<Etudiant>>)
        verifyNoMoreInteractions(signetsApi)
        // NeworkBoundResource should have posted an loading resource
        verify(observer).onChanged(Resource.loading(null))
        dbData.postValue(etudiant)
        // Check if a request has been sent to get the updated data
        verify(signetsApi).infoEtudiant(userCredentials)
        verify(dao, never()).insertEtudiant(etudiant)

        verify(observer).onChanged(Resource.error(errorMsg, etudiant))
    }

    @Test
    fun testLoadFromDb() {
        val dbData: MutableLiveData<Etudiant> = MutableLiveData()
        `when`(dao.getEtudiant()).thenReturn(dbData)

        val etudiant = Etudiant("testFoo", "foo", "foo", "foo", "0,00", true, "")
        val userCredentials = SignetsUserCredentials("test", "foo")

        val data: LiveData<Resource<Etudiant>> = repo.getInfoEtudiant(userCredentials, false)
        val observer = mock(Observer::class.java)
        // Start observing the LiveData returned by SignetsApi
        data.observeForever(observer as Observer<Resource<Etudiant>>)
        verifyNoMoreInteractions(signetsApi)
        // NeworkBoundResource should have posted an loading resource
        verify(observer).onChanged(Resource.loading(null))

        dbData.postValue(etudiant)

        // Make sure there wasn't any interaction with SignetsApi
        verifyNoMoreInteractions(signetsApi)

        verify(observer).onChanged(Resource.success(etudiant))
    }
}
