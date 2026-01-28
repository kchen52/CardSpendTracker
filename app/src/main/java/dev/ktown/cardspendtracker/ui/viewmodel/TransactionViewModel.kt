package dev.ktown.cardspendtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ktown.cardspendtracker.data.Transaction
import dev.ktown.cardspendtracker.data.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: CardRepository) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _totalSpend = MutableStateFlow(0.0)
    val totalSpend: StateFlow<Double> = _totalSpend.asStateFlow()
    
    fun loadTransactionsForCard(cardId: Long) {
        viewModelScope.launch {
            repository.getTransactionsForCard(cardId).collect { transactions ->
                _transactions.value = transactions
            }
            
            repository.getTotalSpendForCard(cardId).collect { total ->
                _totalSpend.value = total
            }
        }
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
    
    suspend fun getTransactionById(transactionId: Long): Transaction? {
        return repository.getTransactionById(transactionId)
    }
}
