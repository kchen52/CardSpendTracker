package dev.ktown.cardspendtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["uniqueId"], unique = true)
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uniqueId: String = UUID.randomUUID().toString(),
    val cardId: Long,
    val amount: Double,
    val description: String = "",
    val date: Date = Date()
)
