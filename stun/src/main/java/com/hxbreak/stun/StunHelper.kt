package com.hxbreak.stun

import de.javawi.jstun.attribute.*
import de.javawi.jstun.header.MessageHeader
import de.javawi.jstun.header.MessageHeaderInterface
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

private data class TestResult1(
    val discoverInfo: DiscoverInfo,
    val changedAddress: ChangedAddress? = null,
    val mappedAddress: MappedAddress? = null,
    val needTest3: Boolean? = null,
)

enum class NatType {
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
    val localPort: Int? = null,
    val errorCode: Int? = null,
    val errorReason: String? = null,
    val natType: NatType? = NatType.UNKNOWN,
)

object StunHelper {

    fun testNatType(localPort: Int, stunServer: String, stunServerPort: Int, timeout: Long = 5000) =
        flow {
            val test1Result = test1(localPort, stunServer, stunServerPort, timeout)
            emit(test1Result.discoverInfo)
            test1Result.discoverInfo.localPort ?: return@flow
            val test2Result = test2(test1Result.discoverInfo.localPort, stunServer, stunServerPort,
                test1Result.discoverInfo, test1Result.changedAddress!!, timeout)
            emit(test2Result)
            if (test2Result.errorCode != null){
                val result1redo = test1(test1Result.discoverInfo.localPort, stunServer, stunServerPort, timeout, test1Result.mappedAddress)
                emit(result1redo.discoverInfo)
                if (result1redo.needTest3 == true){
                    val test3result = test3(test1Result.discoverInfo.localPort, stunServer, stunServerPort,
                        test1Result.discoverInfo, test1Result.changedAddress, timeout)
                    emit(test3result)
                }
            }
        }


    /**
     * when mappedAddress != null is redo test1 Mode
     */
    private fun test1(
        defaultPort: Int,
        stunServer: String,
        stunServerPort: Int,
        timeout: Long,
        mappedAddress: MappedAddress? = null,
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
                                ma.address.inetAddress,
                                ma.port,
                                natType = NatType.NO_NAT,
                                localPort = udp.localPort
                            ),
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
                                        localPort = udp.localPort
                                    ),
                                    changedAddress = ca,
                                    mappedAddress = ma,
                                    needTest3 = true,
                                )
                            } else {
                                TestResult1(
                                    DiscoverInfo(
                                        ma.address.inetAddress,
                                        natType = NatType.SYMMETRIC_NAT,
                                        localPort = udp.localPort,
                                    ),
                                    changedAddress = ca,
                                    mappedAddress = ma,
                                )
                            }
                        }
                        return TestResult1(
                            DiscoverInfo(
                                ma.address.inetAddress,
                                ma.port,
                                localPort = udp.localPort
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
        } while (timeExceed < timeout)
        return TestResult1(
            DiscoverInfo(
                errorCode = -1,
                errorReason = "Network Unreachable",
            )
        )
    }


    private fun test2(
        port: Int,
        stunServer: String,
        stunServerPort: Int,
        test1Result: DiscoverInfo,
        changedAddress: ChangedAddress,
        timeout: Long,
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
        } while (timeExceed < timeout)
        return test1Result.copy(
            errorCode = -1,
            errorReason = "Network Unreachable"
        )
    }

    private fun test3(
        port: Int,
        stunServer: String,
        stunServerPort: Int,
        test1Result: DiscoverInfo,
        changedAddress: ChangedAddress,
        timeout: Long,
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
        } while (timeExceed < timeout)
        return test1Result.copy(
            natType = NatType.RESTRICTED_PORT
        )
    }
}
