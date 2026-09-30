package com.moneo.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.moneo.app.data.local.prefs.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoneoNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

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
                
                // TODO: Wire to AI parser and InboxEvent database in Step 7
                Log.d("MoneoNotification", "Detected financial notification from $packageName")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
