package dev.ktown.cardspendtracker.ui.viewmodel

import app.cash.turbine.test
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class TransactionViewModelTest {
    private lateinit var repository: CardRepository
    private lateinit var viewModel: TransactionViewModel
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = TransactionViewModel(repository)
    }
    
    @Test
    fun `addTransaction calls repository insertTransaction`() = runTest {
        val transaction = Transaction(0, 1L, 100.0, "Test", Date())
        coEvery { repository.insertTransaction(transaction) } returns 1L
        
        viewModel.addTransaction(transaction)
        
        coVerify { repository.insertTransaction(transaction) }
    }
    
    @Test
    fun `updateTransaction calls repository updateTransaction`() = runTest {
        val transaction = Transaction(1, 1L, 150.0, "Updated", Date())
        coEvery { repository.updateTransaction(transaction) } returns Unit
        
        viewModel.updateTransaction(transaction)
        
        coVerify { repository.updateTransaction(transaction) }
    }
    
    @Test
    fun `deleteTransaction calls repository deleteTransaction`() = runTest {
        val transaction = Transaction(1, 1L, 100.0, "Test", Date())
        coEvery { repository.deleteTransaction(transaction) } returns Unit
        
        viewModel.deleteTransaction(transaction)
        
        coVerify { repository.deleteTransaction(transaction) }
    }
    
    @Test
    fun `loadTransactionsForCard loads transactions and total spend`() = runTest {
        val cardId = 1L
        val transactions = listOf(
            Transaction(1, cardId, 100.0, "Transaction 1", Date()),
            Transaction(2, cardId, 200.0, "Transaction 2", Date())
        )
        val totalSpend = 300.0
        
        coEvery { repository.getTransactionsForCard(cardId) } returns flowOf(transactions)
        coEvery { repository.getTotalSpendForCard(cardId) } returns flowOf(totalSpend)
        
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
