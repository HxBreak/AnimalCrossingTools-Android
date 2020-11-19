package com.hxbreak.animalcrossingtools.ui.houseware

import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hxbreak.animalcrossingtools.adapter.ItemComparable
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.Result
import com.hxbreak.animalcrossingtools.data.prefs.DataUsageStorage
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.prefs.StorableDuration
import com.hxbreak.animalcrossingtools.data.source.DataRepository
import com.hxbreak.animalcrossingtools.data.source.entity.HousewareEntity
import com.hxbreak.animalcrossingtools.fragment.Event
import com.hxbreak.animalcrossingtools.i18n.toDatabaseNameColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.time.Duration
import java.time.Instant

class HousewaresViewModel @ViewModelInject constructor(
    private val repository: DataRepository,
    private val preferenceStorage: PreferenceStorage,
    private val dataUsageStorage: DataUsageStorage,
): ViewModel(){

    val locale = preferenceStorage.selectedLocale
    val loading = MutableLiveData(false)
    val refresh = MutableLiveData(false)
    val database = MutableLiveData<Event<String>>()
    val filter = MutableLiveData<String?>()
    private val error = MutableLiveData<Throwable?>()

    private val usagePolicy by lazy(LazyThreadSafetyMode.PUBLICATION) {
        dataUsageStorage.selectStorableDataRefreshDuration
    }

    val housewares = refresh.switchMap {
        loading.value = true
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            val cachedCount = repository.local().housewaresDao().count()
            val queryJob = viewModelScope.launch (viewModelScope.coroutineContext + Dispatchers.IO){
                val cache = repository.local().housewaresDao().all().groupBy {
                    it.seriesId
                }.map { HousewareVariants(it.value) }
                emit(ProjectDataSource.newDatabaseSource(cache))
            }
            val block = suspend {
                val result = repository.repoSource().allHousewares()
                queryJob.join()// make sure database result return
                when (result){
                    is Result.Success -> {
                        emit(ProjectDataSource.newNetworkSource(result.data.second.map { HousewareVariants(it) }))
                        result.data.first.join()
                        dataUsageStorage.lastFurnitureRefreshDateTime = Instant.now()
                        database.postValue(Event("Database updated"))
                        error.postValue(null)
                    }
                    is Result.Error -> {
                        error.postValue(result.exception)
                    }
                }
            }
            when(val policy = usagePolicy){
                is StorableDuration.DOWNLOAD_ALWAYS -> {
                    block()
                }
                is StorableDuration.DOWNLOAD_WHEN_EMPTY -> {
                    if (cachedCount < 100){
                        block()
                    }
                }
                is StorableDuration.InTime -> {
                    val refreshTime = dataUsageStorage.lastFurnitureRefreshDateTime.epochSecond + policy.duration.seconds
                    if (Instant.now().epochSecond > refreshTime){
                        block()
                    }
                }
            }
            queryJob.join()
            loading.postValue(false)
        }
    }

    val filteredData = filter.switchMap {
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            if (!it.isNullOrBlank()){
                val query = SimpleSQLiteQuery(
                    "SELECT * FROM HOUSEWARES WHERE ${locale.toDatabaseNameColumn()} LIKE ?",
                    arrayOf("%$it%")
                )
                val filtered = repository.local().housewaresDao().filterViaQuery(query).groupBy {
                    it.seriesId
                }.map { HousewareVariants(it.value) }
                emit(ProjectDataSource.newDatabaseSource(filtered))
            }else{
                emit(null)
            }
        }
    }

    val screenData = combinedLiveData(
        viewModelScope.coroutineContext + Dispatchers.Default,
        x = housewares, y = filteredData, runCheck = { x, y -> x || y }
    ){ x, y ->
        if (y != null){
            emit(y)
        }else{
            emit(x)
        }
    }

    val unpackedScreenData = screenData.map {
        it?.data
    }

    val screenStatus = combinedLiveData(viewModelScope.coroutineContext + Dispatchers.Default,
        x = error, y = screenData, runCheck = { x, y -> x || y }){ x, y ->
        val result = when {
            y != null && y.type == ProjectDataSource.SOURCE_DATABASE && (!y.data.isNullOrEmpty()) -> UiStatus.Success
            x != null -> UiStatus.Error(x)
            y != null && y.data.isNullOrEmpty() -> UiStatus.Empty
            else -> UiStatus.Success
        }
        emit(result)
    }

    val searchSuggestionKeywords = MutableLiveData<Event<String?>>()
    val suggestionCursor = searchSuggestionKeywords.switchMap {
        liveData (viewModelScope.coroutineContext + Dispatchers.IO){
            val key = it.getContentIfNotHandled()
            if (key != null && !key.isNullOrBlank()){
                val query = SimpleSQLiteQuery(
                    "SELECT * FROM HOUSEWARES WHERE ${locale.toDatabaseNameColumn()} LIKE ?",
                    arrayOf("%$key%")
                )
                val cursor = repository.local().housewaresDao().getCursorViaQuery(query)
                emit(Proxy.newProxyInstance(
                    SQLiteCursor::class.java.classLoader,
                    arrayOf(Cursor::class.java),
                    CursorInvocationHandler(cursor)
                ) as Cursor)
            }else{
                emit(null)
            }
        }
    }

    inner class CursorInvocationHandler(
        private val cursor: Cursor
    ): InvocationHandler{

        private val column = cursor.getColumnIndexOrThrow("internal_id")

        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
            try {
                return if (method?.name == "getColumnIndexOrThrow" && args?.size == 1 && args[0] == "_id"){
                    column
                } else if (method?.name == "getLong" && args?.size == 1 && args[0] == column){
                    cursor.getString(column).toLong()
                } else if (method?.name == "close"){
                    cursor.close()
                } else {
                    method?.invoke(cursor, *(args ?: emptyArray()))
                }
            }catch (e: InvocationTargetException){
                Timber.e("${cursor.hashCode()} ${cursor.isClosed} Exception")
                throw e.targetException
            }
        }
    }
}

data class HousewareVariants(
    val variants: List<HousewareEntity>
): ItemComparable<String> {
    override fun id() = variants.first().seriesId
}

sealed class UiStatus {
    data class Error(val exception: Throwable) : UiStatus()
    object Success : UiStatus()
    object Empty : UiStatus()
}

data class ProjectDataSource<out R>(val data: R, val type: Int) {

    companion object{
        const val SOURCE_NETWORK = 0
        const val SOURCE_DATABASE = 1

        fun <T> newNetworkSource(data: T) = ProjectDataSource(data, SOURCE_NETWORK)
        fun <T> newDatabaseSource(data: T) = ProjectDataSource(data, SOURCE_DATABASE)
    }
}