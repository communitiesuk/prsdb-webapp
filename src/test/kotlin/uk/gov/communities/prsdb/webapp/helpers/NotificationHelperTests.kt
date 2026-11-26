package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.assertDoesNotThrow
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper.Companion.sendAddedToPropertyNotifications
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper.Companion.sendNotificationAndLogFailures
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun `sendAddedToPropertyNotifications sends to every party, continuing if a notification fails`() {
        val notifiedParties = mutableListOf<String>()

        assertDoesNotThrow {
            sendAddedToPropertyNotifications("freeholder", listOf("first", "second", "third"), 1L) { party ->
                notifiedParties.add(party)
                if (party == "second") throw RuntimeException("Notification failed to send")
                AssociatedPartyNotificationOutcome.LETTER_SENT
            }
        }

        assertEquals(listOf("first", "second", "third"), notifiedParties)
    }

    @Test
    fun `sendAddedToPropertyNotifications reports a notification skipped because details are fully redacted`() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out

        System.setOut(PrintStream(outContent))
        try {
            sendAddedToPropertyNotifications("superior landlord", listOf("sent", "redacted"), 1L) { party ->
                if (party == "redacted") {
                    AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED
                } else {
                    AssociatedPartyNotificationOutcome.LETTER_SENT
                }
            }
        } finally {
            System.setOut(originalOut)
        }

        assertEquals(
            "Not sending superior landlord 2 added-to-property notification for property ownership 1: details are fully redacted",
            outContent.toString().trim(),
        )
    }

    @Test
    fun `sendAddedToPropertyNotifications reports a notification skipped because the party is a landlord`() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out

        System.setOut(PrintStream(outContent))
        try {
            sendAddedToPropertyNotifications("freeholder", listOf("landlord", "sent"), 1L) { party ->
                if (party == "landlord") {
                    AssociatedPartyNotificationOutcome.NOT_SENT_IS_LANDLORD
                } else {
                    AssociatedPartyNotificationOutcome.EMAIL_SENT
                }
            }
        } finally {
            System.setOut(originalOut)
        }

        assertEquals(
            "Not sending freeholder 1 added-to-property notification for property ownership 1: they are a landlord on the property",
            outContent.toString().trim(),
        )
    }
}
