package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.assertDoesNotThrow
import uk.gov.communities.prsdb.webapp.helpers.EmailHelper.Companion.sendEmailAndLogFailures
import kotlin.test.Test
import kotlin.test.assertTrue

class EmailHelperTests {
    @Test
    fun `sendEmailAndLogFailures sends the email`() {
        var sent = false

        sendEmailAndLogFailures("a test email") { sent = true }

        assertTrue(sent)
    }

    @Test
    fun `sendEmailAndLogFailures does not throw when the email fails to send`() {
        assertDoesNotThrow {
            sendEmailAndLogFailures("a test email") { throw RuntimeException("Email failed to send") }
        }
    }
}
