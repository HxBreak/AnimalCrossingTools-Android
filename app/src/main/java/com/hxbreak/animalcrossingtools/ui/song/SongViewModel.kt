package com.hxbreak.animalcrossingtools.ui.song

import android.util.Log
import androidx.lifecycle.*
import com.hxbreak.animalcrossingtools.data.services.Song
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.SongSaved
import com.hxbreak.animalcrossingtools.data.services.SongMix
import com.hxbreak.animalcrossingtools.ui.fish.CombinedLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SongViewModel(
    private val repository: DataRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val refresh = MutableLiveData(false)
    val loading = MutableLiveData(false)
    val editMode = MutableLiveData(false)
    val erro = MutableLiveData<Exception>()
    val selected = MutableLiveData<MutableList<Int>>()

    val cds = refresh.switchMap {
        loading.value = true
        repository.getAllSongs().switchMap { filterSongs(it) }
            .switchMap {
                liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                    val saved = repository.getAllSavedSongs()
                    val ret = it.map {
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

    val selItems = CombinedLiveData(selected, cds) { x, y ->
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
                    postValue(ret)
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
            viewModelScope.launch {
                result.postValue(data.data.values.toList())
            }
        } else if (data is Result.Error) {
            erro.value = data.exception
            Log.e("HxBreak", "${data.exception}")
        }
        loading.value = false
        return result
    }

    fun clearSelected() {
        selected.value = arrayListOf()
    }

    fun toggleSong(id: Int) {
        if (selected.value?.contains(id) == true) {
            selected.value?.remove(id)
        } else {
            selected.value?.add(id)
        }
        selected.value = selected.value
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
}

data class SongMixSelectable(
    private val song1: Song,
    private val songSaved1: SongSaved?,
    var selected: Boolean
) : SongMix(song1, songSaved1)