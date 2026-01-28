@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.ktown.cardspendtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class CardWithProgress(
    val card: Card,
    val totalSpend: Double,
    val progress: Float,
    val remaining: Double,
    val daysRemaining: Int?
)

class CardViewModel(private val repository: CardRepository) : ViewModel() {
    private val _cardsWithProgress = MutableStateFlow<List<CardWithProgress>>(emptyList())
    val cardsWithProgress: StateFlow<List<CardWithProgress>> = _cardsWithProgress.asStateFlow()
    
    init {
        loadCards()
    }
    
    private fun loadCards() {
        viewModelScope.launch {
            repository.getAllCards()
                .flatMapLatest { cards ->
                    if (cards.isEmpty()) {
                        kotlinx.coroutines.flow.flowOf(emptyList())
                    } else {
                        // Create flows for each card's spend
                        val spendFlows = cards.map { card ->
                            repository.getTotalSpendForCard(card.id)
                        }
                        
                        // Combine all spend flows
                        combine(spendFlows) { spends ->
                            cards.mapIndexed { index, card ->
                                val totalSpend = spends[index] as Double
                                calculateCardProgress(card, totalSpend)
                            }
                        }
                    }
                }
                .collect { cardsWithProgress ->
                    _cardsWithProgress.value = cardsWithProgress
                }
        }
    }
    
    private fun calculateCardProgress(card: Card, totalSpend: Double): CardWithProgress {
        val daysRemaining = card.endDate?.let { endDate ->
            val now = System.currentTimeMillis()
            val end = endDate.time
            if (end > now) {
                ((end - now) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
        }
        
        val progress = if (card.spendLimit > 0) {
            (totalSpend / card.spendLimit).coerceIn(0.toDouble(), 1.toDouble())
        } else {
            0f
        }.toFloat()
        
        return CardWithProgress(
            card = card,
            totalSpend = totalSpend,
            progress = progress,
            remaining = (card.spendLimit - totalSpend).coerceAtLeast(0.0),
            daysRemaining = daysRemaining
        )
    }
    
    suspend fun getCardById(cardId: Long): Card? = repository.getCardById(cardId)
    
    fun addCard(card: Card) {
        viewModelScope.launch {
            repository.insertCard(card)
        }
    }
    
    fun updateCard(card: Card) {
        viewModelScope.launch {
            repository.updateCard(card)
        }
    }
    
    fun deleteCard(card: Card) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }
}
