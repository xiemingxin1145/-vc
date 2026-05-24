package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // ------------------------------------
    // Playthrough Records (Hall of Fame)
    // ------------------------------------
    @Query("SELECT * FROM character_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CharacterRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CharacterRecord)

    @Query("DELETE FROM character_records")
    suspend fun clearAllRecords()

    // ------------------------------------
    // Active Game Save Session
    // ------------------------------------
    @Query("SELECT * FROM active_game_session WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<ActiveGameSession?>

    @Query("SELECT * FROM active_game_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): ActiveGameSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: ActiveGameSession)

    @Query("DELETE FROM active_game_session WHERE id = 1")
    suspend fun deleteActiveSession()
}

@Database(entities = [CharacterRecord::class, ActiveGameSession::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
