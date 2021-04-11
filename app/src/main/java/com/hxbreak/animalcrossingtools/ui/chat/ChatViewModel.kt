package com.hxbreak.animalcrossingtools.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.hxbreak.animalcrossingtools.data.source.DefaultDataRepository
import com.hxbreak.animalcrossingtools.di.AndroidId
import com.hxbreak.animalcrossingtools.services.FileProviderExt
import com.hxbreak.animalcrossingtools.services.handler.InstantMessageController
import com.hxbreak.animalcrossingtools.utils.closeQuietly
import com.hxbreak.animalcrossingtools.utils.fileToUri
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ChatViewModel @AssistedInject constructor(
    repository: DefaultDataRepository,
    @SuppressLint("StaticFieldLeak") @ApplicationContext private val context: Context,
    private val controller: InstantMessageController,
    @AndroidId private val selfId: String,
    @Assisted private val toId: String,
) : ViewModel() {

    val dao = repository.local().messageDao()

    val paging = Pager(PagingConfig(10, 2, false, 20, 150)){
        dao.chatSource(selfId, toId)
    }.flow.cachedIn(viewModelScope)

    private val worker = viewModelScope.coroutineContext + Dispatchers.IO

    fun sendMessage(text: String){
        viewModelScope.launch(worker) {
            controller.sendMessageTo(toId, text)
        }
    }

    fun sendUri(uri: Uri){
        viewModelScope.launch(worker) {
            val streamId = "${System.currentTimeMillis()}"
            val dir = FileProviderExt.mediaDir(context)
            val input = context.contentResolver.openInputStream(uri) ?: return@launch
            val file = File(dir, streamId)
            val fos = FileOutputStream(file)
            input.copyTo(fos, 4096 * 2)
            input.closeQuietly()
            fos.closeQuietly()
            controller.sendFileTo(streamId, selfId, toId, context.fileToUri(file), context.contentResolver.getType(uri) ?: "unknown")
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(toId: String): ChatViewModel
    }

    companion object{

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: AssistedFactory,
            toId: String
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(toId) as T
            }
        }
    }
}
