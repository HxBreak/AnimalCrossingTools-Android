package com.hxbreak.animalcrossingtools.services.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultAddressedEnvelope
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.DatagramPacketDecoder
import io.netty.handler.codec.MessageToMessageDecoder

class WrappedDatagramPacketDecoder(
    decoder: MessageToMessageDecoder<ByteBuf>
): DatagramPacketDecoder(decoder){

    override fun decode(ctx: ChannelHandlerContext?, msg: DatagramPacket?, out: MutableList<Any>?) {
        super.decode(ctx, msg, out)
        out?.lastOrNull()?.let {
            out.add(DefaultAddressedEnvelope(out.removeLast(), msg?.recipient(), msg?.sender()))
        }
    }
}