package dev.ktown.cardspendtracker.data

import java.util.Date

data class ExportData(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val cards: List<CardExport>,
    val goals: List<GoalExport>,
    val transactions: List<TransactionExport>
)

data class CardExport(
    val uniqueId: String,
    val name: String,
    val color: Long,
    val createdAt: Long
)

data class GoalExport(
    val cardName: String, // Reference by name instead of ID
    val title: String,
    val spendLimit: Double,
    val endDate: Long?,
    val comment: String,
    val createdAt: Long
)

data class TransactionExport(
    val uniqueId: String,
    val cardUniqueId: String, // Reference by uniqueId instead of name
    val amount: Double,
    val description: String,
    val date: Long
)
