package com.example.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "character_records")
data class CharacterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val surname: String,
    @Ignore val courtesy: String? = null,
    @Ignore val alias: String? = null,
    val gender: String,
    val birthYear: Int,
    val deathYear: Int,
    val deathAge: Int,
    val hometown: String,
    val origin: String,
    val maxTitle: String,
    val spouse: String,
    val faction: String,
    val martial: Int,
    val intelligence: Int,
    val command: Int,
    val politics: Int,
    val charisma: Int,
    val reputation: Int,
    val gold: Int,
    val lifeLogs: String, // Delimited by '|'
    val talents: String,  // Delimited by ','
    val endingRank: String, // SS, S, A, B, C...
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "active_game_session")
data class ActiveGameSession(
    @PrimaryKey val id: Int = 1, // Fix to 1 for single save slot
    val name: String,
    val surname: String,
    @Ignore val courtesy: String? = null,
    @Ignore val alias: String? = null,
    val gender: String,
    val age: Int,
    val currentYear: Int,
    val hometown: String,
    val origin: String,
    val martial: Int,
    val intelligence: Int,
    val command: Int,
    val politics: Int,
    val charisma: Int,
    val reputation: Int,
    val gold: Int,
    val currentFaction: String,
    val currentJob: String,
    val spouse: String,
    val childrenCount: Int,
    val lifeLogs: String, // Delimited by '|'
    val unificationProgress: Int, // 0 - 100
    val inventory: String,        // Delimited by ','
    val relations: String,        // Semicolon-and-colon delimited: "赵云:85;曹操:20"
    val talents: String,          // Delimited by ','
    val timestamp: Long = System.currentTimeMillis()
)
