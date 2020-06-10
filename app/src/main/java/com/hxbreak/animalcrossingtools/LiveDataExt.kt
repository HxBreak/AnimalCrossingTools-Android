package com.hxbreak.animalcrossingtools

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

internal const val DEFAULT_TIMEOUT = 5000L

internal typealias Block<R, X, Y> = suspend LiveDataScope<R>.(x: X?, y: Y?) -> Unit


@UseExperimental(ExperimentalTypeInference::class)
fun <R, X, Y> combineLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMs: Long = DEFAULT_TIMEOUT,
    x: LiveData<X>,
    y: LiveData<Y>,
    @BuilderInference block: suspend LiveDataScope<R>.(x: X?, y: Y?) -> Unit
): LiveData<R> = CoroutineLiveData(context, timeoutInMs, x, y, block)

internal class CoroutineLiveData<R, X, Y>(
    val context: CoroutineContext = EmptyCoroutineContext,
    val timeoutInMs: Long = DEFAULT_TIMEOUT,
    x: LiveData<X>,
    y: LiveData<Y>,
    val block: Block<R, X?, Y?>
) : MediatorLiveData<R>() {
    private var blockRunner: BlockRunner<R, X, Y>? = null
    private var emittedSource: EmittedSource? = null
    private var xIsInit = false
    private var yIsInit = false
    private var lastX: X? = null
    private var lastY: Y? = null

    private fun onChange(xData: X?, yData: Y?) {
        if (!(xIsInit && yIsInit)) return
        val supervisorJob = SupervisorJob(context[Job])
        val scope = CoroutineScope(Dispatchers.Main.immediate + context + supervisorJob)

        blockRunner = BlockRunner(
            liveData = this,
            block = block,
            timeoutInMs = timeoutInMs,
            scope = scope,
            x = xData,
            y = yData
        ) {
            blockRunner = null
        }
    }

    init {
        addSource(x) {
            xIsInit = true
            lastX = it
            onChange(lastX, lastY)
        }
        addSource(y) {
            yIsInit = true
            lastY = it
            onChange(lastX, lastY)
        }
    }

    internal suspend fun emitSource(source: LiveData<R>): DisposableHandle {
        clearSource()
        val newSource = addDisposableSource(source)
        emittedSource = newSource
        return newSource
    }

    internal suspend fun clearSource() {
        emittedSource?.disposeNow()
        emittedSource = null
    }

    override fun onActive() {
        super.onActive()
        blockRunner?.maybeRun()
    }

    override fun onInactive() {
        super.onInactive()
        blockRunner?.cancel()
    }
}

internal class LiveDataScopeImpl<R, X, Y>(
    internal var target: CoroutineLiveData<R, X, Y>,
    context: CoroutineContext
) : LiveDataScope<R> {

    override val latestValue: R?
        get() = target.value

    // use `liveData` provided context + main dispatcher to communicate with the target
    // LiveData. This gives us main thread safety as well as cancellation cooperation
    private val coroutineContext = context + Dispatchers.Main.immediate

    override suspend fun emitSource(source: LiveData<R>): DisposableHandle =
        withContext(coroutineContext) {
            return@withContext target.emitSource(source)
        }

    override suspend fun emit(value: R) = withContext(coroutineContext) {
        target.clearSource()
        target.value = value
    }
}

/**
 * Handles running a block at most once to completion.
 */
internal class BlockRunner<R, X, Y>(
    private val liveData: CoroutineLiveData<R, X, Y>,
    private val block: Block<R, X?, Y?>,
    private val timeoutInMs: Long,
    private val scope: CoroutineScope,
    val x: X?,
    val y: Y?,
    private val onDone: () -> Unit
) {
    // currently running block job.
    private var runningJob: Job? = null

    // cancelation job created in cancel.
    private var cancellationJob: Job? = null

    @MainThread
    fun maybeRun() {
        cancellationJob?.cancel()
        cancellationJob = null
        if (runningJob != null) {
            return
        }
        runningJob = scope.launch {
            val liveDataScope = LiveDataScopeImpl(liveData, coroutineContext)
            block(liveDataScope, x, y)
            onDone()
        }
    }

    @MainThread
    fun cancel() {
        if (cancellationJob != null) {
            error("Cancel call cannot happen without a maybeRun")
        }
        cancellationJob = scope.launch(Dispatchers.Main.immediate) {
            delay(timeoutInMs)
            if (!liveData.hasActiveObservers()) {
                // one last check on active observers to avoid any race condition between starting
                // a running coroutine and cancelation
                runningJob?.cancel()
                runningJob = null
            }
        }
    }
}

/**
 * Holder class that keeps track of the previously dispatched [LiveData].
 * It implements [DisposableHandle] interface while also providing a suspend clear function
 * that we can use internally.
 */
internal class EmittedSource(
    private val source: LiveData<*>,
    private val mediator: MediatorLiveData<*>
) : DisposableHandle {
    // @MainThread
    private var disposed = false

    /**
     * Unlike [dispose] which cannot be sync because it not a coroutine (and we do not want to
     * lock), this version is a suspend function and does not return until source is removed.
     */
    suspend fun disposeNow() = withContext(Dispatchers.Main.immediate) {
        removeSource()
    }

    override fun dispose() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            removeSource()
        }
    }

    @MainThread
    private fun removeSource() {
        if (!disposed) {
            mediator.removeSource(source)
            disposed = true
        }
    }
}


internal suspend fun <R> MediatorLiveData<R>.addDisposableSource(
    source: LiveData<R>
): EmittedSource = withContext(Dispatchers.Main.immediate) {
    addSource(source) {
        value = it
    }
    EmittedSource(
        source = source,
        mediator = this@addDisposableSource
    )
}
