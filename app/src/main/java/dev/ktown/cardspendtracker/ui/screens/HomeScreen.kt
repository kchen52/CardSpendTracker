package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import dev.ktown.cardspendtracker.ui.viewmodel.CardWithProgress
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCards: () -> Unit,
    onNavigateToAddCard: () -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    viewModel: CardViewModel = viewModel(
        factory = CardViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )
) {
    val cardsWithProgress by viewModel.cardsWithProgress.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Spend Tracker") },
                actions = {
                    IconButton(onClick = onNavigateToCards) {
                        Icon(Icons.Default.CreditCard, contentDescription = "View Cards")
                    }
                    IconButton(onClick = onNavigateToAddCard) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (cardsWithProgress.isNotEmpty()) {
                        onNavigateToAddTransaction(cardsWithProgress.first().card.id)
                    } else {
                        onNavigateToAddCard()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        if (cardsWithProgress.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No cards yet",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Add your first card to start tracking",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onNavigateToAddCard) {
                        Text("Add Card")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cardsWithProgress) { cardWithProgress ->
                    CardProgressItem(
                        cardWithProgress = cardWithProgress,
                        onAddTransaction = { onNavigateToAddTransaction(cardWithProgress.card.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardProgressItem(
    cardWithProgress: CardWithProgress,
    onAddTransaction: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cardWithProgress.card.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddTransaction) {
                    Text("Add Transaction")
                }
            }
            
            LinearProgressIndicator(
                progress = { cardWithProgress.progress },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    cardWithProgress.progress >= 1f -> MaterialTheme.colorScheme.error
                    cardWithProgress.progress >= 0.8f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(cardWithProgress.totalSpend),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(cardWithProgress.remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Limit: ${currencyFormat.format(cardWithProgress.card.spendLimit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                cardWithProgress.daysRemaining?.let { days ->
                    Text(
                        text = if (days > 0) "$days days remaining" else "Expired",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (days > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                cardWithProgress.card.endDate?.let { date ->
                    Text(
                        text = "Ends: ${dateFormat.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
