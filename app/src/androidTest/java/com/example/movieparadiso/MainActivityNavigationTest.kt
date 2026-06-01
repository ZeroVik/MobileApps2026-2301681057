package com.example.movieparadiso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movieparadiso.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI test for the main user scenario: the app launches on Home and
 * the bottom navigation switches between the Library, Favorites and Stats tabs.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun homeTab_isShownOnLaunch() {
        onView(withId(R.id.btnThemeToggle)).check(matches(isDisplayed()))
    }

    @Test
    fun selectingLibraryTab_showsLibraryScreen() {
        onView(withId(R.id.nav_library)).perform(click())

        onView(withId(R.id.tvLibraryTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun selectingFavoritesTab_showsFavoritesScreen() {
        onView(withId(R.id.nav_favorites)).perform(click())

        onView(withId(R.id.tvFavoritesTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun selectingStatsTab_showsStatisticsScreen() {
        onView(withId(R.id.nav_stats)).perform(click())

        onView(withText("By Status")).check(matches(isDisplayed()))
    }
}