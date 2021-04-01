package com.hxbreak.animalcrossingtools.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageHandler
import com.hxbreak.backend.BackendPacket
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.MessageToByteEncoder
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

    @Volatile
    var channelFuture: ChannelFuture? = null

    override fun onCreate() {
        super.onCreate()
        b.group(group)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel?) {
                    ch?.pipeline()?.run {
                        addLast(object : MessageToByteEncoder<ByteBuf>(){
                            override fun encode(
                                ctx: ChannelHandlerContext?,
                                msg: ByteBuf?,
                                out: ByteBuf?
                            ) {
                                out?.writeBytes(msg)
                                Timber.e(msg.toString())
                            }
                        })
                        addLast(JdkZlibEncoder())
                        addLast(JdkZlibDecoder())
                        addLast(ProtobufVarint32LengthFieldPrepender())
                        addLast(ProtobufVarint32FrameDecoder())
                        addLast("encoder", ProtobufEncoder())
                        addLast(
                            "decoder",
                            ProtobufDecoder(BackendPacket.ToClientPacket.getDefaultInstance())
                        )
                        addLast(handler.get())
                    }
                }
            })
        val job = serviceScope.async(start = CoroutineStart.LAZY){
            try {
                channelFuture = b.connect("192.168.0.104", 19999).sync()
                return@async channelFuture
            }catch (e: Exception){
                e.printStackTrace()
                return@async null
            }
        }
        job.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        channelFuture?.channel()?.close()?.await()
    }
}
