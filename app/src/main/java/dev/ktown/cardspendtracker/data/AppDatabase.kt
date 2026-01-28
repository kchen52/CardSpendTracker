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
    version = 4,
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

        /**
         * v3 -> v4
         * - Add `uniqueId` columns to cards and transactions for deduplication during import
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add uniqueId to cards
                db.execSQL("ALTER TABLE `cards` ADD COLUMN `uniqueId` TEXT")
                // Generate unique IDs for existing cards (32-char hex string)
                db.execSQL("""
                    UPDATE `cards` 
                    SET `uniqueId` = lower(hex(randomblob(16)))
                    WHERE `uniqueId` IS NULL
                """.trimIndent())
                // Make it NOT NULL after populating
                db.execSQL("""
                    CREATE TABLE `cards_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `uniqueId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `cards_new` (`id`, `uniqueId`, `name`, `color`, `createdAt`)
                    SELECT `id`, `uniqueId`, `name`, `color`, `createdAt`
                    FROM `cards`
                """.trimIndent())
                db.execSQL("DROP TABLE `cards`")
                db.execSQL("ALTER TABLE `cards_new` RENAME TO `cards`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_cards_uniqueId` ON `cards` (`uniqueId`)")

                // Add uniqueId to transactions
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `uniqueId` TEXT")
                // Generate unique IDs for existing transactions (32-char hex string)
                db.execSQL("""
                    UPDATE `transactions` 
                    SET `uniqueId` = lower(hex(randomblob(16)))
                    WHERE `uniqueId` IS NULL
                """.trimIndent())
                // Make it NOT NULL after populating
                db.execSQL("""
                    CREATE TABLE `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `uniqueId` TEXT NOT NULL,
                        `cardId` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        FOREIGN KEY(`cardId`) REFERENCES `cards`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `transactions_new` (`id`, `uniqueId`, `cardId`, `amount`, `description`, `date`)
                    SELECT `id`, `uniqueId`, `cardId`, `amount`, `description`, `date`
                    FROM `transactions`
                """.trimIndent())
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_cardId` ON `transactions` (`cardId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_uniqueId` ON `transactions` (`uniqueId`)")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
