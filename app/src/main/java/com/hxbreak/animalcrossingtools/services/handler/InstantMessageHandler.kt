package com.hxbreak.animalcrossingtools.services.handler

import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.backend.BackendPacket
import dagger.Lazy
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import timber.log.Timber
import javax.inject.Inject

class InstantMessageHandler @Inject constructor(
    @AndroidId val androidId: Lazy<String>,
    private val controller: InstantMessageController,
) : SimpleChannelInboundHandler<BackendPacket.ToClientPacket>(){

    override fun channelRead0(
        ctx: ChannelHandlerContext?,
        msg: BackendPacket.ToClientPacket?
    ) {
        msg?.let {
            Timber.e("HxBreak ${msg.messageType}")
            when (msg.messageType) {
                BackendPacket.BackendMessageType.TEXT -> {
                    Timber.e(msg.chatTextEntity.toString())
                }
                BackendPacket.BackendMessageType.LIST -> {
                    controller.updateOnlineList(msg.userEntityList.orEmpty())
                }
                BackendPacket.BackendMessageType.AUTHORIZED -> {
                    controller.updateOnlineList(msg.userEntityList.orEmpty())
                    controller.userAuthorized()
                }
                else -> {

                }
            }
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        controller.mainChannel = ctx?.channel()
        val login = BackendPacket.LoginEntity.newBuilder().apply {
            id = androidId.get()
            key = "testkey"
        }.build()
        val msg = BackendPacket.ToServerPacket.newBuilder()
            .apply {
                messageType = BackendPacket.BackendMessageType.LOGIN
                loginEntity = login
            }.build()
        ctx?.writeAndFlush(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        controller.mainChannel = null
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext?,
        cause: Throwable?
    ) {
        Timber.e(cause)
        controller.handleException(cause)
        ctx?.close()
    }
}