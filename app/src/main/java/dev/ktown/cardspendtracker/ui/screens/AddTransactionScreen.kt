package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Transaction
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import dev.ktown.cardspendtracker.ui.viewmodel.TransactionViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.TransactionViewModelFactory
import java.util.*
import java.util.Calendar

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
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = transactionDate.time
            )
            
            if (showDatePicker) {
                AlertDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    // Convert UTC milliseconds to local date at midnight
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = millis
                                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                                    calendar.set(Calendar.MINUTE, 0)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    transactionDate = calendar.time
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    },
                    text = {
                        DatePicker(state = datePickerState)
                    }
                )
            }
            
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionDate)}"
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
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
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text(if (isEditing) "Update Transaction" else "Add Transaction")
            }
        }
    }
}
