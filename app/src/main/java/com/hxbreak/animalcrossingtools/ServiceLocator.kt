package com.hxbreak.animalcrossingtools

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hxbreak.animalcrossingtools.data.LiveDataCallAdapter
import com.hxbreak.animalcrossingtools.data.LiveDataCallAdapterFactory
import com.hxbreak.animalcrossingtools.data.services.AnimalCrossingServices
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDataSource
import com.hxbreak.animalcrossingtools.data.source.AnimalCrossingDatabase
import com.hxbreak.animalcrossingtools.data.source.DefaultDataRepository
import com.hxbreak.animalcrossingtools.data.source.local.FishDataSource
import com.hxbreak.animalcrossingtools.data.source.local.FishLocalDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongDataSource
import com.hxbreak.animalcrossingtools.data.source.remote.SongRemoteDataSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {
    private val lock = Any()
    private var database: AnimalCrossingDatabase? = null

    private var retrofit: Retrofit? = null
    private var acServices: AnimalCrossingServices? = null

    @Volatile
    var animalCrossingDataRepository: DefaultDataRepository? = null

    fun provideRepository(context: Context): DefaultDataRepository {
        return animalCrossingDataRepository ?: synchronized(lock) {
            animalCrossingDataRepository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): DefaultDataRepository {
        val repo = DefaultDataRepository(
            createLocalDataSource(context),
            database ?: createDataBase(context)
        )
        animalCrossingDataRepository = repo
        return repo
    }

    private fun createLocalDataSource(context: Context): AnimalCrossingDataSource {
        val database = database ?: createDataBase(context)
        val retro = retrofit ?: createRetrofit()
        return object : AnimalCrossingDataSource {
            override fun fishSource(): FishDataSource {
                return FishLocalDataSource(database.fishDao())
            }

            override fun songSource(): SongDataSource {
                return SongRemoteDataSource(
                    acServices!!, database.songSavedDao()
                )
            }
        }
    }

    private fun createRetrofit(): Retrofit {
        retrofit = Retrofit.Builder()
            .baseUrl("http://acnhapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
        acServices = retrofit!!.create(AnimalCrossingServices::class.java)

        return retrofit!!
    }

    private fun createDataBase(context: Context): AnimalCrossingDatabase {
        val migration1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE song (\n" +
                            "        id        INT    NOT NULL,\n" +
                            "        owned       INTEGER NOT NULL,\n" +
                            "        quantity    INTEGER NOT NULL,\n" +
                            "        PRIMARY KEY (\n" +
                            "            id\n" +
                            "        )\n" +
                            "    );"
                )
            }
        }
        val result = Room.databaseBuilder(
            context.applicationContext,
            AnimalCrossingDatabase::class.java, "ACNH.db"
        )
            .createFromAsset("acnh.db")
            .addMigrations(migration1_2)
            .build()
        database = result
        return result
    }
}