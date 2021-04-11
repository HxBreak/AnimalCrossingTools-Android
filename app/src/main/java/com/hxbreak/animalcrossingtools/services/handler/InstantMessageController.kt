package com.hxbreak.animalcrossingtools.services.handler

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.withTransaction
import com.google.protobuf.ByteString
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.data.source.DefaultDataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.MessageEntity
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.services.FileProviderExt
import com.hxbreak.animalcrossingtools.utils.closeQuietly
import com.hxbreak.animalcrossingtools.utils.fileToUri
import com.hxbreak.backend.BackendPacket
import com.hxbreak.stun.DiscoverInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class TransferStream(
    val to: String,
    val key: String,
    val file: File,
    val outputStream: OutputStream,
)

class InstantMessageController(
    private val context: Context,
    private val database: AnimalCrossingDatabase,
) {
    private val messageDao = database.messageDao()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val _lastException = MutableLiveData<Throwable?>()
    private val _authorized = MutableLiveData(false)
    private val _lobbyList = MutableLiveData<List<BackendPacket.NetUserEntity>>()
    private val _stunResponse = MutableLiveData<Event<BackendPacket.StunResponse>>()
    private val _stunRequest = MutableLiveData<Event<BackendPacket.StunRequest>>()

    val authorized: LiveData<Boolean>
        get() = _authorized

    val stunResponse: LiveData<Event<BackendPacket.StunResponse>>
        get() = _stunResponse

    val stunRequest: LiveData<Event<BackendPacket.StunRequest>>
        get() = _stunRequest

    val lastException: LiveData<Throwable?>
        get() = _lastException

    val lobbyList: LiveData<List<BackendPacket.NetUserEntity>>
        get() = _lobbyList

    /**
     * Init By InstantMessageHandler Remove By InstantMessageHandler
     */
    var mainChannel: ChannelHandlerContext? = null
    set(value) {
        field = value
        if (value == null){
            serviceScope.launch {
                if (_authorized.value != false){
                    //clear up
                    _lobbyList.value = emptyList()
                    _authorized.value = false
                }
            }
        }
    }

    private suspend fun authorizedBlock(block: suspend (ChannelHandlerContext) -> Unit) : Boolean{
        return withContext(serviceScope.coroutineContext){
            if ((mainChannel?.isRemoved == false)){
                if (_authorized.value == true){
                    block(mainChannel!!)
                    return@withContext true
                }else{
                    handleException(Exception("User Is Not Authorized"))
                }
            }else{
                handleException(Exception("Not Connected"))
            }
            return@withContext false
        }
    }

    suspend fun sendStunRequest(toId: String): Boolean {
        return authorizedBlock {
            val packet = BackendPacket.ToServerPacket.newBuilder().apply {
                messageType = BackendPacket.BackendMessageType.STUN_REQUEST
                stunRequest = BackendPacket.StunRequest.newBuilder().apply {
                    setToId(toId)
                }.build()
            }.build()
            val p = mainChannel!!.writeAndFlush(packet)
            p.await()
        }
    }

    suspend fun sendStunInfoReply(info: DiscoverInfo, isAccept: Boolean): Boolean {
        return authorizedBlock {
            val packet = BackendPacket.ToServerPacket.newBuilder().apply {
                messageType = BackendPacket.BackendMessageType.STUN_INFO_SWAP
                stunInfoReply = BackendPacket.StunInfoReply.newBuilder().apply {
                    op = if (isAccept) BackendPacket.StunOperationType.ACCEPT else BackendPacket.StunOperationType.DECLINE
                    publicAddr = info.publicIP?.hostAddress
                    publicPort = info.publicPort ?: -1
                }.build()
            }.build()
            val p = mainChannel!!.writeAndFlush(packet)
            p.await()
        }
    }

    suspend fun sendMessageTo(toId: String, msg: String): Boolean {
        return authorizedBlock {
            val bytes = ByteString.copyFromUtf8(msg)
            if (bytes.size() <= MAX_LEN_IN_PACKET){
                val packet = BackendPacket.ToServerPacket.newBuilder().apply {
                    messageType = BackendPacket.BackendMessageType.MEDIA
                    mediaEntity = BackendPacket.MediaEntity.newBuilder().let {
                        it.contentBytes = bytes
                        it.toId = toId
                        it.currentOffset = 0
                        it.contentLength = it.contentBytes.size().toLong()
                        it.contentMimetype = "$APP_PRIVATE_MEDIA_MIME_TYPE/$DIRECT_DISPLAY_MIME_TYPE"
                        it.build()
                    }
                }.build()
                mainChannel!!.writeAndFlush(packet)
            }
        }
    }

    suspend fun sendFileTo(streamId: String, selfId: String, toId: String, uri: Uri, mimeType: String): Boolean {
        return authorizedBlock {
            val input = context.contentResolver.openInputStream(uri) ?: return@authorizedBlock

            val cache = ByteArray(MAX_LEN_IN_PACKET)
            var current = 0L
            val cl = input?.available()
            while (true){
                val len = input?.read(cache, 0, MAX_LEN_IN_PACKET) ?: 0
                if (len < 0) break
                val packet = BackendPacket.ToServerPacket.newBuilder().apply {
                    messageType = BackendPacket.BackendMessageType.MEDIA
                    mediaEntity = BackendPacket.MediaEntity.newBuilder().let {
                        it.streamId = streamId
                        it.contentBytes = ByteString.copyFrom(cache, 0, len)
                        it.toId = toId
                        it.currentOffset = current
                        it.contentLength = cl.toLong()
                        it.contentMimetype = mimeType
                        it.build()
                    }
                }.build()
                suspendCoroutine<Unit> { con ->
                    val f = it.channel().writeAndFlush(packet)
                    f.addListener { con.resume(Unit) }
                }
                current += len
                Timber.e("$cl, $current, $len")
            }
            input.closeQuietly()
            database.withTransaction {
                val entity = MessageEntity(selfId, toId, mimeType, LocalDateTime.now(Clock.systemUTC()), path = uri.toString())
                messageDao.insertAll(entity)
            }
//            val bytes = ByteString.copyFromUtf8(msg)
//            if (bytes.size() <= MAX_LEN_IN_PACKET){

//                mainChannel!!.writeAndFlush(packet)
//            }
        }
    }
    fun handleException(throwable: Throwable?){
        serviceScope.launch {
            _lastException.value = throwable
        }
    }

    fun updateOnlineList(list: List<BackendPacket.NetUserEntity>) {
        serviceScope.launch {
            _lobbyList.value = list
        }
    }

    fun handleStunResponse(response: BackendPacket.StunResponse){
        serviceScope.launch {
            _stunResponse.value = Event(response)
        }
    }

    fun handleStunRequest(request: BackendPacket.StunRequest){
        serviceScope.launch {
            _stunRequest.value = Event(request)
        }
    }

    fun userAuthorized() {
        serviceScope.launch {
            if (_authorized.value != true){
                _authorized.value = true
            }
        }
    }

    val streamingFiles = arrayListOf<TransferStream>()

    fun handleMediaContent(mediaEntity: BackendPacket.MediaEntity?) {
        val media = mediaEntity ?: return
        val toId = media.toId ?: return
        val needFindInTable = media.contentLength > MAX_LEN_IN_PACKET
        val mimeType = (mediaEntity.contentMimetype ?: return).split("/")
        val isAppPrivateMediaMimeType = mimeType.getOrNull(0) == APP_PRIVATE_MEDIA_MIME_TYPE
        val mediaDir = FileProviderExt.mediaDir(context)

        /**
         * Direct Display Message Should Not Be Split And MimeType is `app-private/text`
         */
        val isDirectDisplayText = !needFindInTable && isAppPrivateMediaMimeType && mimeType.getOrNull(1) == DIRECT_DISPLAY_MIME_TYPE
        if (isDirectDisplayText){
            val localDateTime = Instant.ofEpochMilli(media.serverDatetime).atOffset(ZoneOffset.UTC).toLocalDateTime()
            val entity = MessageEntity( media.fromId, media.toId, media.contentMimetype, localDateTime,
                description = media.contentBytes.toStringUtf8()
            )
            serviceScope.launch {
                database.withTransaction {
                    messageDao.insertAll(entity)
                }
            }
        }else{
            /**
             * When Text Message Large Than 8k they will save as File.
             */
            val transfer = if (mediaEntity.currentOffset == 0L){
                val file = File(mediaDir, "${media.streamId}@${media.fromId}")
                val uri = context.fileToUri(file)
                val stream = FileOutputStream(file)
                val transfer = TransferStream(media.toId, media.streamId, file, stream)
                streamingFiles.add(transfer)
                transfer
            }else{
                streamingFiles.firstOrNull { it.key == media.streamId }
            }
            if (transfer != null){
                val len = media.contentBytes.size()
                media.contentBytes.writeTo(transfer.outputStream)
                if (media.currentOffset + len >= media.contentLength){
                    val uri = context.fileToUri(transfer.file)
                    transfer.outputStream.closeQuietly()
                    streamingFiles.remove(transfer)
                    val localDateTime = Instant.ofEpochMilli(media.serverDatetime).atOffset(ZoneOffset.UTC).toLocalDateTime()
                    val entity = MessageEntity(media.fromId, media.toId, media.contentMimetype, localDateTime,
                        path = uri.toString()
                    )
                    serviceScope.launch {
                        database.withTransaction {
                            messageDao.insertAll(entity)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_LEN_IN_PACKET = 4096 * 2
        const val APP_PRIVATE_MEDIA_MIME_TYPE = "app-private"
        const val DIRECT_DISPLAY_MIME_TYPE = "text"
    }
}