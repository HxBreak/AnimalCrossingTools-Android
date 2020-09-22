package com.hxbreak.animalcrossingtools

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.ContentLengthInputStream
import okhttp3.*
import okio.*
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@GlideModule
class GlideModule : AppGlideModule() {

    @Inject
    lateinit var client: OkHttpClient

    @Inject
    lateinit var collector: GlideProgressCollector

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        (context.applicationContext as App).androidInjector().inject(this)

        builder.setDefaultRequestOptions {
            RequestOptions()
                .skipMemoryCache(true)
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            ProgressiveOkHttpUrlLoader.Factory(client, collector)
        )
    }

}

class ProgressiveOkHttpUrlLoader(
    private val client: Call.Factory,
    private val collector: GlideProgressCollector
) : ModelLoader<GlideUrl, InputStream> {

    class Factory(
        private val httpClient: Call.Factory,
        private val collector: GlideProgressCollector
    ) : ModelLoaderFactory<GlideUrl, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl, InputStream> {
            return ProgressiveOkHttpUrlLoader(httpClient, collector)
        }

        override fun teardown() {}
    }

    override fun buildLoadData(
        model: GlideUrl,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(
            model,
            ProgressiveOkHttpStreamFetcher(
                client,
                model,
                model.cacheKey ?: model.toStringUrl(),
                collector
            )
        )
    }

    override fun handles(model: GlideUrl) = true
}

interface GlideAppFetcherListener {

    fun start(key: String)

    fun update(key: String, progress: GlideProgress.Loading)

    fun end(key: String, progress: GlideProgress)

}

class ProgressiveOkHttpStreamFetcher(
    private val client: Call.Factory, private val url: GlideUrl,
    private val key: String,
    private val listener: GlideAppFetcherListener,
) : DataFetcher<InputStream>, Callback {

    private var stream: InputStream? = null
    private var responseBody: ResponseBody? = null
    private var callback: DataFetcher.DataCallback<in InputStream>? = null

    @Volatile
    private var call: Call? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val httpUrl = Request.Builder().url(url.toStringUrl())
        url.headers.entries.forEach {
            httpUrl.addHeader(it.key, it.value)
        }
        call =
            client.newCall(httpUrl.build()).apply { enqueue(this@ProgressiveOkHttpStreamFetcher) }
        listener.start(key)
        this.callback = callback
    }

    override fun cleanup() {
        try {
            stream?.close()
        } catch (e: Exception) {
        }
        responseBody?.close()
        callback = null
    }

    override fun cancel() {
        call?.cancel()
    }

    override fun getDataClass() = InputStream::class.java

    override fun getDataSource() = DataSource.REMOTE

    override fun onFailure(call: Call, e: IOException) {
        listener.end(key, GlideProgress.Error(e))
        callback?.onLoadFailed(e)
    }

    override fun onResponse(call: Call, response: Response) {
        responseBody = ProgressiveResponseBody(key, response.body()!!, listener)
        if (response.isSuccessful) {
            val len = responseBody!!.contentLength()
            stream = ContentLengthInputStream.obtain(responseBody!!.byteStream(), len)
            callback?.onDataReady(stream)
        } else {
            val exception = HttpException(response.message(), response.code())
            listener.end(key, GlideProgress.Error(exception))
            callback?.onLoadFailed(exception)
        }
    }

}

class ProgressiveResponseBody(
    private val key: String,
    private val body: ResponseBody,
    private val listener: GlideAppFetcherListener,
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType() = body.contentType()

    override fun contentLength() = body.contentLength()

    override fun source(): BufferedSource {
        bufferedSource = bufferedSource ?: forwardingSource(body.source()).buffer()
        return bufferedSource!!
    }

    private fun forwardingSource(source: Source) =
        object : ForwardingSource(source) {

            var totalBytesRead = 0L
            val fullLen = body.contentLength()

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead = if (bytesRead < 0L) {
                    fullLen
                } else {
                    totalBytesRead + bytesRead
                }
                listener.update(key, GlideProgress.Loading(totalBytesRead, fullLen))
                return bytesRead
            }

            override fun close() {
                super.close()
                listener.end(key, GlideProgress.DONE)
            }
        }
}