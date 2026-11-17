package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.assertDoesNotThrow
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper.Companion.sendNotificationAndLogFailures
import kotlin.test.Test
import kotlin.test.assertTrue

class NotificationHelperTests {
    @Test
    fun `sendNotificationAndLogFailures sends the notification`() {
        var sent = false

        sendNotificationAndLogFailures("a test notification") { sent = true }

        assertTrue(sent)
    }

    @Test
    fun `sendNotificationAndLogFailures does not throw when sending fails`() {
        assertDoesNotThrow {
            sendNotificationAndLogFailures("a test notification") { throw RuntimeException("Notification failed to send") }
        }
    }
}
