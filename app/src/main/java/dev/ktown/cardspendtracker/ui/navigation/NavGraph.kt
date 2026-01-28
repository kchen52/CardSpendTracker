package dev.ktown.cardspendtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.ktown.cardspendtracker.ui.screens.AddCardScreen
import dev.ktown.cardspendtracker.ui.screens.AddTransactionScreen
import dev.ktown.cardspendtracker.ui.screens.CardsScreen
import dev.ktown.cardspendtracker.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Cards : Screen("cards")
    object AddCard : Screen("add_card")
    object EditCard : Screen("edit_card/{cardId}") {
        fun createRoute(cardId: Long) = "edit_card/$cardId"
    }
    object AddTransaction : Screen("add_transaction/{cardId}") {
        fun createRoute(cardId: Long) = "add_transaction/$cardId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAddCard = { navController.navigate(Screen.AddCard.route) },
                onNavigateToAddTransaction = { cardId ->
                    navController.navigate(Screen.AddTransaction.createRoute(cardId))
                }
            )
        }
        
        composable(Screen.Cards.route) {
            CardsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCard = { navController.navigate(Screen.AddCard.route) },
                onNavigateToEditCard = { cardId ->
                    navController.navigate(Screen.EditCard.createRoute(cardId))
                },
                onNavigateToAddTransaction = { cardId ->
                    navController.navigate(Screen.AddTransaction.createRoute(cardId))
                }
            )
        }
        
        composable(Screen.AddCard.route) {
            AddCardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.EditCard.route) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                AddCardScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.AddTransaction.route) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                AddTransactionScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
