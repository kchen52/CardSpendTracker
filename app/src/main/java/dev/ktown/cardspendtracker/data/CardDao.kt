package dev.ktown.cardspendtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): Card?
    
    @Insert
    suspend fun insertCard(card: Card): Long
    
    @Update
    suspend fun updateCard(card: Card)
    
    @Delete
    suspend fun deleteCard(card: Card)
}
