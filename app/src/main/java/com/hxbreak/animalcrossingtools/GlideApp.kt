package com.hxbreak.animalcrossingtools

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setMemoryCache(LruResourceCache(1024 * 1024 * 100))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 512))
    }
}