package ca.etsmtl.applets.etsmobile.presentation.main

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.presentation.BaseActivity
import kotlinx.android.synthetic.main.activity_main.navigation

/**
 * A screen which displays a bottom navigation view and wrapper for fragment. The user can
 * select items on the bottom navigation view to switch between fragments.
 *
 * This activity is displayed when the is user logged in.
 *
 * Created by Sonphil on 24-02-18.
 */

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.fragment_nav_host_main)

        navigation.setupWithNavController(navController)
        navigation.setOnNavigationItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_nav_host_main).navigateUp()
    }
}
