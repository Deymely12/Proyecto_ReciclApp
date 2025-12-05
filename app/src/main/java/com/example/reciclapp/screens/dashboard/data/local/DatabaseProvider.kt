package com.example.reciclapp.screens.dashboard.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "usuarios_db"
            ).build()
            db = instance
            instance
        }
    }
}
