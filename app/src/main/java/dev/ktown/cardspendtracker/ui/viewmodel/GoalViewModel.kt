package dev.ktown.cardspendtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalViewModel(private val repository: CardRepository) : ViewModel() {
    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    fun loadGoalsForCard(cardId: Long) {
        viewModelScope.launch {
            repository.getGoalsForCard(cardId).collect { goals ->
                _goals.value = goals
            }
        }
    }

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    suspend fun getGoalById(goalId: Long): Goal? = repository.getGoalById(goalId)
}

