package com.hxbreak.animalcrossingtools.data.source

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MIGRATIONS : Migration(1, 2) {
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