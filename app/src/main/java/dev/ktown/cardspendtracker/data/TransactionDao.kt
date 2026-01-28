package dev.ktown.cardspendtracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE cardId = :cardId ORDER BY date DESC")
    fun getTransactionsForCard(cardId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): Transaction?
    
    @Query("SELECT * FROM transactions WHERE uniqueId = :uniqueId")
    suspend fun getTransactionByUniqueId(uniqueId: String): Transaction?
    
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE cardId = :cardId")
    fun getTotalSpendForCard(cardId: Long): Flow<Double>
    
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
