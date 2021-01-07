package com.hxbreak.stun


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.Executors


class StunHelperTest {

    @Test
    fun test() {
        runBlocking {
            StunHelper.testNatType(0, "stun.sipgate.net", 10000)
                .flowOn(Dispatchers.IO)
                .collect {
                    println(it)
                }
        }
    }



}