package com.example.tracker_proto

import java.nio.ByteBuffer

object NetworkUtils {

    fun bytesToIp(ip: ByteArray?): Int {
        return if (ip == null || ip.size != 4) {
            0
        } else {
            val b = ByteBuffer.allocate(4)
            ip.forEach { b.put(it) }
            b.flip()
            b.int;
        }
    }

    fun ipToBytes(ip: Int): ByteArray {
        val b = ByteBuffer.allocate(4)
        b.putInt(ip)
        b.flip()
        return byteArrayOf(b.get(), b.get(), b.get(), b.get())
    }

    fun ipToString(ip: Int): String {
        val b = ByteBuffer.allocate(4)
        b.putInt(ip)
        b.flip()
        return "${b.get().toInt() and 0xFF}.${b.get().toUByte()}.${b.get().toUByte()}.${b.get().toUByte()}"
    }

}