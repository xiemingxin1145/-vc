package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow

class GameRepository(context: Context) {
    
    private val database: GameDatabase = Room.databaseBuilder(
        context.applicationContext,
        GameDatabase::class.java,
        "sanguo_life_sim.db"
    ).fallbackToDestructiveMigration().build()

    private val dao = database.gameDao()

    // Records (Hall of Fame)
    val allRecords: Flow<List<CharacterRecord>> = dao.getAllRecords()

    suspend fun insertRecord(record: CharacterRecord) {
        dao.insertRecord(record)
    }

    suspend fun clearAllRecords() {
        dao.clearAllRecords()
    }

    // Active Session (Saves)
    val activeSessionFlow: Flow<ActiveGameSession?> = dao.getActiveSessionFlow()

    suspend fun getActiveSession(): ActiveGameSession? {
        return dao.getActiveSession()
    }

    suspend fun saveSession(session: ActiveGameSession) {
        dao.saveSession(session)
    }

    suspend fun deleteActiveSession() {
        dao.deleteActiveSession()
    }
}
