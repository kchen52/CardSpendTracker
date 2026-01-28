package dev.ktown.cardspendtracker.data

import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CardDao,
    private val goalDao: GoalDao,
    private val transactionDao: TransactionDao
) {
    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards()
    
    suspend fun getCardById(cardId: Long): Card? = cardDao.getCardById(cardId)
    
    suspend fun getCardByUniqueId(uniqueId: String): Card? = cardDao.getCardByUniqueId(uniqueId)
    
    suspend fun insertCard(card: Card): Long = cardDao.insertCard(card)
    
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    
    suspend fun deleteCard(card: Card) = cardDao.deleteCard(card)

    fun getGoalsForCard(cardId: Long): Flow<List<Goal>> =
        goalDao.getGoalsForCard(cardId)

    suspend fun getGoalById(goalId: Long): Goal? =
        goalDao.getGoalById(goalId)

    suspend fun insertGoal(goal: Goal): Long =
        goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: Goal) =
        goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: Goal) =
        goalDao.deleteGoal(goal)
    
    fun getTransactionsForCard(cardId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForCard(cardId)
    
    suspend fun getTransactionById(transactionId: Long): Transaction? =
        transactionDao.getTransactionById(transactionId)
    
    suspend fun getTransactionByUniqueId(uniqueId: String): Transaction? =
        transactionDao.getTransactionByUniqueId(uniqueId)
    
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
