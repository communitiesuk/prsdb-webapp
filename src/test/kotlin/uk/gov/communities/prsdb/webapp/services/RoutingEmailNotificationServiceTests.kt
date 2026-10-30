package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

class RoutingEmailNotificationServiceTests {
    private val emailDeliveryService: EmailDeliveryService = mock()
    private val routingEmailNotificationService =
        RoutingEmailNotificationService<BetaFeedbackEmail>(emailDeliveryService)

    @Test
    fun `sendEmail delivers the email through the delivery service`() {
        val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

        routingEmailNotificationService.sendEmail("recipient@example.com", email)

        verify(emailDeliveryService).deliverEmail("recipient@example.com", email)
    }
}
