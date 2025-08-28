package com.kidd1412.workcalendar.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        val current = INSTANCE
        if (current != null) return current

        synchronized(this) {
            val existing = INSTANCE
            if (existing != null) return existing

            val created = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "workcalendar.db"
            ).build()
            INSTANCE = created
            return created
        }
    }
}



