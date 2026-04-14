package dev.ktown.cardspendtracker.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class CardRepositoryTest {
    private lateinit var cardDao: CardDao
    private lateinit var goalDao: GoalDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: CardRepository
    
    @Before
    fun setup() {
        cardDao = mockk()
        goalDao = mockk()
        transactionDao = mockk()
        repository = CardRepository(cardDao, goalDao, transactionDao)
    }
    
    @Test
    fun `getAllCards returns flow from dao`() = runTest {
        val cards = listOf(
            Card(id = 1, name = "Test Card"),
            Card(id = 2, name = "Another Card")
        )
        coEvery { cardDao.getAllCards() } returns flowOf(cards)
        
        val result = repository.getAllCards().first()
        
        assertEquals(cards, result)
    }
    
    @Test
    fun `insertCard calls dao insert`() = runTest {
        val card = Card(id = 0, name = "New Card")
        coEvery { cardDao.insertCard(card) } returns 1L
        
        repository.insertCard(card)
        
        coVerify { cardDao.insertCard(card) }
    }
    
    @Test
    fun `getTotalSpendForCard returns flow from dao`() = runTest {
        val cardId = 1L
        val totalSpend = 500.0
        coEvery { transactionDao.getTotalSpendForCard(cardId) } returns flowOf(totalSpend)
        
        val result = repository.getTotalSpendForCard(cardId).first()
        
        assertEquals(totalSpend, result, 0.01)
    }
    
    @Test
    fun `insertTransaction calls dao insert`() = runTest {
        val transaction = Transaction(
            id = 0,
            cardId = 1L,
            amount = 100.0,
            description = "Test",
            date = Date()
        )
        coEvery { transactionDao.insertTransaction(transaction) } returns 1L
        
        repository.insertTransaction(transaction)
        
        coVerify { transactionDao.insertTransaction(transaction) }
    }
    
    @Test
    fun `deleteCard calls dao delete`() = runTest {
        val card = Card(id = 1, name = "Card")
        coEvery { cardDao.deleteCard(card) } returns Unit
        
        repository.deleteCard(card)
        
        coVerify { cardDao.deleteCard(card) }
    }
}
