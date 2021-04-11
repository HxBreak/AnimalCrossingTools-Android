package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.character.CharUtil
import com.hxbreak.animalcrossingtools.services.UdpConnection
import com.hxbreak.backend.BackendPacket
import com.hxbreak.stun.DiscoverInfo
import com.hxbreak.stun.StunHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.model.Statement
import timber.log.Timber
import java.lang.Exception
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.coroutines.suspendCoroutine

@RunWith(JUnit4::class)
class UnitTest {

    @Test
    fun testIs() {
        println(CharUtil.toCategory(CharUtil.headPinyin("恩")))
        println(CharUtil.toCategory("1"))
        println(CharUtil.toCategory("asd12"))
        println(Duration.ofHours(12).seconds % (3600 * 24) / 3600)
    }
    @Test
    fun timeTest(){
        println(DateTimeFormatter.ISO_LOCAL_DATE.format(Instant.now().atOffset(ZoneOffset.UTC)))
    }

    @Test
    fun testClock(){
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.US)
        println(LocalDateTime.now().format(formatter))
        val now = Clock.systemDefaultZone()
        val instant = now.instant()
        println(instant.toEpochMilli() - (instant.epochSecond * 1000))
    }

    @Test
    fun testFlow(){
        val f1 = flow {
            repeat(3){
                delay(5000L)
                emit("${Math.random()}")
            }
            delay(Long.MAX_VALUE)
        }
        val f2 = flow {
            repeat(Int.MAX_VALUE){
                delay(2000L)
                emit(it)
            }
        }
        runBlocking {
            f1.combine(f2){ x, y ->
                x to y
            }.collect {
                println(it.toString())
            }
        }
    }

    @Test
    fun connectionTest(){
//        runBlocking {
//            Timber.plant(object : Timber.Tree() {
//                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                    println("$priority $tag $message")
//                }
//            })
//            val job = SupervisorJob()
//            val scope = CoroutineScope(job)
//            val conn = UdpConnection(
//                scope, 9999
//            )
//            val f = conn.connect()
//            try {
//                f.addListener {
//                    scope.launch {
//                        conn.newLoop()
//                    }
//                }
//                delay(1000L * 10)
//                scope.cancel()
//                job.join()
//            } finally {
//                f.channel().close()
//                f.channel().closeFuture().await()
//            }
//        }
    }

    @Test
    fun whileTest(){
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println("${Date()} $message")
            }
        })
        do {
            try {
                runBlocking {
                    stunTest(this)
                }
            } catch (e: Exception){
            } finally {
                println("Good Separator")
            }
        }while (true)
    }

    suspend fun stunTest(scope: CoroutineScope){
        scope.run {

            val stun1 = async (scope.coroutineContext){
                var di: DiscoverInfo? = null
                StunHelper.testNatType(0, "stun.sipgate.net", 10000)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        println(it)
                        di = it
                    }
                val udp = UdpConnection(
                    scope, di!!.localPort!!
                )
                udp.connect()
                udp to di
            }
            val stun2 = async (scope.coroutineContext){
                var di: DiscoverInfo? = null
                StunHelper.testNatType(0, "stun.sipgate.net", 10000)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        println(it)
                        di = it
                    }
                val udp = UdpConnection(
                    scope, di!!.localPort!!
                )
                udp.connect()
                udp to di
            }
            awaitAll(stun1, stun2)
            val p = BackendPacket.ToClientPacket.newBuilder().apply {
                messageType = BackendPacket.BackendMessageType.TEXT
                chatTextEntity = BackendPacket.ChatMessageEntity.newBuilder().apply {
                    toId = "..."
                    fromId = "unknown"
                    msg = "Hello This is a message from udp"
                }.build()
            }.build()

            stun1.getCompleted().first.newLoop(
                stun2.getCompleted().second?.publicIP?.hostAddress!!,
                stun2.getCompleted().second?.publicPort!!
            )
            delay(100L)
            stun2.getCompleted().first.newLoop(
                stun1.getCompleted().second?.publicIP?.hostAddress!!,
                stun1.getCompleted().second?.publicPort!!
            )
            delay(4000L)
            println("Start Stun Test")
            if (!(stun1.getCompleted().first.receivedMessage && stun2.getCompleted().first.receivedMessage)){
                error("Failed To Stun")
            }
        }
    }
}