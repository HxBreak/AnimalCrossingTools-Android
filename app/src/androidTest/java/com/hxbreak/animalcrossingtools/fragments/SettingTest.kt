package com.hxbreak.animalcrossingtools.fragments

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hxbreak.animalcrossingtools.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain

import com.hxbreak.animalcrossingtools.R
import org.junit.Test

@HiltAndroidTest
class SettingTest {

    private val hiltRule = HiltAndroidRule(this)
    private val activityTestRule = ActivityScenarioRule(MainActivity::class.java)
    private val resource = ApplicationProvider.getApplicationContext<Application>().resources

    @JvmField
    @Rule
    val rule = RuleChain
        .outerRule(hiltRule)
        .around(activityTestRule)

    @Before
    fun lunchSettingFragment(){
        activityTestRule.scenario.onActivity {
            it.navigator.navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    @Test
    fun testUi(){
        onView(withId(R.id.settings_build_date))
            .check(matches(withSubstring("Build Date:")))
    }

}