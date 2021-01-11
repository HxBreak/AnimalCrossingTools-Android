package com.hxbreak.animalcrossingtools

import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.backend.BackendPacket

data class SimpleNetUser(val ip: Int, val port: Int, val id: String): ItemComparable<String>{
    override fun id() = id
}

fun BackendPacket.NetUserEntity.toSimpleNetUser(): SimpleNetUser{
    return SimpleNetUser(ip, port, id)
}

fun SimpleNetUser.toNetUserEntity() =
    BackendPacket.NetUserEntity.newBuilder()
        .also {
            it.id = id
            it.ip = ip
            it.port = port
        }.build()