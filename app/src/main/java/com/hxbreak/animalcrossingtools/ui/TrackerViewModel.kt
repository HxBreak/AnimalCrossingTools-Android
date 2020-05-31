package com.hxbreak.animalcrossingtools.ui

import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.*
import com.google.protobuf.GeneratedMessageV3
import com.hxbreak.animalcrossingtools.di.ApplicationModule
import com.hxbreak.animalcrossingtools.fragment.Event
import com.example.tracker_proto.NetworkUtils
import com.hxbreak.nat.NatPacket
import com.hxbreak.tracker_proto.data.ConnectedClient
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

class TrackerViewModel @Inject constructor(@ApplicationModule.AndroidId val id: String) :
    ViewModel() {

    companion object {
        const val OPEN_SERVER_ADDR = "120.79.4.153"
    }

    val onlines = MutableLiveData<List<ConnectedClient>>()
    var lastRecvTime = 0L
    val peerLastData = MutableLiveData<Event<String>>()
    val synchronousQueue = LinkedBlockingQueue<Pair<InetSocketAddress, GeneratedMessageV3>>()
    val lock = Any()
    var lastSentTest = 0L
    val oclient = MutableLiveData<Event<ConnectedClient>>()
    val mConnectMode = MutableLiveData(0)
    /**
     * 客户端发起连接
     * 服务端转发连接请求
     * 客户端连接状态确认成功修改ModeValue = 1
     * 客户端穿透连接失败修改状态 = 2
     * 为1时保持继续通讯，固定间隔时间时间发送数据包
     * 为2时不做处理
     */
    var mConnectModeValue = 0
    val targetLastInteractive = MutableLiveData(0L)

    init {
        Log.e("HxBreak", "Tracker ViewModel Created")
        viewModelScope.launch(Dispatchers.IO) {
            val sel = Selector.open()
            val channel = DatagramChannel.open()
            channel.configureBlocking(false)
            channel.register(sel, SelectionKey.OP_READ)
            val buf = ByteBuffer.allocate(2048)
            var target: ConnectedClient? = null
            var grantRequestTest = false//允许开始NAT
            var natTestCount = 0;//NAT测试次数

            while (isActive) {
                synchronized(lock) {
                    val size = synchronousQueue.size
                    if (size == 0) return@synchronized
                    for (i in 0..size) {
                        val data = synchronousQueue.poll()
                        if (!(data?.first == null || data.second == null)) {
                            buf.apply {
                                clear()
                                put(data.second!!.toByteArray())
                                flip()
                                channel.send(buf, data.first)
                            }
                        }
                    }
                }

                requestOnline(buf, channel, id)
                val num = sel.select(1000)
                if (num > 0) {
                    val iter = sel.selectedKeys().iterator()
                    while (iter.hasNext()) {
                        val key = iter.next()
                        iter.remove()
                        if (key.isReadable) {
                            val chan = key.channel() as DatagramChannel
                            buf.clear()
                            val addr = chan.receive(buf)
                            buf.flip()
                            if (buf.hasRemaining()) {
                                try {
                                    val data = NatPacket.CommResponse.parseFrom(buf)
                                    when (data.senderType) {
                                        0 -> {
                                            when (data.fromServer.order) {
                                                NatPacket.BaseResponse.BaseResponseOrderType.OK -> {
                                                    val value = data.fromServer.addrsList.map {
                                                        ConnectedClient.unpack(it)
                                                    }
                                                    if (value.size >= 2 && target == null) {
                                                        val ret = value.firstOrNull { it.id != id }
                                                        target = ret
                                                    }
                                                    onlines.postValue(value)
                                                    lastRecvTime = System.currentTimeMillis()
                                                    sent = 0
                                                }
                                                NatPacket.BaseResponse.BaseResponseOrderType.REQ -> {
                                                    data.fromServer.pairedAddr.let {
                                                        /**
                                                         * 设置状态以允许进行NAT
                                                         */
                                                        mConnectMode.postValue(0)
                                                        peerLastData.postValue(null)
                                                        mConnectModeValue = 0
                                                        target = ConnectedClient.unpack(it)
                                                        Log.e("HxBreak", "Start Nat Test")
                                                        grantRequestTest = true
                                                        natTestCount = 0
                                                    }
                                                }
                                            }
                                        }
                                        1 -> {
                                            if (addr is InetSocketAddress) {
                                                when (data.fromClient.type) {
                                                    NatPacket.BaseClientCommunication.BaseCommType.TEXT -> {
                                                        peerLastData.postValue(Event(data.fromClient.text))
                                                    }
                                                    NatPacket.BaseClientCommunication.BaseCommType.TEST -> {
                                                        mConnectMode.postValue(1)
                                                        mConnectModeValue = 1
                                                        targetLastInteractive.postValue(System.currentTimeMillis())
                                                    }
                                                    NatPacket.BaseClientCommunication.BaseCommType.OK -> {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else if (key.isWritable) {
                        }
                    }
                } else {
                    target = null
                }
                if (mConnectModeValue == 0) {
                    if (grantRequestTest && natTestCount < 30 && target != null && (System.currentTimeMillis() - lastSentTest > 33)) {
                        lastSentTest = System.currentTimeMillis()
                        val reqTest = NatPacket.CommResponse.newBuilder()
                            .setSenderType(1)
                            .setFromClient(
                                NatPacket.BaseClientCommunication.newBuilder()
                                    .setType(NatPacket.BaseClientCommunication.BaseCommType.TEST)
                                    .setId("$id")
                            ).build()
                        buf.apply {
                            clear()
                            put(reqTest.toByteArray())
                            flip()
                            try {
                                channel.send(
                                    this,
                                    InetSocketAddress(
                                        NetworkUtils.ipToString(target!!.addr),
                                        target!!.port
                                    )
                                )
                            } catch (e: Exception) {
                            }
                        }
                        Log.e(
                            "HxBreak",
                            "Test to ${InetSocketAddress(
                                NetworkUtils.ipToString(target!!.addr),
                                target!!.port
                            )}"
                        )
                        natTestCount++
                        if (natTestCount >= 29) {
                            natTestCount = 0
                            grantRequestTest = false
//                            target = null
                            mConnectModeValue = 2
                            mConnectMode.postValue(2)
                            peerLastData.postValue(Event("NAT穿透失败，使用转发模式。"))
                        }
                    }
                } else if (mConnectModeValue == 1) {
                    if (target != null && (System.currentTimeMillis() - lastSentTest > 333)) {
                        lastSentTest = System.currentTimeMillis()
                        val reqTest = NatPacket.CommResponse.newBuilder()
                            .setSenderType(1)
                            .setFromClient(
                                NatPacket.BaseClientCommunication.newBuilder()
                                    .setType(NatPacket.BaseClientCommunication.BaseCommType.TEST)
                                    .setId(id)
                                    .setText(System.nanoTime().toString())
                            ).build()
                        buf.apply {
                            clear()
                            put(reqTest.toByteArray())
                            flip()
                            try {
                                channel.send(
                                    this,
                                    InetSocketAddress(
                                        NetworkUtils.ipToString(target!!.addr),
                                        target!!.port
                                    )
                                )
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
                delay(16)
            }
        }
    }

    var sent = 0
    fun requestOnline(buf: ByteBuffer, channel: DatagramChannel, id: String) {
        if (System.currentTimeMillis() - lastRecvTime > 200 && sent < 10) {
            sent++
            lastRecvTime = System.currentTimeMillis()
        }// 1/5 Second
        buf.clear()
        val v = NatPacket.BasePacket.newBuilder()
            .setHeader(
                NatPacket.BasePacket.PacketHeader.newBuilder()
                    .setId("$id")
                    .setType(NatPacket.BasePacket.PacketHeader.PacketType.LOGIN)
                    .build()
            ).build()
        buf.apply {
            put(v.toByteArray())
            flip()
            try {
                channel.send(this, InetSocketAddress(OPEN_SERVER_ADDR, 8000))
            } catch (e: Exception) {
            }
        }
    }

    fun sendTo(client: ConnectedClient, text: String) {
        if (client == oclient.value?.peekContent()) {
            if (mConnectModeValue == 1) {
                val reqTest = NatPacket.CommResponse.newBuilder()
                    .setSenderType(1)
                    .setFromClient(
                        NatPacket.BaseClientCommunication.newBuilder()
                            .setType(NatPacket.BaseClientCommunication.BaseCommType.TEXT)
                            .setId(id)
                            .setText(text)
                    ).build()
                val addr = InetSocketAddress(NetworkUtils.ipToString(client.addr), client.port)
                synchronized(lock) {
                    synchronousQueue.offer(Pair(addr, reqTest))
                }
            } else {
//                NatPacket.BasePacket.newBuilder()
            }
        }
    }


    fun connectTo(client: ConnectedClient) {
        if (mConnectModeValue == 1 && client == oclient.value?.peekContent()) return
        val requestConnect = NatPacket.BasePacket.newBuilder()
            .setHeader(
                NatPacket.BasePacket.PacketHeader.newBuilder()
                    .setId("$id")
                    .setType(NatPacket.BasePacket.PacketHeader.PacketType.CONNECT)
                    .setAddr(client.pack())
                    .build()
            ).build()
        val addr = InetSocketAddress(OPEN_SERVER_ADDR, 8000)
        synchronized(lock) {
            synchronousQueue.offer(Pair(addr, requestConnect))
        }
    }

    fun chatWith(client: ConnectedClient) {
        if (client.id != id)
            oclient.value = Event(client)
    }
}
