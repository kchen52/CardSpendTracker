package dev.ktown.cardspendtracker.data

import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CardDao,
    private val transactionDao: TransactionDao
) {
    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards()
    
    suspend fun getCardById(cardId: Long): Card? = cardDao.getCardById(cardId)
    
    suspend fun insertCard(card: Card): Long = cardDao.insertCard(card)
    
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    
    suspend fun deleteCard(card: Card) = cardDao.deleteCard(card)
    
    fun getTransactionsForCard(cardId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForCard(cardId)
    
    fun getTotalSpendForCard(cardId: Long): Flow<Double> =
        transactionDao.getTotalSpendForCard(cardId)
    
    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)
    
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)
    
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)
    
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)
}
