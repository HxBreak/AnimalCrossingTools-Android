package com.hxbreak.animalcrossingtools.di

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hxbreak.animalcrossingtools.GlideProgressCollector
import com.hxbreak.animalcrossingtools.data.CoroutinesCallAdapterFactory
import com.hxbreak.animalcrossingtools.data.LiveDataCallAdapterFactory
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.data.prefs.SharedPreferenceStorage
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServiceV2
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServices
import com.hxbreak.animalcrossingtools.data.source.*
import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import com.hxbreak.animalcrossingtools.data.source.local.FishLocalDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.RepoDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongRemoteDataSource
import com.hxbreak.animalcrossingtools.media.MusicService
import com.hxbreak.animalcrossingtools.media.MusicServiceConnection
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideAnimalCrossingDataSource(
        database: AnimalCrossingDatabase,
        @ApiV1 services: AnimalCrossingServices,
        serviceV2: AnimalCrossingServiceV2
    ): AnimalCrossingDataSource {
        return object : AnimalCrossingDataSource {
            override fun fishSource(): FishDataSource {
                return FishLocalDataSource(database.fishDao(), serviceV2)
            }

            override fun songSource(): SongDataSource {
                return SongRemoteDataSource(
                    services, database.songSavedDao()
                )
            }

            override fun repoSource() = RepoDataSource(serviceV2, database)
        }
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideLogger(): HttpLoggingInterceptor {
        val log = Timber.tag("normalApi")
        val ret = HttpLoggingInterceptor {
            log.e(it)
        }
        ret.level = HttpLoggingInterceptor.Level.BASIC
        return ret
    }

    @JvmStatic
    @Singleton
    @Provides
    @GlideLogger
    fun provideGlideLogger(): HttpLoggingInterceptor {
        val log = Timber.tag("Glide")
        val ret = HttpLoggingInterceptor {
            log.e(it)
        }
        ret.level = HttpLoggingInterceptor.Level.BASIC
        return ret
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideGson(): Gson {
        val builder = GsonBuilder()
        return builder.create()
    }

    @JvmStatic
    @Singleton
    @Provides
    @ApiV1
    fun provideRetrofit(@Named("NormalApi") okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://acnhapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
    }


    @JvmStatic
    @Singleton
    @Provides
    @Named("NormalApi")
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideGlideHttpClient(@GlideLogger logger: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    @ApiV2
    fun provideRetrofitV2(@Named("NormalApi") okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://acnhapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
//            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addCallAdapterFactory(CoroutinesCallAdapterFactory())
            .build()
    }


    @JvmStatic
    @Singleton
    @Provides
    @ApiV1
    fun provideService(@ApiV1 retrofit: Retrofit): AnimalCrossingServices {
        return retrofit.create(AnimalCrossingServices::class.java)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideServicsV2(@ApiV2 retrofit: Retrofit) =
        retrofit.create(AnimalCrossingServiceV2::class.java)


    @JvmStatic
    @Singleton
    @Provides
    fun providerFishLocalDataSource(
        database: AnimalCrossingDatabase,
        serviceV2: AnimalCrossingServiceV2,
        ioDispatcher: CoroutineDispatcher
    ): FishLocalDataSource {
        return FishLocalDataSource(database.fishDao(), serviceV2, ioDispatcher)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideMusicServiceConnection(context: Context) = MusicServiceConnection.getInstance(
        context,
        ComponentName(context, MusicService::class.java)
    )

    @JvmStatic
    @Singleton
    @Provides
    fun provideDatabase(context: Context): AnimalCrossingDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            AnimalCrossingDatabase::class.java, "ACNH.db"
        ).build()
        return result
    }

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AndroidId

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ApiV1

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ApiV2

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class GlideOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class GlideLogger

    @JvmStatic
    @Singleton
    @Provides
    @AndroidId
    fun provideAndroidId(context: Context): String {
        val ANDROID_ID =
            Settings.System.getString(context.contentResolver, Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @JvmStatic
    @Singleton
    @Provides
    fun providesPreferenceStorage(context: Context): PreferenceStorage =
        SharedPreferenceStorage(context)

    @JvmStatic
    @Singleton
    @Provides
    fun provideProgressCollector() = GlideProgressCollector()
}

@Module
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultDataRepository): DataRepository
}