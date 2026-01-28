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
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    cardId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: CardViewModel = viewModel(
        factory = CardViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )
) {
    var name by remember { mutableStateOf("") }
    var spendLimit by remember { mutableStateOf("") }
    var hasEndDate by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isEditMode = cardId != null
    
    LaunchedEffect(cardId) {
        if (cardId != null) {
            val card = viewModel.getCardById(cardId)
            card?.let {
                name = it.name
                spendLimit = it.spendLimit.toString()
                hasEndDate = it.endDate != null
                endDate = it.endDate
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Card" else "Add Card") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Card Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = spendLimit,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) spendLimit = it },
                label = { Text("Spend Limit") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Set End Date")
                Switch(
                    checked = hasEndDate,
                    onCheckedChange = { 
                        hasEndDate = it
                        if (!it) endDate = null
                    }
                )
            }
            
            if (hasEndDate) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = endDate?.time
                )
                
                if (showDatePicker) {
                    AlertDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        endDate = Date(it)
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
                        text = endDate?.let { 
                            java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                        } ?: "Select End Date"
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val limit = spendLimit.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && limit > 0) {
                        val card = Card(
                            id = cardId ?: 0,
                            name = name,
                            spendLimit = limit,
                            endDate = endDate
                        )
                        if (isEditMode) {
                            viewModel.updateCard(card)
                        } else {
                            viewModel.addCard(card)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && spendLimit.toDoubleOrNull() != null && spendLimit.toDoubleOrNull()!! > 0
            ) {
                Text(if (isEditMode) "Update Card" else "Add Card")
            }
        }
    }
}

