package com.hxbreak.tracker_proto.data

import com.hxbreak.nat.NatPacket
import java.io.Serializable

data class ConnectedClient(
    val id: String,
    val addr: Int,
    val port: Int,
    val lastActiveTime: Long
) : Serializable {
    fun pack() = NatPacket.BaseResponse.NetAddr.newBuilder().apply {
        id = this@ConnectedClient.id
        ip = this@ConnectedClient.addr
        port = this@ConnectedClient.port
        lastActiveTime = this@ConnectedClient.lastActiveTime
    }

    companion object {
        fun unpack(netAddr: NatPacket.BaseResponse.NetAddr) =
            ConnectedClient(netAddr.id, netAddr.ip, netAddr.port, netAddr.lastActiveTime)
    }

    override fun equals(other: Any?): Boolean {
        if (other is ConnectedClient) {
            if (other.hashCode() == this.hashCode()) return true
            return other.lastActiveTime == lastActiveTime &&
                    other.addr == addr &&
                    other.port == port &&
                    other.id == id
        }
        return false
    }
}