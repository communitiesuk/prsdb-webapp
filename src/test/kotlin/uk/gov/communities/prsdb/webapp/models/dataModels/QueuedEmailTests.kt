package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

class QueuedEmailTests {
    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

    @Test
    fun `from captures the recipient, template and personalisation of an email`() {
        val queuedEmail = QueuedEmail.from("recipient@example.com", email)

        assertEquals(QueuedEmail(email.template, "recipient@example.com", email.toHashMap()), queuedEmail)
    }

    @Test
    fun `a queued email survives a JSON round trip`() {
        val queuedEmail = QueuedEmail.from("recipient@example.com", email)

        val decoded = Json.decodeFromString<QueuedEmail>(Json.encodeToString(queuedEmail))

        assertEquals(queuedEmail, decoded)
    }

    @Test
    fun `toEmailTemplateModel recreates an email with the same template and personalisation`() {
        val recreated = QueuedEmail.from("recipient@example.com", email).toEmailTemplateModel()

        assertEquals(email.template, recreated.template)
        assertEquals(email.toHashMap(), recreated.toHashMap())
    }
}
