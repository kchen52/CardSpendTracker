package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.ui.theme.CtaButton
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCard: () -> Unit,
    onNavigateToEditCard: (Long) -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    onNavigateToTransactions: (Long) -> Unit,
    onNavigateToGoals: (Long) -> Unit,
    viewModel: CardViewModel = viewModel(
        factory = CardViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).goalDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )
) {
    val cardsWithProgress by viewModel.cardsWithProgress.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddCard) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCard) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        if (cardsWithProgress.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No cards yet",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    CtaButton(onClick = onNavigateToAddCard, text = "Add Your First Card")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cardsWithProgress) { cardWithProgress ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToTransactions(cardWithProgress.card.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cardWithProgress.card.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Spent: ${currencyFormat.format(cardWithProgress.totalSpend)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Goals: ${cardWithProgress.goals.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onNavigateToAddTransaction(cardWithProgress.card.id) }) {
                                    Text("Add")
                                }
                                TextButton(onClick = { onNavigateToGoals(cardWithProgress.card.id) }) {
                                    Text("Goals")
                                }
                                TextButton(onClick = { onNavigateToEditCard(cardWithProgress.card.id) }) {
                                    Text("Card")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
