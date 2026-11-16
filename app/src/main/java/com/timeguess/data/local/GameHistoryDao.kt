package com.timeguess.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<GameHistoryEntity>>
    
    @Query("SELECT * FROM game_history WHERE id = :id")
    suspend fun getById(id: Long): GameHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameHistoryEntity): Long
    
    @Delete
    suspend fun delete(entity: GameHistoryEntity)
    
    @Query("DELETE FROM game_history WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM game_history")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun getCount(): Int
}

@Dao
interface GameRoundDao {
    
    @Query("SELECT * FROM game_rounds WHERE gameId = :gameId ORDER BY roundIndex, playerName")
    suspend fun getRoundsForGame(gameId: Long): List<GameRoundEntity>
    
    @Query("SELECT * FROM game_rounds WHERE gameId = :gameId ORDER BY roundIndex, playerName")
    fun getRoundsForGameFlow(gameId: Long): Flow<List<GameRoundEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GameRoundEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<GameRoundEntity>)
    
    @Query("DELETE FROM game_rounds WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: Long)
}
