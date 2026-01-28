@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.ktown.cardspendtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Goal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class GoalWithProgress(
    val goal: Goal,
    val progress: Float,
    val remaining: Double,
    val daysRemaining: Int?
)

data class CardWithGoalsProgress(
    val card: Card,
    val totalSpend: Double,
    val goals: List<GoalWithProgress>
)

class CardViewModel(private val repository: CardRepository) : ViewModel() {
    private val _cardsWithProgress = MutableStateFlow<List<CardWithGoalsProgress>>(emptyList())
    val cardsWithProgress: StateFlow<List<CardWithGoalsProgress>> = _cardsWithProgress.asStateFlow()
    
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
                        val cardFlows = cards.map { card ->
                            combine(
                                repository.getTotalSpendForCard(card.id),
                                repository.getGoalsForCard(card.id)
                            ) { totalSpend, goals ->
                                CardWithGoalsProgress(
                                    card = card,
                                    totalSpend = totalSpend,
                                    goals = goals.map { goal -> calculateGoalProgress(goal, totalSpend) }
                                )
                            }
                        }

                        combine(cardFlows) { items ->
                            items.map { it as CardWithGoalsProgress }
                        }
                    }
                }
                .collect { cardsWithProgress ->
                    _cardsWithProgress.value = cardsWithProgress
                }
        }
    }
    
    private fun calculateGoalProgress(goal: Goal, totalSpend: Double): GoalWithProgress {
        val daysRemaining = goal.endDate?.let { endDate ->
            val now = System.currentTimeMillis()
            val end = endDate.time
            if (end > now) {
                ((end - now) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                0
            }
        }

        val progress = if (goal.spendLimit > 0) {
            (totalSpend / goal.spendLimit).coerceIn(0.toDouble(), 1.toDouble())
        } else {
            0f
        }.toFloat()

        return GoalWithProgress(
            goal = goal,
            progress = progress,
            remaining = (goal.spendLimit - totalSpend).coerceAtLeast(0.0),
            daysRemaining = daysRemaining
        )
    }
    
    suspend fun getCardById(cardId: Long): Card? = repository.getCardById(cardId)
    
    fun addCard(card: Card) {
        viewModelScope.launch {
            repository.insertCard(card)
        }
    }

    fun addCardWithInitialGoal(
        cardName: String,
        cardColor: Long,
        goalTitle: String,
        spendLimit: Double,
        endDate: java.util.Date?,
        comment: String
    ) {
        viewModelScope.launch {
            val cardId = repository.insertCard(Card(name = cardName, color = cardColor))
            repository.insertGoal(
                Goal(
                    cardId = cardId,
                    title = goalTitle,
                    spendLimit = spendLimit,
                    endDate = endDate,
                    comment = comment
                )
            )
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
