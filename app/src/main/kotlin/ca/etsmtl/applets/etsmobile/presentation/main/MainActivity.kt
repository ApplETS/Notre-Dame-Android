package ca.etsmtl.applets.etsmobile.presentation.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.presentation.BaseActivity
import ca.etsmtl.applets.etsmobile.presentation.dashboard.DashboardFragment
import ca.etsmtl.applets.etsmobile.presentation.ets.EtsFragment
import ca.etsmtl.applets.etsmobile.presentation.more.MoreFragment
import ca.etsmtl.applets.etsmobile.presentation.schedule.ScheduleFragment
import ca.etsmtl.applets.etsmobile.presentation.student.StudentFragment
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

    companion object {
        private const val CURRENT_FRAGMENT_TAG_KEY = "CurrentFragmentTag"
    }

    private lateinit var currentFragmentTag: String
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        goToFragment(item)

        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            selectDashboard()
        } else { // Another fragment is being displayed
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG_KEY) ?: ""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_TAG_KEY, currentFragmentTag)

        super.onSaveInstanceState(outState)
    }

    /**
     * Displays the fragment corresponding to the selected item. The fragment is instantiated if it
     * doesn't exist yet.
     *
     * @param navigationItem the item the user selected in the bottom navigation view
     */
    private fun goToFragment(navigationItem: MenuItem) {
        val fragmentManager = supportFragmentManager
        val fragmentTag: String = navigationItem.itemId.toString()
        var fragment = fragmentManager.findFragmentByTag(fragmentTag)

        if (fragment == null) {
            fragment = getNewFragment(navigationItem.itemId)
        }

        if (fragment != null) {
            currentFragmentTag = fragmentTag
            with(fragmentManager.beginTransaction()) {
                replace(R.id.content, fragment, fragmentTag)
                addToBackStack(fragmentTag)
                commit()
            }
        }
    }

    /**
     * Creates a new fragment corresponding to the selected item
     *
     * @param selectedItemId the id of the item selected in the bottom navigation view
     * @return the fragment corresponding to the selected item
     */
    private fun getNewFragment(selectedItemId: Int): Fragment? = when (selectedItemId) {
        R.id.navigation_dashboard -> DashboardFragment.newInstance()
        R.id.navigation_schedule -> ScheduleFragment.newInstance()
        R.id.navigation_profile -> StudentFragment.newInstance()
        R.id.navigation_ets -> EtsFragment.newInstance()
        R.id.navigation_more -> MoreFragment.newInstance()
        else -> null
    }

    /**
     * On back pressed, lets the current fragment handles the event if it's an instance of
     * [MainFragment] and if it's not null.
     * If the event hasn't been consumed, returns to the dashboard or close if the dashboard has
     * already been selected
     */
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)

        if ((currentFragment as? MainFragment)?.onBackPressed() == true) {
            return // Return if the fragment has consumed the event
        }

        if (navigation.selectedItemId != R.id.navigation_dashboard) {
            selectDashboard()
        } else {
            finishAndRemoveTask()
        }
    }

    private fun selectDashboard() {
        val homeMenuItem = navigation.menu.findItem(R.id.navigation_dashboard)
        homeMenuItem.isChecked = true
        onNavigationItemSelectedListener.onNavigationItemSelected(homeMenuItem)
    }

    fun getBottomNavigationView(): BottomNavigationView = navigation
}
