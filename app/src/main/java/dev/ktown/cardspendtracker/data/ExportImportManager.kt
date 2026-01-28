package dev.ktown.cardspendtracker.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportImportManager(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun exportData(repository: CardRepository): String = withContext(Dispatchers.IO) {
        // Get all cards
        val cards = repository.getAllCardsSync()
        
        // Create card uniqueId to card mapping
        val cardUniqueIdToCard = cards.associateBy { it.uniqueId }
        
        // Get all goals
        val goals = cards.flatMap { card ->
            repository.getGoalsForCardSync(card.id).map { goal ->
                GoalExport(
                    cardName = card.name,
                    title = goal.title,
                    spendLimit = goal.spendLimit,
                    endDate = goal.endDate?.time,
                    comment = goal.comment,
                    createdAt = goal.createdAt.time
                )
            }
        }
        
        // Get all transactions
        val transactions = cards.flatMap { card ->
            repository.getTransactionsForCardSync(card.id).map { transaction ->
                TransactionExport(
                    uniqueId = transaction.uniqueId,
                    cardUniqueId = card.uniqueId,
                    amount = transaction.amount,
                    description = transaction.description,
                    date = transaction.date.time
                )
            }
        }
        
        // Create export data
        val exportData = ExportData(
            cards = cards.map { card ->
                CardExport(
                    uniqueId = card.uniqueId,
                    name = card.name,
                    color = card.color,
                    createdAt = card.createdAt.time
                )
            },
            goals = goals,
            transactions = transactions
        )
        
        // Convert to JSON
        gson.toJson(exportData)
    }

    suspend fun importData(json: String, repository: CardRepository): ImportResult = withContext(Dispatchers.IO) {
        try {
            val exportData = gson.fromJson(json, ExportData::class.java)
            
            val importedCards = mutableListOf<String>()
            val importedGoals = mutableListOf<String>()
            val importedTransactions = mutableListOf<String>()
            val errors = mutableListOf<String>()
            
            // Import cards
            val cardUniqueIdToId = mutableMapOf<String, Long>()
            for (cardExport in exportData.cards) {
                try {
                    // Check if card already exists by uniqueId
                    val existingCard = repository.getCardByUniqueId(cardExport.uniqueId)
                    if (existingCard != null) {
                        // Update existing card (preserve uniqueId)
                        val updatedCard = existingCard.copy(
                            name = cardExport.name,
                            color = cardExport.color
                        )
                        repository.updateCard(updatedCard)
                        cardUniqueIdToId[cardExport.uniqueId] = existingCard.id
                        importedCards.add(cardExport.name)
                    } else {
                        // Create new card with the exported uniqueId
                        val newCard = Card(
                            uniqueId = cardExport.uniqueId,
                            name = cardExport.name,
                            color = cardExport.color,
                            createdAt = Date(cardExport.createdAt)
                        )
                        val cardId = repository.insertCard(newCard)
                        cardUniqueIdToId[cardExport.uniqueId] = cardId
                        importedCards.add(cardExport.name)
                    }
                } catch (e: Exception) {
                    errors.add("Failed to import card '${cardExport.name}': ${e.message}")
                }
            }
            
            // Import goals
            // Goals still reference cards by name for backward compatibility
            val allCards = repository.getAllCardsSync()
            val cardNameToId = allCards.associateBy({ it.name }, { it.id })
            for (goalExport in exportData.goals) {
                try {
                    val cardId = cardNameToId[goalExport.cardName]
                    if (cardId == null) {
                        errors.add("Goal '${goalExport.title}' references unknown card '${goalExport.cardName}'")
                        continue
                    }
                    
                    val goal = Goal(
                        cardId = cardId,
                        title = goalExport.title,
                        spendLimit = goalExport.spendLimit,
                        endDate = goalExport.endDate?.let { Date(it) },
                        comment = goalExport.comment,
                        createdAt = Date(goalExport.createdAt)
                    )
                    repository.insertGoal(goal)
                    importedGoals.add(goalExport.title)
                } catch (e: Exception) {
                    errors.add("Failed to import goal '${goalExport.title}': ${e.message}")
                }
            }
            
            // Import transactions
            for (transactionExport in exportData.transactions) {
                try {
                    val cardId = cardUniqueIdToId[transactionExport.cardUniqueId]
                    if (cardId == null) {
                        errors.add("Transaction references unknown card (uniqueId: ${transactionExport.cardUniqueId})")
                        continue
                    }
                    
                    // Check if transaction already exists by uniqueId
                    val existingTransaction = repository.getTransactionByUniqueId(transactionExport.uniqueId)
                    if (existingTransaction != null) {
                        // Skip duplicate transaction
                        continue
                    }
                    
                    val transaction = Transaction(
                        uniqueId = transactionExport.uniqueId,
                        cardId = cardId,
                        amount = transactionExport.amount,
                        description = transactionExport.description,
                        date = Date(transactionExport.date)
                    )
                    repository.insertTransaction(transaction)
                    importedTransactions.add("${transactionExport.amount}")
                } catch (e: Exception) {
                    errors.add("Failed to import transaction: ${e.message}")
                }
            }
            
            ImportResult(
                success = errors.isEmpty(),
                cardsImported = importedCards.size,
                goalsImported = importedGoals.size,
                transactionsImported = importedTransactions.size,
                errors = errors
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                cardsImported = 0,
                goalsImported = 0,
                transactionsImported = 0,
                errors = listOf("Failed to parse JSON: ${e.message}")
            )
        }
    }

    suspend fun readFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun writeToUri(uri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.write(content)
                }
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return "card_spend_tracker_${dateFormat.format(Date())}.json"
    }
}

data class ImportResult(
    val success: Boolean,
    val cardsImported: Int,
    val goalsImported: Int,
    val transactionsImported: Int,
    val errors: List<String>
)

// Extension functions to get sync versions of Flow-based methods
suspend fun CardRepository.getAllCardsSync(): List<Card> {
    return this.getAllCards().first()
}

suspend fun CardRepository.getGoalsForCardSync(cardId: Long): List<Goal> {
    return this.getGoalsForCard(cardId).first()
}

suspend fun CardRepository.getTransactionsForCardSync(cardId: Long): List<Transaction> {
    return this.getTransactionsForCard(cardId).first()
}
