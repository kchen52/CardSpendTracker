package dev.ktown.cardspendtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val spendLimit: Double,
    val endDate: Date? = null,
    val createdAt: Date = Date()
)
