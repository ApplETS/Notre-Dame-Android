package ca.etsmtl.etsmobile.di.activitymodule

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import ca.etsmtl.etsmobile.di.ViewModelKey
import ca.etsmtl.etsmobile.presentation.login.LoginActivity
import ca.etsmtl.etsmobile.presentation.login.LoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Sonphil on 15-03-18.
 */
@Module
interface MainActivityModule {
    @Binds
    fun providesAppCompatActivity(activity: LoginActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    fun bindLoginViewModel(
            loginViewModel: LoginViewModel
    ): ViewModel
}