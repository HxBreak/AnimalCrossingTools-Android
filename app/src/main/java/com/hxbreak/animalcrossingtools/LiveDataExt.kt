package com.hxbreak.animalcrossingtools

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

//Copy from LiveData Ktx
internal const val DEFAULT_TIMEOUT = 5000L

internal typealias Block<R, X, Y> = suspend CombinedLiveDataScope<R>.(x: X?, y: Y?) -> Unit
/**
 * if return value is False, keep waiting for next call
 */
typealias CombinedRunCheck = (x: Boolean, y: Boolean) -> Boolean

internal val DEFAULT_RUN_CHECK: CombinedRunCheck = { x, y -> x && y }

@UseExperimental(ExperimentalTypeInference::class)
fun <R, X, Y> combinedLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMs: Long = DEFAULT_TIMEOUT,
    x: LiveData<X>,
    y: LiveData<Y>,
    runCheck: CombinedRunCheck = DEFAULT_RUN_CHECK,
    runnerType: RunnerType = RunnerType.LINEAR,
    @BuilderInference block: suspend CombinedLiveDataScope<R>.(x: X?, y: Y?) -> Unit
): LiveData<R> = CoroutineLiveData(context, timeoutInMs, x, y, runCheck, runnerType, block)

enum class RunnerType {
    LINEAR, CANCEL_PRE_AND_RUN
}

internal class CoroutineLiveData<R, X, Y>(
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val timeoutInMs: Long = DEFAULT_TIMEOUT,
    private val x: LiveData<X>,
    private val y: LiveData<Y>,
    private val runCheck: CombinedRunCheck,
    private val runnerType: RunnerType = RunnerType.LINEAR,
    private val block: Block<R, X?, Y?>
) : MediatorLiveData<R>() {

    private var emittedSource: EmittedSource? = null
    private var xIsInit = false
    private var yIsInit = false
    private var lastX: X? = null
    private var lastY: Y? = null

    private val supervisorJob = SupervisorJob(context[Job])
    private val scope = CoroutineScope(Dispatchers.Main.immediate + context + supervisorJob)
    private var blockRunner: BlockRunnerInterface<R, X, Y>?

    private var runningJob: Job? = null

    private fun onChange(xData: X?, yData: Y?) {
        if (!runCheck(xIsInit, yIsInit)) return
        runningJob = blockRunner?.maybeRun(xData, yData)
    }

    init {
        val onDone = {
            runningJob = null
        }
        blockRunner = when (runnerType) {
            RunnerType.LINEAR -> BlockRunner(
                liveData = this,
                block = block,
                timeoutInMs = timeoutInMs,
                scope = scope,
                onDone = onDone
            )
            RunnerType.CANCEL_PRE_AND_RUN -> CancelAndJoinBlockRunner(
                liveData = this,
                block = block,
                timeoutInMs = timeoutInMs,
                scope = scope,
                onDone = onDone
            )
            else -> throw IllegalArgumentException("runnerType not found")
        }
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
    }

    override fun onInactive() {
        super.onInactive()
        blockRunner?.cancel()
    }
}

internal class LiveDataScopeImpl<R>(
    internal var target: CoroutineLiveData<R, *, *>,
    private val context: CoroutineContext
) : CombinedLiveDataScope<R> {

    override val latestValue: R?
        get() = target.value

    // use `liveData` provided context + main dispatcher to communicate with the target
    // LiveData. This gives us main thread safety as well as cancellation cooperation
    private val mainContext = context + Dispatchers.Main.immediate

    override suspend fun emitSource(source: LiveData<R>) =
        withContext(mainContext) {
            return@withContext target.emitSource(source)
        }

    override suspend fun emit(value: R) = withContext(mainContext) {
        target.clearSource()
        target.value = value
    }

    override val coroutineContext: CoroutineContext
        get() = context
}

interface BlockRunnerInterface<R, X, Y> {

    fun maybeRun(x: X?, y: Y?): Job

    fun cancel();
}

