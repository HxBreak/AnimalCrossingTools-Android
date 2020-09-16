package com.hxbreak.animalcrossingtools

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class Tester {

    @get:Rule
    val rule = DaggerTestApplicationRule()

    lateinit var repository: DataRepository

    @Before
    fun setupDagger() {
        repository = rule.component.repository
    }

    @Test
    fun eq() {
        runBlocking {
            val list = when (val result = repository.fishSource().allFish()) {
                is Result.Success -> result.data
                else -> Collections.emptyList()
            }
            assert(list.size == 80)
        }
    }

    @Test
    fun testHousewareApi(){
        runBlocking {
            val result = repository.repoSource().service.allHousewares()
            when (result){
                is Result.Success -> result.data.entries.flatMap { data -> data.value.map { "${data.key}-${it.variant}" } }.forEach {
                    Log.e("HxBreak", it)
                }
                is Result.Error -> result.exception.printStackTrace()
            }
            assert(result is Result.Success)
        }
    }
}