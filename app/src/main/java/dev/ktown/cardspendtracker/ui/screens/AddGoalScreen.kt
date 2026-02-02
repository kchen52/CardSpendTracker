package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.Goal
import dev.ktown.cardspendtracker.ui.CalendarDatePickerDialog
import dev.ktown.cardspendtracker.ui.theme.CtaButton
import dev.ktown.cardspendtracker.ui.viewmodel.GoalViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.GoalViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    cardId: Long,
    goalId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(
            CardRepository(
                AppDatabase.getDatabase(LocalContext.current).cardDao(),
                AppDatabase.getDatabase(LocalContext.current).goalDao(),
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )
) {
    val isEditMode = goalId != null

    var title by remember { mutableStateOf("") }
    var spendLimit by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var hasEndDate by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        goalId?.let { id ->
            val goal = viewModel.getGoalById(id)
            goal?.let {
                title = it.title
                spendLimit = it.spendLimit.toString()
                comment = it.comment
                hasEndDate = it.endDate != null
                endDate = it.endDate
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Goal" else "Add Goal") },
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = spendLimit,
                onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) spendLimit = it },
                label = { Text("Spend Limit") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (Optional)") },
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
                if (showDatePicker) {
                    CalendarDatePickerDialog(
                        selectedDate = endDate ?: Date(),
                        onDateSelected = { endDate = it },
                        onDismiss = { showDatePicker = false }
                    )
                }

                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = endDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                        } ?: "Select End Date"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CtaButton(
                onClick = {
                    val limit = spendLimit.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && limit > 0) {
                        val goal = Goal(
                            id = goalId ?: 0,
                            cardId = cardId,
                            title = title.trim(),
                            spendLimit = limit,
                            endDate = endDate,
                            comment = comment.trim()
                        )
                        if (isEditMode) viewModel.updateGoal(goal) else viewModel.addGoal(goal)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && (spendLimit.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = if (isEditMode) "Update Goal" else "Add Goal",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