/**
 * Handles running a block at most once to completion.
 */
internal class BlockRunner<R, X, Y>(
    private val liveData: CoroutineLiveData<R, X, Y>,
    private val block: Block<R, X?, Y?>,
    private val timeoutInMs: Long,
    private val scope: CoroutineScope,
    private val onDone: () -> Unit
) : BlockRunnerInterface<R, X, Y> {
    private val activeTask = AtomicReference<Job?>(null)
    // currently running block job.
    private var runningJob: Job? = null

    private val mutex = Mutex()

    private var cancellationJob: Job? = null

    @MainThread
    override fun maybeRun(x: X?, y: Y?): Job {
        val job = scope.launch {
            mutex.withLock {
                val liveDataScope = LiveDataScopeImpl(liveData, coroutineContext)
                block(liveDataScope, x, y)
                runningJob = null
                onDone()
            }
        }
        runningJob = job
        return job
    }

    @MainThread
    override fun cancel() {
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
                activeTask.get()?.cancel()
            }
        }

    }
}


/**
 * Handles running a block at most once to completion.
 */
internal class CancelAndJoinBlockRunner<R, X, Y>(
    private val liveData: CoroutineLiveData<R, X, Y>,
    private val block: Block<R, X?, Y?>,
    private val timeoutInMs: Long,
    private val scope: CoroutineScope,
    private val onDone: () -> Unit
) : BlockRunnerInterface<R, X, Y> {
    private val activeTask = AtomicReference<Job?>(null)

    private var cancellationJob: Job? = null

    override fun maybeRun(x: X?, y: Y?): Job {
        val jobEntity = scope.launch(start = CoroutineStart.LAZY) {
            val liveDataScope = LiveDataScopeImpl(liveData, coroutineContext)
            block(liveDataScope, x, y)
        }
        jobEntity.invokeOnCompletion {
            activeTask.compareAndSet(jobEntity, null)
        }
        return scope.launch {
            while (true) {
                if (!activeTask.compareAndSet(null, jobEntity)) {
                    activeTask.get()?.cancelAndJoin()
                    yield()//take a rest
                } else {
                    jobEntity.join()
                    onDone()
                    break
                }
            }
        }
    }

    @MainThread
    override fun cancel() {
        if (cancellationJob != null) {
            error("Cancel call cannot happen without a maybeRun")
        }
        cancellationJob = scope.launch(Dispatchers.Main.immediate) {
            delay(timeoutInMs)
            if (!liveData.hasActiveObservers()) {
                // one last check on active observers to avoid any race condition between starting
                // a running coroutine and cancelation
                activeTask.get()?.cancel()
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


/**
 * Interface that allows controlling a [LiveData] from a coroutine block.
 *
 * @see liveData
 */
interface CombinedLiveDataScope<T> : CoroutineScope {
    /**
     * Set's the [LiveData]'s value to the given [value]. If you've called [emitSource] previously,
     * calling [emit] will remove that source.
     *
     * Note that this function suspends until the value is set on the [LiveData].
     *
     * @param value The new value for the [LiveData]
     *
     * @see emitSource
     */
    suspend fun emit(value: T)

    /**
     * Add the given [LiveData] as a source, similar to [MediatorLiveData.addSource]. Calling this
     * method will remove any source that was yielded before via [emitSource].
     *
     * @param source The [LiveData] instance whose values will be dispatched from the current
     * [LiveData].
     *
     * @see emit
     * @see MediatorLiveData.addSource
     * @see MediatorLiveData.removeSource
     */
    suspend fun emitSource(source: LiveData<T>): DisposableHandle

    /**
     * References the current value of the [LiveData].
     *
     * If the block never `emit`ed a value, [latestValue] will be `null`. You can use this
     * value to check what was then latest value `emit`ed by your `block` before it got cancelled.
     *
     * Note that if the block called [emitSource], then `latestValue` will be last value
     * dispatched by the `source` [LiveData].
     */
    val latestValue: T?
}

