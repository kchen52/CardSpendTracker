package dev.ktown.cardspendtracker.ui.viewmodel

import app.cash.turbine.test
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Goal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date
import dev.ktown.cardspendtracker.MainDispatcherRule

class CardViewModelTest {
    private lateinit var repository: CardRepository
    private lateinit var viewModel: CardViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = CardViewModel(repository)
    }
    
    @Test
    fun `addCard calls repository insertCard`() = runTest {
        val card = Card(0, "Test Card")
        coEvery { repository.insertCard(card) } returns 1L
        every { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.addCard(card)
        
        coVerify { repository.insertCard(card) }
    }
    
    @Test
    fun `updateCard calls repository updateCard`() = runTest {
        val card = Card(1, "Updated Card")
        coEvery { repository.updateCard(card) } returns Unit
        every { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.updateCard(card)
        
        coVerify { repository.updateCard(card) }
    }
    
    @Test
    fun `deleteCard calls repository deleteCard`() = runTest {
        val card = Card(1, "Card")
        coEvery { repository.deleteCard(card) } returns Unit
        every { repository.getAllCards() } returns flowOf(emptyList())
        
        viewModel.deleteCard(card)
        
        coVerify { repository.deleteCard(card) }
    }
    
    @Test
    fun `getCardById returns card from repository`() = runTest {
        val cardId = 1L
        val card = Card(cardId, "Test Card")
        coEvery { repository.getCardById(cardId) } returns card
        
        val result = viewModel.getCardById(cardId)
        
        assertEquals(card, result)
    }
    
    @Test
    fun `cardsWithProgress calculates progress correctly`() = runTest {
        val card = Card(1, "Test Card")
        val goal = Goal(id = 1, cardId = 1, title = "Goal", spendLimit = 1000.0)
        every { repository.getAllCards() } returns flowOf(listOf(card))
        every { repository.getTotalSpendForCard(1) } returns flowOf(500.0)
        every { repository.getGoalsForCard(1) } returns flowOf(listOf(goal))

        val vm = CardViewModel(repository)
        vm.cardsWithProgress.test {
            val first = awaitItem()
            val cardsWithProgress = if (first.isEmpty()) awaitItem() else first
            assertEquals(1, cardsWithProgress.size)
            assertEquals(500.0, cardsWithProgress[0].totalSpend, 0.01)
            assertEquals(1, cardsWithProgress[0].goals.size)
            assertEquals(0.5f, cardsWithProgress[0].goals[0].progress, 0.01f)
            assertEquals(500.0, cardsWithProgress[0].goals[0].remaining, 0.01)
        }
    }
    
    @Test
    fun `cardsWithProgress calculates days remaining correctly`() = runTest {
        val futureDate = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L) // 10 days from now
        val card = Card(1, "Test Card")
        val goal = Goal(id = 1, cardId = 1, title = "Goal", spendLimit = 1000.0, endDate = futureDate)
        every { repository.getAllCards() } returns flowOf(listOf(card))
        every { repository.getTotalSpendForCard(1) } returns flowOf(0.0)
        every { repository.getGoalsForCard(1) } returns flowOf(listOf(goal))

        val vm = CardViewModel(repository)
        vm.cardsWithProgress.test {
            val first = awaitItem()
            val cardsWithProgress = if (first.isEmpty()) awaitItem() else first
            assertEquals(1, cardsWithProgress.size)
            assertNotNull(cardsWithProgress[0].goals[0].daysRemaining)
            assertTrue(cardsWithProgress[0].goals[0].daysRemaining!! >= 9) // Should be around 10 days
        }
    }
    
    @Test
    fun `cardsWithProgress handles expired end date`() = runTest {
        val pastDate = Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L) // 10 days ago
        val card = Card(1, "Test Card")
        val goal = Goal(id = 1, cardId = 1, title = "Goal", spendLimit = 1000.0, endDate = pastDate)
        every { repository.getAllCards() } returns flowOf(listOf(card))
        every { repository.getTotalSpendForCard(1) } returns flowOf(0.0)
        every { repository.getGoalsForCard(1) } returns flowOf(listOf(goal))

        val vm = CardViewModel(repository)
        vm.cardsWithProgress.test {
            val first = awaitItem()
            val cardsWithProgress = if (first.isEmpty()) awaitItem() else first
            assertEquals(1, cardsWithProgress.size)
            assertEquals(0, cardsWithProgress[0].goals[0].daysRemaining)
        }
    }
}
