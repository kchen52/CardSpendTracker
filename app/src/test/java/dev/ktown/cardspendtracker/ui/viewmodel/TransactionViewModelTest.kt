package dev.ktown.cardspendtracker.ui.viewmodel

import app.cash.turbine.test
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Transaction
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

class TransactionViewModelTest {
    private lateinit var repository: CardRepository
    private lateinit var viewModel: TransactionViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = TransactionViewModel(repository)
    }
    
    @Test
    fun `addTransaction calls repository insertTransaction`() = runTest {
        val transaction = Transaction(
            id = 0,
            cardId = 1L,
            amount = 100.0,
            description = "Test",
            date = Date()
        )
        coEvery { repository.insertTransaction(transaction) } returns 1L
        
        viewModel.addTransaction(transaction)
        
        coVerify { repository.insertTransaction(transaction) }
    }

    @Test
    fun `addTransactions inserts each transaction`() = runTest {
        val first = Transaction(
            id = 0,
            cardId = 1L,
            amount = 100.0,
            description = "Groceries",
            date = Date()
        )
        val second = Transaction(
            id = 0,
            cardId = 1L,
            amount = 49.99,
            description = "Coffee",
            date = Date()
        )
        coEvery { repository.insertTransaction(any()) } returns 1L

        viewModel.addTransactions(listOf(first, second))

        coVerify(exactly = 1) { repository.insertTransaction(first) }
        coVerify(exactly = 1) { repository.insertTransaction(second) }
    }
    
    @Test
    fun `updateTransaction calls repository updateTransaction`() = runTest {
        val transaction = Transaction(
            id = 1,
            cardId = 1L,
            amount = 150.0,
            description = "Updated",
            date = Date()
        )
        coEvery { repository.updateTransaction(transaction) } returns Unit
        
        viewModel.updateTransaction(transaction)
        
        coVerify { repository.updateTransaction(transaction) }
    }
    
    @Test
    fun `deleteTransaction calls repository deleteTransaction`() = runTest {
        val transaction = Transaction(
            id = 1,
            cardId = 1L,
            amount = 100.0,
            description = "Test",
            date = Date()
        )
        coEvery { repository.deleteTransaction(transaction) } returns Unit
        
        viewModel.deleteTransaction(transaction)
        
        coVerify { repository.deleteTransaction(transaction) }
    }
    
    @Test
    fun `loadTransactionsForCard loads transactions and total spend`() = runTest {
        val cardId = 1L
        val transactions = listOf(
            Transaction(
                id = 1,
                cardId = cardId,
                amount = 100.0,
                description = "Transaction 1",
                date = Date()
            ),
            Transaction(
                id = 2,
                cardId = cardId,
                amount = 200.0,
                description = "Transaction 2",
                date = Date()
            )
        )
        val totalSpend = 300.0
        
        every { repository.getTransactionsForCard(cardId) } returns flowOf(transactions)
        every { repository.getTotalSpendForCard(cardId) } returns flowOf(totalSpend)
        
        viewModel.loadTransactionsForCard(cardId)
        
        viewModel.transactions.test {
            val result = awaitItem()
            assertEquals(transactions, result)
        }
        
        viewModel.totalSpend.test {
            val result = awaitItem()
            assertEquals(totalSpend, result, 0.01)
        }
    }
}
