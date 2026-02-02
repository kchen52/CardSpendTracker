package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Transaction
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import dev.ktown.cardspendtracker.ui.CalendarDatePickerDialog
import dev.ktown.cardspendtracker.ui.theme.CtaButton
import dev.ktown.cardspendtracker.ui.viewmodel.TransactionViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.TransactionViewModelFactory
import java.util.*

/**
 * Filters input to valid dollar amount characters only: digits and at most one decimal with at most 2 decimal places.
 * Returns the filtered string to apply, or null to reject the change.
 */
private fun filterDollarAmountInput(newText: String): String? {
    if (newText.isEmpty()) return ""
    var hasDot = false
    var digitsAfterDot = 0
    return buildString {
        for (c in newText) {
            when {
                c == '.' -> if (!hasDot) {
                    append(c)
                    hasDot = true
                } else return null
                c.isDigit() -> {
                    if (hasDot && digitsAfterDot >= 2) return null
                    if (hasDot) digitsAfterDot++
                    append(c)
                }
                else -> return null
            }
        }
    }
}

/**
 * True if [amount] is a valid positive dollar amount (parses to a number > 0).
 */
private fun isValidDollarAmount(amount: String): Boolean {
    if (amount.isBlank()) return false
    val value = amount.toDoubleOrNull() ?: return false
    return value > 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    cardId: Long,
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    cardViewModel: CardViewModel = viewModel(
        factory = CardViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).goalDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    ),
    transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).goalDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var cardName by remember { mutableStateOf("") }
    var existingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val isEditing = transactionId != null
    
    LaunchedEffect(cardId, transactionId) {
        val card = cardViewModel.getCardById(cardId)
        card?.let { cardName = it.name }
        
        // Load transaction data if editing
        transactionId?.let { id ->
            val transaction = transactionViewModel.getTransactionById(id)
            transaction?.let {
                existingTransaction = it
                amount = it.amount.toString()
                description = it.description
                transactionDate = it.date
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (cardName.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Card: $cardName",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = { newText ->
                    filterDollarAmountInput(newText)?.let { amount = it }
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { newText ->
                    description = newText.replaceFirstChar { c ->
                        if (c.isLetter()) c.uppercaseChar() else c
                    }
                },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (showDatePicker) {
                CalendarDatePickerDialog(
                    selectedDate = transactionDate,
                    onDateSelected = { transactionDate = it },
                    onDismiss = { showDatePicker = false }
                )
            }
            
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionDate)}")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            CtaButton(
                onClick = {
                    val transactionAmount = amount.toDoubleOrNull() ?: 0.0
                    if (transactionAmount > 0) {
                        val transaction = if (isEditing && existingTransaction != null) {
                            existingTransaction!!.copy(
                                amount = transactionAmount,
                                description = description,
                                date = transactionDate
                            )
                        } else {
                            Transaction(
                                cardId = cardId,
                                amount = transactionAmount,
                                description = description,
                                date = transactionDate
                            )
                        }
                        if (isEditing) {
                            transactionViewModel.updateTransaction(transaction)
                        } else {
                            transactionViewModel.addTransaction(transaction)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValidDollarAmount(amount)
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
