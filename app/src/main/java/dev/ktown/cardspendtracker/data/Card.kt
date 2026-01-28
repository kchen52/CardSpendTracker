package dev.ktown.cardspendtracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "cards",
    indices = [Index(value = ["uniqueId"], unique = true)]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uniqueId: String = UUID.randomUUID().toString(),
    val name: String,
    // Stored as Compose Color packed long (ARGB in the top bytes for sRGB).
    // Default corresponds to Material purple 500.
    val color: Long = 0xFF6200EE00000000UL.toLong(),
    val createdAt: Date = Date()
)
