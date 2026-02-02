package dev.ktown.cardspendtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import android.content.Context
import android.content.SharedPreferences
import dev.ktown.cardspendtracker.ui.theme.CtaButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.ExportImportManager
import dev.ktown.cardspendtracker.worker.DailyExportScheduler
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModel
import dev.ktown.cardspendtracker.ui.viewmodel.CardViewModelFactory
import dev.ktown.cardspendtracker.ui.viewmodel.CardWithGoalsProgress
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private const val KEY_AUTO_DAILY_EXPORT = "auto_daily_export"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCards: () -> Unit,
    onNavigateToAddCard: () -> Unit,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportImportManager = remember { ExportImportManager(context) }
    val repository = remember {
        CardRepository(
            AppDatabase.getDatabase(context).cardDao(),
            AppDatabase.getDatabase(context).goalDao(),
            AppDatabase.getDatabase(context).transactionDao()
        )
    }

    // Persist expand/collapse state
    val prefs = remember {
        context.getSharedPreferences("card_expand_state", Context.MODE_PRIVATE)
    }
    val appSettingsPrefs = remember {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }
    var autoDailyExportEnabled by remember {
        mutableStateOf(appSettingsPrefs.getBoolean(KEY_AUTO_DAILY_EXPORT, false))
    }
    var showSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(autoDailyExportEnabled) {
        if (autoDailyExportEnabled) {
            DailyExportScheduler.schedule(context)
        } else {
            DailyExportScheduler.cancel(context)
        }
        appSettingsPrefs.edit().putBoolean(KEY_AUTO_DAILY_EXPORT, autoDailyExportEnabled).apply()
    }
    var collapsedCardIds by remember {
        mutableStateOf<Set<Long>>(emptySet())
    }

    LaunchedEffect(Unit) {
        val storedIds = prefs.getStringSet("collapsed_ids", emptySet()) ?: emptySet()
        collapsedCardIds = storedIds.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun updateCollapsedState(cardId: Long, isCollapsed: Boolean) {
        val newSet = if (isCollapsed) {
            collapsedCardIds + cardId
        } else {
            collapsedCardIds - cardId
        }
        collapsedCardIds = newSet
        prefs.edit()
            .putStringSet("collapsed_ids", newSet.map { it.toString() }.toSet())
            .apply()
    }

    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importResult by remember {
        mutableStateOf<dev.ktown.cardspendtracker.data.ImportResult?>(
            null
        )
    }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isImporting = true
                val json = exportImportManager.readFromUri(it)
                if (json != null) {
                    importResult = exportImportManager.importData(json, repository)
                    showImportDialog = true
                } else {
                    importResult = dev.ktown.cardspendtracker.data.ImportResult(
                        success = false,
                        cardsImported = 0,
                        goalsImported = 0,
                        transactionsImported = 0,
                        errors = listOf("Failed to read file")
                    )
                    showImportDialog = true
                }
                isImporting = false
            }
        }
    }

    // Create document launcher for export
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isExporting = true
                val json = exportImportManager.exportData(repository)
                val success = exportImportManager.writeToUri(it, json)
                if (success) {
                    // Share the file using ShareSheet (includes Google Drive option)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, it)
                        putExtra(Intent.EXTRA_SUBJECT, "Card Spend Tracker Backup")
                        putExtra(Intent.EXTRA_TEXT, "Card Spend Tracker backup file")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        val chooser = Intent.createChooser(shareIntent, "Share backup file")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    } catch (e: Exception) {
                        // If sharing fails, just show success dialog
                    }
                    showExportDialog = true
                }
                isExporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Spend Tracker") },
                actions = {
                    IconButton(
                        onClick = {
                            val fileName = exportImportManager.generateFileName()
                            createDocumentLauncher.launch(fileName)
                        },
                        enabled = !isExporting && !isImporting
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Export Data")
                    }
                    IconButton(
                        onClick = { importLauncher.launch("application/json") },
                        enabled = !isImporting
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Import Data")
                    }
                    IconButton(onClick = onNavigateToCards) {
                        Icon(Icons.Default.CreditCard, contentDescription = "View Cards")
                    }
                    IconButton(onClick = onNavigateToAddCard) {
                        Icon(Icons.Default.Add, contentDescription = "Add Card")
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Auto daily export",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = autoDailyExportEnabled,
                                    onCheckedChange = { autoDailyExportEnabled = it }
                                )
                            }
                        }
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                        CtaButton(onClick = onNavigateToAddCard, text = "Add Card")
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
                    val cardId = cardWithProgress.card.id
                    val isExpanded = cardId !in collapsedCardIds
                    
                    CardProgressItem(
                        cardWithProgress = cardWithProgress,
                        isExpanded = isExpanded,
                        onToggleExpanded = {
                            updateCollapsedState(cardId, isCollapsed = isExpanded)
                        },
                        onAddTransaction = { onNavigateToAddTransaction(cardId) },
                        onViewTransactions = { onNavigateToTransactions(cardId) },
                        onManageGoals = { onNavigateToGoals(cardId) }
                    )
                }
                }

                // Loading indicators overlay
                if (isExporting || isImporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Import result dialog
        if (showImportDialog && importResult != null) {
            val result = importResult!!
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text(if (result.success) "Import Successful" else "Import Completed with Errors") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cards imported: ${result.cardsImported}")
                        Text("Goals imported: ${result.goalsImported}")
                        Text("Transactions imported: ${result.transactionsImported}")
                        if (result.errors.isNotEmpty()) {
                            Text(
                                text = "Errors:\n${result.errors.joinToString("\n")}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Export success dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Successful") },
                text = { Text("Your data has been exported successfully. You can now share it to Google Drive or save it elsewhere.") },
                confirmButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun CardProgressItem(
    cardWithProgress: CardWithGoalsProgress,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAddTransaction: () -> Unit,
    onViewTransactions: () -> Unit,
    onManageGoals: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val cardColor = Color(cardWithProgress.card.color.toULong())
    val allGoalsHit = cardWithProgress.goals.isNotEmpty() && cardWithProgress.goals.all { it.progress >= 1f }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Color accent bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(cardColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Header(
                    cardWithProgress = cardWithProgress,
                    allGoalsHit = allGoalsHit,
                    isExpanded = isExpanded,
                    onToggleExpanded = onToggleExpanded,
                    onAddTransaction = onAddTransaction,
                    onViewTransactions = onViewTransactions,
                    onManageGoals = onManageGoals
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    text = "Goals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cardWithProgress.goals.size.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (cardWithProgress.goals.isEmpty()) {
                            Text(
                                text = "No goals yet — add one to start tracking limits.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                cardWithProgress.goals.forEach { goalWithProgress ->
                                    val percent = (goalWithProgress.progress * 100f).roundToInt()
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (goalWithProgress.progress >= 1f) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Goal completed",
                                                        tint = Color(0xFF2E7D32) // green
                                                    )
                                                }
                                                Text(
                                                    text = goalWithProgress.goal.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${percent}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Limit: ${currencyFormat.format(goalWithProgress.goal.spendLimit)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        LinearProgressIndicator(
                                            progress = { goalWithProgress.progress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = when {
                                                goalWithProgress.progress >= 1f -> MaterialTheme.colorScheme.error
                                                goalWithProgress.progress >= 0.8f -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Remaining: ${currencyFormat.format(goalWithProgress.remaining)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            goalWithProgress.daysRemaining?.let { days ->
                                                Text(
                                                    text = if (days > 0) "$days days remaining" else "Expired",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (days > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        if (goalWithProgress.goal.comment.isNotBlank()) {
                                            Text(
                                                text = goalWithProgress.goal.comment,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        goalWithProgress.goal.endDate?.let { date ->
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
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    cardWithProgress: CardWithGoalsProgress,
    allGoalsHit: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAddTransaction: () -> Unit,
    onViewTransactions: () -> Unit,
    onManageGoals: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { onToggleExpanded() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (allGoalsHit) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "All goals completed",
                    tint = Color(0xFF2E7D32) // green
                )
            }
            Text(
                text = cardWithProgress.card.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(horizontal = 12.dp),
    ) {
        TextButton(onClick = { onAddTransaction() }) {
            Text("Add")
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = { onViewTransactions() }) {
            Text("View")
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = { onManageGoals() }) {
            Text("Goals")
        }
    }
}
