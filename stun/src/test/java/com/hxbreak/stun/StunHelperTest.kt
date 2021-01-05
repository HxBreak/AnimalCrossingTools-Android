package com.hxbreak.stun

import de.javawi.jstun.attribute.*
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import org.junit.Test
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

data class TestResult1(
    val discoverInfo: DiscoverInfo,
    val changedAddress: ChangedAddress? = null,
)

data class DiscoverInfo(
    val publicIP: InetAddress? = null,
    val publicPort: Int? = null,
    val errorCode: Int? = null,
    val errorReason: String? = null,
    val natNet: Boolean? = null,
    val openAccess: Boolean? = null,
    val fullCone: Boolean? = null,
)

class StunHelperTest {

    val service = Executors.newSingleThreadExecutor()

    @Test
    fun test(){
//        "stun.l.google.com:19302", "stun1.l.google.com:19302"
//        testNetworkDiscover(stunServer = "stun.l.google.com", stunServerPort = 19302)
        testNetworkDiscover()
    }

    private fun testNetworkDiscover(defaultPort: Int = 33660, stunServer: String = "stun.sipgate.net", stunServerPort: Int = 10000){
        val byteBuffer = ByteArray(200)
        val test1: Future<TestResult1> = service.submit( Callable {
            var udp: DatagramSocket? = null
            var timeExceed: Long = 0
            do {
                val start = System.currentTimeMillis()
                try {
                    udp = DatagramSocket(defaultPort)
                    udp.soTimeout = 3000
                    udp.connect(InetAddress.getByName(stunServer), stunServerPort)
                    val msg = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
                        .apply {
                            generateTransactionID()
                            addMessageAttribute(ChangeRequest())
                        }
                    udp.send(DatagramPacket(msg.bytes, msg.length))
                    val packet = DatagramPacket(byteBuffer, 200)
                    var receiveMsg: MessageHeader?
                    do {
                        udp.receive(packet)
                        receiveMsg = MessageHeader.parseHeader(packet.data)
                        receiveMsg.parseAttributes(packet.data)
                    } while (receiveMsg?.equalTransactionID(msg) != true)
                    val ma = receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress) as? MappedAddress
                    val ca = receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ChangedAddress) as? ChangedAddress
                    val errCode = receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode) as? ErrorCode
                    if (errCode != null){
                        return@Callable TestResult1(DiscoverInfo(
                            errorCode = errCode.responseCode,
                            errorReason = errCode.reason
                        ))
                    }
                    if (ma == null || ca == null){
                        return@Callable TestResult1(DiscoverInfo(
                            errorCode = 700,
                            errorReason = "The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry."
                        ))
                    }else{
                        if (ma.port == udp.localPort && ma.address.bytes.contentEquals(udp.inetAddress.address)){
                            return@Callable TestResult1(
                                DiscoverInfo(
                                    natNet = false
                                )
                            )
                        }else{
                            return@Callable TestResult1(
                                DiscoverInfo(
                                    ma.address.inetAddress,
                                    ma.port
                                ),
                                changedAddress = ca
                            )
                        }
                    }
                }catch (e: Exception){
                    if (e is SocketTimeoutException){
                        val timeout = System.currentTimeMillis() - start
                        timeExceed += timeout
                    }else{
                        return@Callable TestResult1(
                            DiscoverInfo(
                                errorCode = -1,
                                errorReason = e.message
                            )
                        )
                    }
                }finally {
                    udp?.close()
                }
            }while (timeExceed < 7900)
            return@Callable TestResult1(
                DiscoverInfo(
                    errorCode = -1,
                    errorReason = "Network Unreachable",
                )
            )
        })
        val test1Result = test1.get()
        println(test1Result)
        if (test1Result.discoverInfo.publicIP != null){
            val test2future = service.submit(Callable {
                return@Callable test2(defaultPort, stunServer, stunServerPort, test1Result.discoverInfo, test1Result.changedAddress!!)
            })
            println(test2future.get())
        }

    }

    private fun test2(port: Int, stunServer: String, stunServerPort: Int, test1Result: DiscoverInfo, changedAddress: ChangedAddress): DiscoverInfo{
        val byteBuffer = ByteArray(200)
        var udp: DatagramSocket? = null
        var timeExceed: Long = 0
        do {
            val start = System.currentTimeMillis()
            try {
                udp = DatagramSocket(port)
                udp.soTimeout = 3000
                udp.connect(InetAddress.getByName(stunServer), stunServerPort)
                val msg = MessageHeader(MessageHeaderInterface.MessageHeaderType.BindingRequest)
                    .apply {
                        generateTransactionID()
                        addMessageAttribute(ChangeRequest().apply {
                            setChangeIP()
                            setChangePort()
                        })
                    }

                udp.send(DatagramPacket(msg.bytes, msg.length))

                udp.close()
                udp = DatagramSocket(port)
                udp.soTimeout = 3000
                udp.connect(changedAddress.address.inetAddress, changedAddress.port)

                val packet = DatagramPacket(byteBuffer, 200)
                var receiveMsg: MessageHeader?
                do {
                    udp.receive(packet)
                    receiveMsg = MessageHeader.parseHeader(packet.data)
                    receiveMsg.parseAttributes(packet.data)
                } while (receiveMsg?.equalTransactionID(msg) != true)
                val errCode = receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode) as? ErrorCode
                if (errCode != null){
                    return test1Result.copy(
                        errorCode = errCode.responseCode,
                        errorReason = errCode.reason
                    )
                }
                if (test1Result.natNet == false){
                    return test1Result.copy(
                        openAccess = true
                    )
                }else{
                    return test1Result.copy(
                        fullCone = true
                    )
                }
            }catch (e: Exception){
                if (e is SocketTimeoutException){
                    timeExceed += System.currentTimeMillis() - start
                }else{
                    return test1Result.copy(
                        errorCode = -1,
                        errorReason = e.message
                    )
                }
            }finally {
                udp?.close()
            }
        }while (timeExceed < 7900)
        return test1Result.copy(
            errorCode = -1,
            errorReason = "Network Unreachable"
        )
    }
}