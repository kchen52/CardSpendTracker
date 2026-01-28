package dev.ktown.cardspendtracker.ui.viewmodel

import app.cash.turbine.test
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class CardViewModelTest {
    private lateinit var repository: CardRepository
    private lateinit var viewModel: CardViewModel
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = CardViewModel(repository)
    }
    
    @Test
    fun `addCard calls repository insertCard`() = runTest {
        val card = Card(0, "Test Card", 1000.0, null)
        coEvery { repository.insertCard(card) } returns 1L
        coEvery { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.addCard(card)
        
        coVerify { repository.insertCard(card) }
    }
    
    @Test
    fun `updateCard calls repository updateCard`() = runTest {
        val card = Card(1, "Updated Card", 1500.0, null)
        coEvery { repository.updateCard(card) } returns Unit
        coEvery { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.updateCard(card)
        
        coVerify { repository.updateCard(card) }
    }
    
    @Test
    fun `deleteCard calls repository deleteCard`() = runTest {
        val card = Card(1, "Card", 1000.0, null)
        coEvery { repository.deleteCard(card) } returns Unit
        coEvery { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.deleteCard(card)
        
        coVerify { repository.deleteCard(card) }
    }
    
    @Test
    fun `getCardById returns card from repository`() = runTest {
        val cardId = 1L
        val card = Card(cardId, "Test Card", 1000.0, null)
        coEvery { repository.getCardById(cardId) } returns card
        
        val result = viewModel.getCardById(cardId)
        
        assertEquals(card, result)
    }
    
    @Test
    fun `cardsWithProgress calculates progress correctly`() = runTest(StandardTestDispatcher()) {
        val card = Card(1, "Test Card", 1000.0, null)
        coEvery { repository.getAllCards() } returns flowOf(listOf(card))
        coEvery { repository.getTotalSpendForCard(1) } returns flowOf(500.0)
        
        viewModel.cardsWithProgress.test {
            skipItems(1) // Skip initial empty list
            val cardsWithProgress = awaitItem()
            assertEquals(1, cardsWithProgress.size)
            assertEquals(0.5f, cardsWithProgress[0].progress, 0.01f)
            assertEquals(500.0, cardsWithProgress[0].totalSpend, 0.01)
            assertEquals(500.0, cardsWithProgress[0].remaining, 0.01)
        }
    }
    
    @Test
    fun `cardsWithProgress calculates days remaining correctly`() = runTest(StandardTestDispatcher()) {
        val futureDate = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L) // 10 days from now
        val card = Card(1, "Test Card", 1000.0, futureDate)
        coEvery { repository.getAllCards() } returns flowOf(listOf(card))
        coEvery { repository.getTotalSpendForCard(1) } returns flowOf(0.0)
        
        viewModel.cardsWithProgress.test {
            skipItems(1) // Skip initial empty list
            val cardsWithProgress = awaitItem()
            assertEquals(1, cardsWithProgress.size)
            assertNotNull(cardsWithProgress[0].daysRemaining)
            assertTrue(cardsWithProgress[0].daysRemaining!! >= 9) // Should be around 10 days
        }
    }
    
    @Test
    fun `cardsWithProgress handles expired end date`() = runTest(StandardTestDispatcher()) {
        val pastDate = Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L) // 10 days ago
        val card = Card(1, "Test Card", 1000.0, pastDate)
        coEvery { repository.getAllCards() } returns flowOf(listOf(card))
        coEvery { repository.getTotalSpendForCard(1) } returns flowOf(0.0)
        
        viewModel.cardsWithProgress.test {
            skipItems(1) // Skip initial empty list
            val cardsWithProgress = awaitItem()
            assertEquals(1, cardsWithProgress.size)
            assertEquals(0, cardsWithProgress[0].daysRemaining)
        }
    }
}
