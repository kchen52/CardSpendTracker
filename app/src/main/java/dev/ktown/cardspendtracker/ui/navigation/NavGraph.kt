package dev.ktown.cardspendtracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.ktown.cardspendtracker.ui.screens.AddCardScreen
import dev.ktown.cardspendtracker.ui.screens.AddGoalScreen
import dev.ktown.cardspendtracker.ui.screens.AddTransactionScreen
import dev.ktown.cardspendtracker.ui.screens.CardsScreen
import dev.ktown.cardspendtracker.ui.screens.GoalsScreen
import dev.ktown.cardspendtracker.ui.screens.HomeScreen
import dev.ktown.cardspendtracker.ui.screens.TransactionsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Cards : Screen("cards")
    object AddCard : Screen("add_card")
    object EditCard : Screen("edit_card/{cardId}") {
        fun createRoute(cardId: Long) = "edit_card/$cardId"
    }
    object Goals : Screen("goals/{cardId}") {
        fun createRoute(cardId: Long) = "goals/$cardId"
    }
    object AddGoal : Screen("add_goal/{cardId}") {
        fun createRoute(cardId: Long) = "add_goal/$cardId"
    }
    object EditGoal : Screen("edit_goal/{cardId}/{goalId}") {
        fun createRoute(cardId: Long, goalId: Long) = "edit_goal/$cardId/$goalId"
    }
    object AddTransaction : Screen("add_transaction/{cardId}") {
        fun createRoute(cardId: Long) = "add_transaction/$cardId"
    }
    object EditTransaction : Screen("edit_transaction/{cardId}/{transactionId}") {
        fun createRoute(cardId: Long, transactionId: Long) = "edit_transaction/$cardId/$transactionId"
    }
    object Transactions : Screen("transactions/{cardId}") {
        fun createRoute(cardId: Long) = "transactions/$cardId"
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
        composable(
            route = Screen.Home.route,
            enterTransition = {
                null // No transition for start destination
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            HomeScreen(
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAddCard = { navController.navigate(Screen.AddCard.route) },
                onNavigateToAddTransaction = { cardId ->
                    navController.navigate(Screen.AddTransaction.createRoute(cardId))
                },
                onNavigateToTransactions = { cardId ->
                    navController.navigate(Screen.Transactions.createRoute(cardId))
                },
                onNavigateToGoals = { cardId ->
                    navController.navigate(Screen.Goals.createRoute(cardId))
                }
            )
        }
        
        composable(
            route = Screen.Cards.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            CardsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCard = { navController.navigate(Screen.AddCard.route) },
                onNavigateToEditCard = { cardId ->
                    navController.navigate(Screen.EditCard.createRoute(cardId))
                },
                onNavigateToAddTransaction = { cardId ->
                    navController.navigate(Screen.AddTransaction.createRoute(cardId))
                },
                onNavigateToTransactions = { cardId ->
                    navController.navigate(Screen.Transactions.createRoute(cardId))
                },
                onNavigateToGoals = { cardId ->
                    navController.navigate(Screen.Goals.createRoute(cardId))
                }
            )
        }
        
        composable(
            route = Screen.AddCard.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            AddCardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditCard.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                AddCardScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.Goals.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                GoalsScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddGoal = { id -> navController.navigate(Screen.AddGoal.createRoute(id)) },
                    onNavigateToEditGoal = { id, goalId -> navController.navigate(Screen.EditGoal.createRoute(id, goalId)) }
                )
            }
        }

        composable(
            route = Screen.AddGoal.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                AddGoalScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.EditGoal.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull()
            if (cardId != null && goalId != null) {
                AddGoalScreen(
                    cardId = cardId,
                    goalId = goalId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = Screen.AddTransaction.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                AddTransactionScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = Screen.EditTransaction.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()
            if (cardId != null && transactionId != null) {
                AddTransactionScreen(
                    cardId = cardId,
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = Screen.Transactions.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")?.toLongOrNull()
            if (cardId != null) {
                TransactionsScreen(
                    cardId = cardId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddTransaction = { cardId ->
                        navController.navigate(Screen.AddTransaction.createRoute(cardId))
                    },
                    onNavigateToEditTransaction = { cardId, transactionId ->
                        navController.navigate(Screen.EditTransaction.createRoute(cardId, transactionId))
                    }
                )
            }
        }
    }
}
