package ca.etsmtl.etsmobile.presentation

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import ca.etsmtl.etsmobile.presentation.profile.ProfileViewModel
import ca.etsmtl.etsmobile.util.mock
import ca.etsmtl.repository.data.model.Resource
import ca.etsmtl.repository.data.model.signets.Etudiant
import ca.etsmtl.repository.data.model.signets.SignetsUserCredentials
import ca.etsmtl.repository.data.repository.signets.InfoEtudiantRepository
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify

/**
 * Created by Sonphil on 28-04-18.
 */

class ProfileViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private var repository: InfoEtudiantRepository = mock(InfoEtudiantRepository::class.java)
    private val userCredentials: SignetsUserCredentials = SignetsUserCredentials("test", "test")
    private val infoEtudiantViewModel = ProfileViewModel(repository, userCredentials)

    @Test
    fun testCallRepo() {
        val foo = MutableLiveData<Resource<Etudiant>>()
        `when`(repository.getInfoEtudiant(userCredentials, true)).thenReturn(foo)

        infoEtudiantViewModel.refresh()

        verify(repository).getInfoEtudiant(userCredentials, true)
    }

    @Test
    fun testSendResultToUI() {
        val foo = MutableLiveData<Resource<Etudiant>>()
        `when`(repository.getInfoEtudiant(userCredentials, true)).thenReturn(foo)

        val etudiantObserver = mock<Observer<Etudiant>>()
        verify(etudiantObserver, Mockito.never()).onChanged(ArgumentMatchers.any())
        val loadingObserver = mock<Observer<Boolean>>()
        verify(loadingObserver, Mockito.never()).onChanged(ArgumentMatchers.any())
        val errorMsgObserver = mock<Observer<String>>()
        verify(errorMsgObserver, Mockito.never()).onChanged(ArgumentMatchers.any())

        infoEtudiantViewModel.getEtudiant().observeForever(etudiantObserver)
        infoEtudiantViewModel.getLoading().observeForever(loadingObserver)
        infoEtudiantViewModel.getErrorMessage().observeForever(errorMsgObserver)

        infoEtudiantViewModel.refresh()

        var fooRes: Resource<Etudiant> = Resource.loading(null)
        foo.value = fooRes
        verify(etudiantObserver).onChanged(null)
        verify(loadingObserver).onChanged(true)
        verify(errorMsgObserver).onChanged(null)

        reset(etudiantObserver)
        reset(loadingObserver)
        reset(errorMsgObserver)

        val fooEtudiant = Etudiant("testFoo", "foo", "foo", "foo", "0,00", true)
        fooRes = Resource.success(fooEtudiant)
        foo.value = fooRes
        verify(etudiantObserver).onChanged(fooEtudiant)
        verify(loadingObserver).onChanged(false)
        verify(errorMsgObserver).onChanged(null)

        reset(etudiantObserver)
        reset(loadingObserver)
        reset(errorMsgObserver)

        val errorMsg = "Test error"
        fooRes = Resource.error(errorMsg, null)
        foo.value = fooRes
        verify(etudiantObserver).onChanged(null)
        verify(loadingObserver).onChanged(false)
        verify(errorMsgObserver).onChanged(errorMsg)
    }
}