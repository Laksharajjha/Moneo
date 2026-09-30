package com.moneo.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.moneo.app.data.local.prefs.PreferencesManager
import com.moneo.app.ai.parser.TransactionParser
import com.moneo.app.data.local.dao.InboxEventDao
import com.moneo.app.data.local.entity.InboxEventEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class MoneoNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var transactionParser: TransactionParser

    @Inject
    lateinit var inboxEventDao: InboxEventDao

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        scope.launch {
            // Check if notification access is enabled in Moneo settings
            val isEnabled = preferencesManager.notificationListenerEnabled.first()
            if (!isEnabled) return@launch

            val packageName = sbn.packageName
            val monitoredApps = preferencesManager.monitoredApps.first()

            if (monitoredApps.contains(packageName)) {
                val extras = sbn.notification.extras
                val title = extras.getString("android.title") ?: ""
                val text = extras.getCharSequence("android.text")?.toString() ?: ""
                val combinedText = "$title $text"
                
                if (combinedText.isNotBlank()) {
                    val parseResult = transactionParser.parse(combinedText)
                    if (parseResult.isSuccess && parseResult.transactions.isNotEmpty()) {
                        val tx = parseResult.transactions.first() // Take the most confident one
                        
                        val entity = InboxEventEntity(
                            amountInPaise = tx.amountInPaise,
                            currency = tx.currency,
                            type = tx.type.name,
                            category = tx.category.name,
                            merchant = tx.merchant,
                            date = tx.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            sourcePackage = packageName,
                            confidence = parseResult.confidence,
                            rawText = combinedText,
                            timestamp = sbn.postTime
                        )
                        
                        // Deduplication: Don't insert if we already saw the same rawText or amount+merchant within the last hour
                        val recentEvents = inboxEventDao.getAllEvents().first().filter { 
                            it.timestamp > (sbn.postTime - 3600_000) 
                        }
                        
                        val isDuplicate = recentEvents.any { 
                            it.rawText == combinedText || 
                            (it.amountInPaise == tx.amountInPaise && it.merchant == tx.merchant && it.type == tx.type.name)
                        }

                        if (!isDuplicate) {
                            inboxEventDao.insertEvent(entity)
                            Log.d("MoneoNotification", "Parsed and stored financial event from $packageName")
                        } else {
                            Log.d("MoneoNotification", "Ignored duplicate financial event from $packageName")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
