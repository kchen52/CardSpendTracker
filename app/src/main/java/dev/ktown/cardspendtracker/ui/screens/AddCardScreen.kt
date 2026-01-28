package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.Card
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    cardId: Long? = null,
    onNavigateBack: () -> Unit,
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
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) } // Default Material purple
    var showColorPicker by remember { mutableStateOf(false) }
    // Initial goal fields (used only when creating a new card)
    var goalTitle by remember { mutableStateOf("") }
    var spendLimit by remember { mutableStateOf("") }
    var goalComment by remember { mutableStateOf("") }
    var hasEndDate by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var existingCard by remember { mutableStateOf<Card?>(null) }
    
    val isEditMode = cardId != null
    
    LaunchedEffect(cardId) {
        if (cardId != null) {
            val card = viewModel.getCardById(cardId)
            card?.let {
                existingCard = it
                name = it.name
                selectedColor = Color(it.color)
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
                .imePadding()
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

            // Color picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Card Color",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selected color preview
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(selectedColor)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { showColorPicker = true }
                    )
                    
                    // Color palette
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color(0xFF6200EE), // Purple
                            Color(0xFF03DAC6), // Teal
                            Color(0xFF018786), // Dark Teal
                            Color(0xFF3700B3), // Dark Purple
                            Color(0xFF03DAC5), // Cyan
                            Color(0xFF000000), // Black
                            Color(0xFF6200EA), // Deep Purple
                            Color(0xFF1976D2), // Blue
                            Color(0xFF388E3C), // Green
                            Color(0xFFF57C00), // Orange
                            Color(0xFFD32F2F), // Red
                            Color(0xFF7B1FA2), // Purple 700
                        )
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (selectedColor == color) 2.dp else 1.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            }

            if (showColorPicker) {
                ColorPickerDialog(
                    currentColor = selectedColor,
                    onColorSelected = { color ->
                        selectedColor = color
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
                )
            }

            if (!isEditMode) {
                Text(
                    text = "Initial goal",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Goal Title") },
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

                OutlinedTextField(
                    value = goalComment,
                    onValueChange = { goalComment = it },
                    label = { Text("Goal Comment (Optional)") },
                    modifier = Modifier.fillMaxWidth()
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
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            // Convert UTC milliseconds to local date at midnight
                                            val calendar = Calendar.getInstance()
                                            calendar.timeInMillis = millis
                                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                                            calendar.set(Calendar.MINUTE, 0)
                                            calendar.set(Calendar.SECOND, 0)
                                            calendar.set(Calendar.MILLISECOND, 0)
                                            endDate = calendar.time
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
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (isEditMode) {
                        if (name.isNotBlank() && existingCard != null) {
                            viewModel.updateCard(
                                existingCard!!.copy(
                                    name = name,
                                    color = selectedColor.value.toLong()
                                )
                            )
                            onNavigateBack()
                        }
                    } else {
                        val limit = spendLimit.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && goalTitle.isNotBlank() && limit > 0) {
                            viewModel.addCardWithInitialGoal(
                                cardName = name.trim(),
                                cardColor = selectedColor.value.toLong(),
                                goalTitle = goalTitle.trim(),
                                spendLimit = limit,
                                endDate = endDate,
                                comment = goalComment.trim()
                            )
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (isEditMode) {
                    name.isNotBlank()
                } else {
                    name.isNotBlank() &&
                        goalTitle.isNotBlank() &&
                        spendLimit.toDoubleOrNull() != null &&
                        spendLimit.toDoubleOrNull()!! > 0
                }
            ) {
                Text(if (isEditMode) "Update Card" else "Add Card + Goal")
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Extended color palette
                val colorRows = listOf(
                    listOf(
                        Color(0xFF6200EE), Color(0xFF3700B3), Color(0xFF7B1FA2),
                        Color(0xFF1976D2), Color(0xFF0288D1), Color(0xFF0277BD)
                    ),
                    listOf(
                        Color(0xFF03DAC6), Color(0xFF018786), Color(0xFF00ACC1),
                        Color(0xFF388E3C), Color(0xFF2E7D32), Color(0xFF1B5E20)
                    ),
                    listOf(
                        Color(0xFFF57C00), Color(0xFFE65100), Color(0xFFD32F2F),
                        Color(0xFFC62828), Color(0xFFAD1457), Color(0xFF880E4F)
                    ),
                    listOf(
                        Color(0xFF000000), Color(0xFF424242), Color(0xFF616161),
                        Color(0xFF757575), Color(0xFF9E9E9E), Color(0xFFBDBDBD)
                    )
                )
                
                colorRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        if (currentColor == color) 3.dp else 1.dp,
                                        if (currentColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

