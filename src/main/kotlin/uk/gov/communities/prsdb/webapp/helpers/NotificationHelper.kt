package uk.gov.communities.prsdb.webapp.helpers

class NotificationHelper {
    companion object {
        fun sendNotificationAndLogFailures(
            notificationDescription: String,
            sendNotification: () -> Unit,
        ) {
            try {
                sendNotification()
            } catch (exception: Exception) {
                println("Failed to send $notificationDescription: ${exception.stackTraceToString()}")
            }
        }
    }
}
