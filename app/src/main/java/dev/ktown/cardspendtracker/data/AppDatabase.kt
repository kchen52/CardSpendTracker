package dev.ktown.cardspendtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Card::class, Goal::class, Transaction::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun goalDao(): GoalDao
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        /**
         * v1 -> v2
         * - Introduce `goals` table
         * - Convert old Card.spendLimit/endDate into an initial Goal per Card
         * - Drop legacy columns by recreating the `cards` table
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cardId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `spendLimit` REAL NOT NULL,
                        `endDate` INTEGER,
                        `comment` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`cardId`) REFERENCES `cards`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_cardId` ON `goals` (`cardId`)")

                // Create new cards table without legacy columns.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cards_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Copy existing cards over.
                db.execSQL(
                    """
                    INSERT INTO `cards_new` (`id`, `name`, `createdAt`)
                    SELECT `id`, `name`, `createdAt`
                    FROM `cards`
                    """.trimIndent()
                )

                // Create an initial goal per card from the legacy columns.
                // These columns existed in v1: spendLimit (REAL), endDate (INTEGER nullable).
                db.execSQL(
                    """
                    INSERT INTO `goals` (`cardId`, `title`, `spendLimit`, `endDate`, `comment`, `createdAt`)
                    SELECT `id`, 'Initial Goal', `spendLimit`, `endDate`, '', `createdAt`
                    FROM `cards`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `cards`")
                db.execSQL("ALTER TABLE `cards_new` RENAME TO `cards`")
            }
        }

        /**
         * v2 -> v3
         * - Add `color` to cards with a default.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Default corresponds to Material purple 500 in our packed-long format.
                db.execSQL("ALTER TABLE `cards` ADD COLUMN `color` INTEGER NOT NULL DEFAULT -44472024118067200")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "card_spend_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
