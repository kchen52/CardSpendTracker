package dev.ktown.cardspendtracker.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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

private data class TransactionDraftUi(
    val id: Long,
    val amount: String = "",
    val description: String = "",
    val date: Date = Date()
)

/**
 * Filters input to valid dollar amount characters only: digits and at most one decimal with at most 2 decimal places.
 * Returns the filtered string to apply, or null to reject the change.
 */
internal fun filterDollarAmountInput(newText: String): String? {
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
internal fun isValidDollarAmount(amount: String): Boolean {
    if (amount.isBlank()) return false
    val value = amount.toDoubleOrNull() ?: return false
    return value > 0
}

private fun TransactionDraftUi.isValid(): Boolean {
    return description.isNotBlank() && isValidDollarAmount(amount)
}

private fun Modifier.handleTabNavigation(
    onTabPressed: () -> Unit
): Modifier = this.onPreviewKeyEvent { event ->
    if (
        event.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN &&
        event.nativeKeyEvent.keyCode == AndroidKeyEvent.KEYCODE_TAB
    ) {
        onTabPressed()
        true
    } else {
        false
    }
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val saveButtonFocusRequester = remember { FocusRequester() }
    val addRowButtonFocusRequester = remember { FocusRequester() }
    val drafts = remember { mutableStateListOf(TransactionDraftUi(id = 1L)) }
    var nextDraftId by remember { mutableLongStateOf(2L) }
    var selectedDraftIdForDatePicker by remember { mutableStateOf<Long?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }

    var cardName by remember { mutableStateOf("") }
    var existingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val isEditing = transactionId != null

    LaunchedEffect(cardId, transactionId) {
        val card = cardViewModel.getCardById(cardId)
        card?.let { cardName = it.name }

        transactionId?.let { id ->
            val transaction = transactionViewModel.getTransactionById(id)
            transaction?.let {
                existingTransaction = it
                drafts.clear()
                drafts.add(
                    TransactionDraftUi(
                        id = 1L,
                        amount = it.amount.toString(),
                        description = it.description,
                        date = it.date
                    )
                )
            }
        }
    }

    val selectedDraft = selectedDraftIdForDatePicker?.let { selectedId ->
        drafts.firstOrNull { it.id == selectedId }
    }
    if (selectedDraft != null) {
        CalendarDatePickerDialog(
            selectedDate = selectedDraft.date,
            onDateSelected = { date ->
                val index = drafts.indexOfFirst { it.id == selectedDraft.id }
                if (index >= 0) {
                    drafts[index] = drafts[index].copy(date = date)
                }
            },
            onDismiss = { selectedDraftIdForDatePicker = null }
        )
    }

    val canSubmit = if (isEditing) {
        drafts.firstOrNull()?.isValid() == true
    } else {
        drafts.isNotEmpty()
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
                .verticalScroll(rememberScrollState())
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

            drafts.forEachIndexed { index, draft ->
                TransactionDraftEditor(
                    draft = draft,
                    isEditing = isEditing,
                    isFirstDraft = index == 0,
                    canRemove = !isEditing && drafts.size > 1,
                    showValidationErrors = showValidationErrors,
                    onRemove = {
                        drafts.removeAll { it.id == draft.id }
                    },
                    onDescriptionChange = { newValue ->
                        val updatedValue = newValue.replaceFirstChar { c ->
                            if (c.isLetter()) c.uppercaseChar() else c
                        }
                        val draftIndex = drafts.indexOfFirst { it.id == draft.id }
                        if (draftIndex >= 0) {
                            drafts[draftIndex] = drafts[draftIndex].copy(description = updatedValue)
                        }
                    },
                    onAmountChange = { newValue ->
                        filterDollarAmountInput(newValue)?.let { filteredValue ->
                            val draftIndex = drafts.indexOfFirst { it.id == draft.id }
                            if (draftIndex >= 0) {
                                drafts[draftIndex] = drafts[draftIndex].copy(amount = filteredValue)
                            }
                        }
                    },
                    onDateClick = {
                        selectedDraftIdForDatePicker = draft.id
                    },
                    onMoveToNextDraft = {
                        if (index < drafts.lastIndex) {
                            focusManager.moveFocus(FocusDirection.Next)
                        } else if (isEditing) {
                            saveButtonFocusRequester.requestFocus()
                        } else {
                            addRowButtonFocusRequester.requestFocus()
                        }
                    },
                    onRequestInitialFocus = {
                        if (!isEditing) {
                            keyboardController?.show()
                        }
                    }
                )
            }

            if (!isEditing) {
                Button(
                    onClick = {
                        drafts.add(TransactionDraftUi(id = nextDraftId++))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(addRowButtonFocusRequester)
                        .handleTabNavigation {
                            saveButtonFocusRequester.requestFocus()
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add another transaction row"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Another Transaction")
                }
            }

            CtaButton(
                onClick = {
                    showValidationErrors = true
                    val allValid = drafts.all { it.isValid() }
                    if (!allValid) {
                        return@CtaButton
                    }

                    if (isEditing && existingTransaction != null) {
                        val draft = drafts.first()
                        val transaction = existingTransaction!!.copy(
                            amount = draft.amount.toDouble(),
                            description = draft.description.trim(),
                            date = draft.date
                        )
                        transactionViewModel.updateTransaction(transaction)
                    } else {
                        val transactions = drafts.map { draft ->
                            Transaction(
                                cardId = cardId,
                                amount = draft.amount.toDouble(),
                                description = draft.description.trim(),
                                date = draft.date
                            )
                        }
                        transactionViewModel.addTransactions(transactions)
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(saveButtonFocusRequester)
                    .handleTabNavigation {
                        focusManager.moveFocus(FocusDirection.Next)
                    },
                enabled = canSubmit
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else "Add Transactions",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TransactionDraftEditor(
    draft: TransactionDraftUi,
    isEditing: Boolean,
    isFirstDraft: Boolean,
    canRemove: Boolean,
    showValidationErrors: Boolean,
    onRemove: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onMoveToNextDraft: () -> Unit,
    onRequestInitialFocus: () -> Unit
) {
    val descriptionFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val dateFocusRequester = remember { FocusRequester() }
    var autofocusDone by remember(draft.id) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val dateLabel = remember(draft.date) {
        java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(draft.date)
    }
    val hasDescriptionError = showValidationErrors && draft.description.isBlank()
    val hasAmountError = showValidationErrors && !isValidDollarAmount(draft.amount)

    LaunchedEffect(isFirstDraft, isEditing, autofocusDone) {
        if (isFirstDraft && !isEditing && !autofocusDone) {
            descriptionFocusRequester.requestFocus()
            onRequestInitialFocus()
            autofocusDone = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Transaction" else "Transaction ${draft.id}",
                style = MaterialTheme.typography.titleMedium
            )
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove transaction row"
                    )
                }
            }
        }

        OutlinedTextField(
            value = draft.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(descriptionFocusRequester)
                .focusProperties {
                    next = amountFocusRequester
                }
                .handleTabNavigation {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            singleLine = true,
            isError = hasDescriptionError,
            supportingText = {
                if (hasDescriptionError) {
                    Text("Description is required.")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { amountFocusRequester.requestFocus() }
            )
        )

        OutlinedTextField(
            value = draft.amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(amountFocusRequester)
                .focusProperties {
                    next = dateFocusRequester
                }
                .handleTabNavigation {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            singleLine = true,
            prefix = { Text("$") },
            isError = hasAmountError,
            supportingText = {
                if (hasAmountError) {
                    Text("Enter an amount greater than 0.")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { dateFocusRequester.requestFocus() }
            )
        )

        Button(
            onClick = onDateClick,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(dateFocusRequester)
                .handleTabNavigation {
                    onMoveToNextDraft()
                }
        ) {
            Text("Date: $dateLabel")
        }
    }
}
