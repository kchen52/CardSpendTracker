package dev.ktown.cardspendtracker.ui.screens

import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.Goal
import dev.ktown.cardspendtracker.ui.viewmodel.CardWithGoalsProgress
import dev.ktown.cardspendtracker.ui.viewmodel.GoalWithProgress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenLogicTest {

    @Test
    fun `isCardFullyCompleted returns true when all goals are complete`() {
        val cardWithProgress = CardWithGoalsProgress(
            card = Card(id = 1, name = "Card"),
            totalSpend = 500.0,
            goals = listOf(
                GoalWithProgress(
                    goal = Goal(id = 1, cardId = 1, title = "Goal 1", spendLimit = 100.0),
                    progress = 1f,
                    remaining = 0.0,
                    daysRemaining = 10
                ),
                GoalWithProgress(
                    goal = Goal(id = 2, cardId = 1, title = "Goal 2", spendLimit = 100.0),
                    progress = 1f,
                    remaining = 0.0,
                    daysRemaining = 5
                )
            )
        )

        assertTrue(isCardFullyCompleted(cardWithProgress))
    }

    @Test
    fun `isCardFullyCompleted returns false for empty or partial goals`() {
        val emptyGoalsCard = CardWithGoalsProgress(
            card = Card(id = 1, name = "Card"),
            totalSpend = 0.0,
            goals = emptyList()
        )
        val partialGoalsCard = CardWithGoalsProgress(
            card = Card(id = 2, name = "Card 2"),
            totalSpend = 50.0,
            goals = listOf(
                GoalWithProgress(
                    goal = Goal(id = 3, cardId = 2, title = "Goal 3", spendLimit = 100.0),
                    progress = 0.5f,
                    remaining = 50.0,
                    daysRemaining = 8
                )
            )
        )

        assertFalse(isCardFullyCompleted(emptyGoalsCard))
        assertFalse(isCardFullyCompleted(partialGoalsCard))
    }
}
