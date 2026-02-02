package dev.ktown.cardspendtracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.ktown.cardspendtracker.data.AppDatabase
import dev.ktown.cardspendtracker.data.CardRepository
import dev.ktown.cardspendtracker.data.ExportImportManager

class DailyExportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = CardRepository(
                database.cardDao(),
                database.goalDao(),
                database.transactionDao()
            )
            val exportManager = ExportImportManager(applicationContext)
            val file = exportManager.exportToAppStorage(repository)
            if (file != null) Result.success() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
