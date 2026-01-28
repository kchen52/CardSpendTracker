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
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: CardRepository
    
    @Before
    fun setup() {
        cardDao = mockk()
        transactionDao = mockk()
        repository = CardRepository(cardDao, transactionDao)
    }
    
    @Test
    fun `getAllCards returns flow from dao`() = runTest {
        val cards = listOf(
            Card(1, "Test Card", 1000.0, null),
            Card(2, "Another Card", 2000.0, Date())
        )
        coEvery { cardDao.getAllCards() } returns flowOf(cards)
        
        val result = repository.getAllCards().first()
        
        assertEquals(cards, result)
    }
    
    @Test
    fun `insertCard calls dao insert`() = runTest {
        val card = Card(0, "New Card", 1500.0, null)
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
        val transaction = Transaction(0, 1L, 100.0, "Test", Date())
        coEvery { transactionDao.insertTransaction(transaction) } returns 1L
        
        repository.insertTransaction(transaction)
        
        coVerify { transactionDao.insertTransaction(transaction) }
    }
    
    @Test
    fun `deleteCard calls dao delete`() = runTest {
        val card = Card(1, "Card", 1000.0, null)
        coEvery { cardDao.deleteCard(card) } returns Unit
        
        repository.deleteCard(card)
        
        coVerify { cardDao.deleteCard(card) }
    }
}
