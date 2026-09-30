package com.moneo.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.data.local.entity.TransactionEntity
import com.moneo.app.data.local.prefs.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val appLockEnabled = preferencesManager.appLockEnabled

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAppLockEnabled(enabled)
        }
    }

    suspend fun exportToCsv(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionDao.getAllTransactions().first()
            val file = File(context.cacheDir, "Moneo_Backup_${System.currentTimeMillis()}.csv")
            FileWriter(file).use { writer ->
                writer.append("id,amountInPaise,currency,type,category,merchant,description,date,timestamp,paymentMethod,person,notes,source,confidence,createdAt,updatedAt\n")
                for (t in transactions) {
                    writer.append("${t.id},")
                    writer.append("${t.amountInPaise},")
                    writer.append("${escapeCsv(t.currency)},")
                    writer.append("${escapeCsv(t.type)},")
                    writer.append("${escapeCsv(t.category)},")
                    writer.append("${escapeCsv(t.merchant)},")
                    writer.append("${escapeCsv(t.description)},")
                    writer.append("${escapeCsv(t.date)},")
                    writer.append("${escapeCsv(t.timestamp)},")
                    writer.append("${escapeCsv(t.paymentMethod)},")
                    writer.append("${escapeCsv(t.person)},")
                    writer.append("${escapeCsv(t.notes)},")
                    writer.append("${escapeCsv(t.source)},")
                    writer.append("${t.confidence},")
                    writer.append("${escapeCsv(t.createdAt)},")
                    writer.append("${escapeCsv(t.updatedAt)}\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String?): String {
        if (value == null) return ""
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    fun importFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).readLines().drop(1).forEach { line ->
                        if (line.isNotBlank()) {
                            val parts = parseCsvLine(line)
                            if (parts.size >= 16) {
                                val entity = TransactionEntity(
                                    id = parts[0].toLongOrNull() ?: 0L,
                                    amountInPaise = parts[1].toLongOrNull() ?: 0L,
                                    currency = parts[2],
                                    type = parts[3],
                                    category = parts[4],
                                    merchant = parts[5].takeIf { it.isNotEmpty() },
                                    description = parts[6].takeIf { it.isNotEmpty() },
                                    date = parts[7],
                                    timestamp = parts[8],
                                    paymentMethod = parts[9].takeIf { it.isNotEmpty() },
                                    person = parts[10].takeIf { it.isNotEmpty() },
                                    notes = parts[11].takeIf { it.isNotEmpty() },
                                    source = parts[12],
                                    confidence = parts[13].toFloatOrNull() ?: 1.0f,
                                    createdAt = parts[14],
                                    updatedAt = parts[15],
                                    account = null,
                                    toAccount = null,
                                    isRecurring = false,
                                    billingCycle = null
                                )
                                transactionDao.insertTransaction(entity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val char = line[i]
            if (inQuotes) {
                if (char == '\"') {
                    if (i < line.length - 1 && line[i + 1] == '\"') {
                        current.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    current.append(char)
                }
            } else {
                if (char == '\"') {
                    inQuotes = true
                } else if (char == ',') {
                    result.add(current.toString())
                    current = StringBuilder()
                } else {
                    current.append(char)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
