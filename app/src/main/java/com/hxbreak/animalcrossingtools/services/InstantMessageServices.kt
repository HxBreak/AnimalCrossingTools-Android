package com.hxbreak.animalcrossingtools.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.protobuf.ExtensionRegistryLite
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageHandler
import com.hxbreak.animalcrossingtools.services.network.codec.WrappedDatagramPacketDecoder
import com.hxbreak.backend.BackendPacket
import com.hxbreak.stun.DiscoverInfo
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DatagramPacketDecoder
import io.netty.handler.codec.DatagramPacketEncoder
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.compression.JdkZlibDecoder
import io.netty.handler.codec.compression.JdkZlibEncoder
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.*
import javax.inject.Inject
import javax.inject.Provider

class UdpConnection(
    private val scope: CoroutineScope,
    private val localPort: Int){

    private var _channelFuture: ChannelFuture? = null
    private val channelFuture: ChannelFuture
        get() = _channelFuture!!

    var receivedMessage = false

    suspend fun connect(): ChannelFuture {
        return withContext(scope.coroutineContext){
            val b = Bootstrap()
            val eventLoopGroup = NioEventLoopGroup()
            _channelFuture = b.group(eventLoopGroup)
                .channel(NioDatagramChannel::class.java)
                .handler(object : ChannelInitializer<DatagramChannel>(){
                    override fun initChannel(ch: DatagramChannel?) {
                        ch?.pipeline()?.run {
                            addLast(WrappedDatagramPacketDecoder(ProtobufDecoder(BackendPacket.ToClientPacket.getDefaultInstance(), ExtensionRegistryLite.newInstance())))
                            addLast(DatagramPacketEncoder<BackendPacket.ToClientPacket>(ProtobufEncoder()))
                            addLast(object : SimpleChannelInboundHandler<BackendPacket.ToClientPacket>(){
                                override fun channelRead0(
                                    ctx: ChannelHandlerContext?,
                                    msg: BackendPacket.ToClientPacket?
                                ) {
                                    receivedMessage = true
                                    Timber.e("recv $msg")
                                }
                            })
                            addLast(object : SimpleChannelInboundHandler<Any>(){
                                override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
                                    Timber.e("HxBreak ChannelIn $msg")
                                }
                            })
                        }
                    }
                })
                .bind(localPort)
            return@withContext channelFuture
        }
    }

    val pool = Executors.newCachedThreadPool()

    fun newLoop(addr: String, port: Int){
        Timber.e("Submit A Task")
        scope.launch(start = CoroutineStart.ATOMIC) {
            val packet = BackendPacket.ToClientPacket.newBuilder().apply {
                messageType = BackendPacket.BackendMessageType.TEXT
            }
            val envelope = DefaultAddressedEnvelope(packet, InetSocketAddress(addr, port))
            Timber.e("sending")
            while (scope.isActive){
                channelFuture.channel().writeAndFlush(envelope)
                delay(10L)
            }
            Timber.e("end")
        }
    }
}

object DiscoverManager{
    val queue = LinkedBlockingQueue<DiscoverInfo>()
}

@AndroidEntryPoint
class InstantMessageServices : LifecycleService(){

    private val serviceScope = CoroutineScope(lifecycleScope.coroutineContext + Dispatchers.IO)
    private val group = NioEventLoopGroup()
    private val b = Bootstrap()

    private val connectionPool = MutableStateFlow(emptyList<Int>())

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
//                        addLast(object : MessageToByteEncoder<ByteBuf>(){
//                            override fun encode(
//                                ctx: ChannelHandlerContext?,
//                                msg: ByteBuf?,
//                                out: ByteBuf?
//                            ) {
//                                out?.writeBytes(msg)
//                                Timber.e(msg.toString())
//                            }
//                        })
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
                        addLast(object : SimpleChannelInboundHandler<BackendPacket.ToClientPacket>(){
                            override fun channelRead0(
                                ctx: ChannelHandlerContext?,
                                msg: BackendPacket.ToClientPacket?
                            ) {
                                msg?.let {
                                    when (msg.messageType){
                                        BackendPacket.BackendMessageType.STUN_INFO_SWAP -> {
                                            val publicIp = msg.stunInfoReply.publicAddr ?: return
                                            val localIp = msg.stunInfoReply.localAddr ?: return
                                            val publicPort = msg.stunInfoReply.publicPort
                                            val di = DiscoverManager.queue.poll()
                                            Timber.e("Connect To $publicIp:$publicPort")
                                            connectTo(publicIp, publicPort, di.localPort ?: error("localPort is None"))
                                        }
                                        else -> Unit
                                    }
                                }
                            }
                        })
                    }
                }
            })
        val job = serviceScope.async(start = CoroutineStart.LAZY){
            try {
                channelFuture = b.connect("213.183.53.35", 19999)
                return@async channelFuture
            }catch (e: Exception){
                e.printStackTrace()
                return@async null
            }
        }
        job.start()
    }

    fun connectTo(publicIp: String, publicPort: Int, localPort:Int){
        serviceScope.launch {
            val connection = UdpConnection(serviceScope, localPort)
            withContext(Dispatchers.IO){
                val future = connection.connect()
                future.addListener {
                    serviceScope.launch {
                        connection.newLoop(publicIp, publicPort)
                    }
                }
            }
        }
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
