package com.hxbreak.animalcrossingtools.ui.song

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.uamp.media.extensions.*
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.data.source.entity.Song
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.source.entity.SongMix
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.livedata.CombinedLiveData
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.ref.WeakReference

class SongViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val connection: MusicServiceConnection,
    private val preferenceStorage: PreferenceStorage
) : ViewModel() {

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)
    val editMode = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
    val selected = MutableLiveData<MutableList<Int>>(mutableListOf())
    internal val lunchNowPlayingEvent = MutableLiveData<Event<WeakReference<Pair<Song, SongItemView>>>>()
    val locale = preferenceStorage.selectedLocale

    val cds = refresh.switchMap {
        loading.value = true
        repository.getAllSongs().switchMap { filterSongs(it) }
            .switchMap {
                liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                    val saved = repository.getAllSavedSongs()
                    val ret = it.map {
                        it.localName = it.name.toLocaleName(locale)
                        SongMixSelectable(
                            it,
                            saved.firstOrNull { s -> s.id == it.id },
                            false
                        )
                    }
                    emit(ret)
                }
            }
    }

    private val selItems = CombinedLiveData(selected, cds) { x, y ->
        y?.forEach { it.selected = x?.contains(it.song.id) ?: false }
        return@CombinedLiveData y
    }

    /**
     * trigger on savedchange
     */
    val savedChange = MutableLiveData(false)

    var sort = false
        set(value) {
            field = value
            savedChange.value = false
        }

    val items = MediatorLiveData<List<SongMixSelectable>>().apply {
        var source1: Boolean? = null
        var y: List<SongMixSelectable>? = null
        fun makeValue() {
            viewModelScope.launch {
                val saved = repository.getAllSavedSongs()
                val ret = y?.map {
                    SongMixSelectable(
                        it.song,
                        saved.firstOrNull { s -> s.id == it.song.id },
                        it.selected
                    )
                }
                if (sort) {
                    postValue(ret?.sortedBy { it.songSaved?.owned ?: false })
                } else {
                    postValue(ret.orEmpty())
                }
            }
        }
        addSource(savedChange) { source1 = it;makeValue() }
        addSource(selItems) { y = it;makeValue() }
    }

    val collectedText = items.switchMap { i ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            if (i.isNullOrEmpty()) {
                emit("等待中")
            } else {
                val count = i.count { it.songSaved?.owned ?: false }
                emit("$count/${i.size}")
            }
        }
    }

    val ownAction = MediatorLiveData<Boolean>().apply {
        var source1: MutableList<Int>? = null
        var y: List<SongMixSelectable>? = null
        fun makeValue() {
            viewModelScope.launch {
                val ret = !(source1?.any {
                    y?.firstOrNull { s -> s.song.id == it }?.songSaved?.owned ?: false
                } ?: false)
                postValue(ret)
            }
        }
        addSource(selected) { source1 = it;makeValue() }
        addSource(items) { y = it;makeValue() }
    }

    init {
        selected.value = arrayListOf()
        refresh.value = true
    }

    private fun filterSongs(data: Result<Map<String, Song>>): LiveData<List<Song>> {
        val result = MutableLiveData<List<Song>>()
        if (data is Result.Success) {
            error.postValue(null)
            viewModelScope.launch {
                result.postValue(data.data.values.toList())
            }
        } else if (data is Result.Error) {
            error.value = data.exception
        }
        loading.value = false
        return result
    }

    fun clearSelected() {
        selected.value = arrayListOf()
    }

    fun toggleSong(id: Int) {
        val list = mutableListOf<Int>()
        list.addAll(selected.value.orEmpty())
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        selected.value = list
    }

    fun toggleOwnSong() {
        if (selected.value.isNullOrEmpty()) return

        val v = ownAction.value
        val to = selected.value!!.map { SongSaved(it, v!!, 0) }
        viewModelScope.launch {
            loading.value = true
            repository.local().songSavedDao()
                .insertSongSaved(to)
            savedChange.value = false
            loading.value = false
        }
    }

    internal fun playSong(song: Song, itemView: SongItemView) {
        val metadata = MediaMetadataCompat.Builder().apply {
            mediaUri = song.musicUrl
            displayTitle = "${song.localName}"
            displaySubtitle = "K.K."
            title = song.fileName
            albumArtUri = song.imageUrl
        }.build()
        connection.transportControls.playFromUri(
            Uri.parse(song.musicUrl),
            bundleOf("MediaMetaData" to metadata)
        )
        lunchNowPlayingEvent.value = Event(WeakReference(song to itemView))
    }
}

data class SongMixSelectable(
    private val song1: Song,
    private val songSaved1: SongSaved?,
    var selected: Boolean
) : SongMix(song1, songSaved1), ItemComparable<Int>{
    override fun id() = song1.id
}

fun Song.imageTransitionName(): String = "$fileName-image"
fun Song.titleTransitionName(): String = "$fileName-title"
