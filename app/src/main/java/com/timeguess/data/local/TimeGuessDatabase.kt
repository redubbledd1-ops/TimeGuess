package com.timeguess.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameHistoryEntity::class, GameRoundEntity::class],
    version = 4,
    exportSchema = false
)
abstract class TimeGuessDatabase : RoomDatabase() {
    
    abstract fun gameHistoryDao(): GameHistoryDao
    abstract fun gameRoundDao(): GameRoundDao
    
    companion object {
        @Volatile
        private var INSTANCE: TimeGuessDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_history ADD COLUMN playMode TEXT NOT NULL DEFAULT 'ONE_BY_ONE'")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_rounds (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gameId INTEGER NOT NULL,
                        roundIndex INTEGER NOT NULL,
                        playerName TEXT NOT NULL,
                        targetTimeMs INTEGER NOT NULL,
                        actualTimeMs INTEGER NOT NULL,
                        differenceMs INTEGER NOT NULL,
                        FOREIGN KEY(gameId) REFERENCES game_history(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_game_rounds_gameId ON game_rounds(gameId)")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_rounds ADD COLUMN tapOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_rounds ADD COLUMN positionIndex INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE game_rounds ADD COLUMN isWinner INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): TimeGuessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        TimeGuessDatabase::class.java,
                        "timeguess_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .fallbackToDestructiveMigration()
                        .build()
                } catch (_: Exception) {
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        TimeGuessDatabase::class.java
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }

                INSTANCE = instance
                instance
            }
        }
    }
}
