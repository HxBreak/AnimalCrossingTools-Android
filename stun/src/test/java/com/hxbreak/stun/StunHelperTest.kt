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
import java.nio.ByteBuffer
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

data class TestResult1(
    val discoverInfo: DiscoverInfo,
    val changedAddress: ChangedAddress? = null,
    val mappedAddress: MappedAddress? = null,
    val needTest3: Boolean? = null,
)

enum class NatType{
    UNKNOWN,
    NO_NAT,
    FULL_CONE,
    SYMMETRIC_NAT,
    RESTRICTED_CONE,
    RESTRICTED_PORT
}

data class DiscoverInfo(
    val publicIP: InetAddress? = null,
    val publicPort: Int? = null,
    val errorCode: Int? = null,
    val errorReason: String? = null,
    val natType: NatType? = NatType.UNKNOWN,
)

class StunHelperTest {

    val service = Executors.newSingleThreadExecutor()

    @Test
    fun test() {
        testNetworkDiscover()
    }

    /**
     * when mappedAddress != null is redo test1 Mode
     */
    private fun test1(
        defaultPort: Int,
        stunServer: String,
        stunServerPort: Int,
        mappedAddress: MappedAddress? = null
    ): TestResult1 {
        val byteBuffer = ByteArray(200)
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
                        if (mappedAddress == null) {
                            addMessageAttribute(ChangeRequest())
                        }
                    }
                udp.send(DatagramPacket(msg.bytes, msg.length))
                val packet = DatagramPacket(byteBuffer, 200)
                var receiveMsg: MessageHeader?
                do {
                    udp.receive(packet)
                    receiveMsg = MessageHeader.parseHeader(packet.data)
                    receiveMsg.parseAttributes(packet.data)
                } while (receiveMsg?.equalTransactionID(msg) != true)
                val ma =
                    receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.MappedAddress) as? MappedAddress
                val ca =
                    receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ChangedAddress) as? ChangedAddress
                val errCode =
                    receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode) as? ErrorCode
                if (errCode != null) {
                    return TestResult1(
                        DiscoverInfo(
                            errorCode = errCode.responseCode,
                            errorReason = errCode.reason
                        )
                    )
                }

                if (ma == null || (mappedAddress == null && ca == null)) {
                    return TestResult1(
                        DiscoverInfo(
                            errorCode = 700,
                            errorReason = "The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry."
                        )
                    )
                } else {
                    if (ma.port == udp.localPort && ma.address.bytes.contentEquals(udp.inetAddress.address)) {
                        return TestResult1(
                            DiscoverInfo(
                                natType = NatType.NO_NAT
                            )
                        )
                    } else {
                        if (mappedAddress != null) {
                            if (mappedAddress.address.bytes.contentEquals(ma.address.bytes) &&
                                mappedAddress.port == ma.port
                            ) {
                                return TestResult1(
                                    DiscoverInfo(
                                        ma.address.inetAddress,
                                        ma.port,
                                    ),
                                    changedAddress = ca,
                                    mappedAddress = ma,
                                    needTest3 = true,
                                )
                            } else {
                                TestResult1(
                                    DiscoverInfo(
                                        ma.address.inetAddress,
                                        natType = NatType.SYMMETRIC_NAT
                                    ),
                                    changedAddress = ca,
                                    mappedAddress = ma,
                                )
                            }
                        }
                        return TestResult1(
                            DiscoverInfo(
                                ma.address.inetAddress,
                                ma.port
                            ),
                            changedAddress = ca,
                            mappedAddress = ma,
                        )

                    }
                }
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    val timeout = System.currentTimeMillis() - start
                    timeExceed += timeout
                } else {
                    return TestResult1(
                        DiscoverInfo(
                            errorCode = -1,
                            errorReason = e.message
                        )
                    )
                }
            } finally {
                udp?.close()
            }
        } while (timeExceed < 7900)
        return TestResult1(
            DiscoverInfo(
                errorCode = -1,
                errorReason = "Network Unreachable",
            )
        )
    }

    private fun testNetworkDiscover(
        defaultPort: Int = 33660,
        stunServer: String = "stun.sipgate.net",
        stunServerPort: Int = 10000
    ) {
        val test1: Future<TestResult1> = service.submit(Callable {
            return@Callable test1(defaultPort, stunServer, stunServerPort)
        })
        val test1Result = test1.get()
        if (test1Result.discoverInfo.publicIP != null) {
            val test2future = service.submit(Callable {
                return@Callable test2(
                    defaultPort,
                    stunServer,
                    stunServerPort,
                    test1Result.discoverInfo,
                    test1Result.changedAddress!!
                )
            })
            val test2Result = test2future.get()
            if (test2Result.errorCode != null) {
                val test1redo = service.submit(Callable {
                    return@Callable test1(
                        defaultPort,
                        stunServer,
                        stunServerPort,
                        test1Result.mappedAddress
                    )
                })
                val redoResult = test1redo.get()
                if(redoResult.needTest3 == true){
                    val test3future = service.submit(Callable {
                        return@Callable test3(
                            defaultPort,
                            stunServer,
                            stunServerPort,
                            test1Result.discoverInfo,
                            test1Result.changedAddress!!)
                    })

                    println(test3future.get())
                }
            }
        }

    }

    private fun test2(
        port: Int,
        stunServer: String,
        stunServerPort: Int,
        test1Result: DiscoverInfo,
        changedAddress: ChangedAddress
    ): DiscoverInfo {
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
                val errCode =
                    receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode) as? ErrorCode
                if (errCode != null) {
                    return test1Result.copy(
                        errorCode = errCode.responseCode,
                        errorReason = errCode.reason
                    )
                }
                if (test1Result.natType == NatType.NO_NAT) {
                    return test1Result.copy(
                        natType = NatType.NO_NAT
                    )
                } else {
                    return test1Result.copy(
                        natType = NatType.FULL_CONE
                    )
                }
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    timeExceed += System.currentTimeMillis() - start
                } else {
                    return test1Result.copy(
                        errorCode = -1,
                        errorReason = e.message
                    )
                }
            } finally {
                udp?.close()
            }
        } while (timeExceed < 7900)
        return test1Result.copy(
            errorCode = -1,
            errorReason = "Network Unreachable"
        )
    }

    fun test3(
        port: Int,
        stunServer: String,
        stunServerPort: Int,
        test1Result: DiscoverInfo,
        changedAddress: ChangedAddress
    ): DiscoverInfo {
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
                val errCode =
                    receiveMsg.getMessageAttribute(MessageAttributeInterface.MessageAttributeType.ErrorCode) as? ErrorCode
                if (errCode != null) {
                    return test1Result.copy(
                        errorCode = errCode.responseCode,
                        errorReason = errCode.reason
                    )
                }
                return test1Result.copy(natType = NatType.RESTRICTED_CONE)
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    timeExceed += System.currentTimeMillis() - start
                } else {
                    return test1Result.copy(
                        errorCode = -1,
                        errorReason = e.message
                    )
                }
            } finally {
                udp?.close()
            }
        }while (timeExceed < 7900)
        return test1Result.copy(
            natType = NatType.RESTRICTED_PORT
        )
    }
}