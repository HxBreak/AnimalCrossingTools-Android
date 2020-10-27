package com.hxbreak.animalcrossingtools.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.di.ApplicationModule
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageHandler
import com.hxbreak.backend.BackendPacket
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.compression.JdkZlibDecoder
import io.netty.handler.codec.compression.JdkZlibEncoder
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class InstantMessageServices : LifecycleService(){

    private val serviceScope = CoroutineScope(lifecycleScope.coroutineContext + Dispatchers.IO)
    private val group = NioEventLoopGroup()
    private val b = Bootstrap()

    @Inject
    lateinit var handler: Provider<InstantMessageHandler>

    @Inject
    @AndroidId
    lateinit var androidId: Lazy<String>

    override fun onCreate() {
        super.onCreate()
        b.group(group)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel?) {
                    ch?.pipeline()?.run {
                        addLast(JdkZlibEncoder())
                        addLast(JdkZlibDecoder())
                        addLast(ProtobufVarint32LengthFieldPrepender())
                        addLast(ProtobufVarint32FrameDecoder())
                        addLast("encoder", ProtobufEncoder())
                        addLast("decoder", ProtobufDecoder(BackendPacket.ToClientPacket.getDefaultInstance()))
                        addLast(handler.get())
                    }
                }
            })
    }

    @Volatile
    var channelFuture: ChannelFuture? = null
    @Volatile
    var runningJob: Job? = null
    @Volatile
    var connectingJob: Deferred<ChannelFuture?>? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val host = intent?.getStringExtra("host")
        val port = intent?.getIntExtra("port", -1) ?: -1

        val nextConnectJob = serviceScope.async (start = CoroutineStart.LAZY){
            try {
                return@async b.connect(host, port).sync()
            }catch (e: Exception){
                return@async null
            }
        }
        serviceScope.launch {
            Timber.d("Start Job")
            Timber.e("$connectingJob $channelFuture $runningJob")
            /**
             * Close Previous Channel When Previous ConnectingJob Is Done
             */
            val future = connectingJob?.await()
            future?.channel()?.close()?.await()
            /**
             * Close Running Channel
             */
            channelFuture?.channel()?.close()?.await()
            connectingJob = nextConnectJob
            /**
             * Start Next Connecting Job
             */
            nextConnectJob.start()
            nextConnectJob.await()?.let {
                /**
                 * Setup RunningJob To Instance Field And Launch A Job Waiting For Channel Close
                 */
                connectingJob = null
                channelFuture = it
                val runningJob = serviceScope.async (start = CoroutineStart.LAZY){
                    it.channel().closeFuture().await()
                    Timber.d("Connection Close")
                    channelFuture = null
                    runningJob = null
                }
                this@InstantMessageServices.runningJob = runningJob
                runningJob.start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        channelFuture?.channel()?.close()?.await()
    }
}
