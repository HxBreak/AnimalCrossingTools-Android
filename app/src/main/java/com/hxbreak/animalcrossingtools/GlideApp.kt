package com.hxbreak.animalcrossingtools

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.hxbreak.animalcrossingtools.di.DaggerApplicationComponent
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

@GlideModule
class GlideModule : AppGlideModule() {

    @Inject
    lateinit var client: OkHttpClient

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setMemoryCache(LruResourceCache(1024 * 1024 * 100))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 512))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        (context.applicationContext as App).androidInjector().inject(this)
        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}