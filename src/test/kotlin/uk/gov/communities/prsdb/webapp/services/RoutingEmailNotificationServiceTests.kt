package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

class RoutingEmailNotificationServiceTests {
    private val emailDeliveryService: EmailDeliveryService = mock()
    private val emailQueueService: EmailQueueService = mock()
    private val featureFlagManager: FeatureFlagManager = mock()
    private val routingEmailNotificationService =
        RoutingEmailNotificationService<BetaFeedbackEmail>(emailDeliveryService, emailQueueService, featureFlagManager)
    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

    @Test
    fun `sendEmail delivers the email synchronously when asynchronous sending is disabled`() {
        whenever(featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)).thenReturn(false)

        routingEmailNotificationService.sendEmail("recipient@example.com", email)

        verify(emailDeliveryService).deliverEmail("recipient@example.com", email)
        verifyNoInteractions(emailQueueService)
    }

    @Test
    fun `sendEmail queues the email when asynchronous sending is enabled`() {
        whenever(featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)).thenReturn(true)

        routingEmailNotificationService.sendEmail("recipient@example.com", email)

        verify(emailQueueService).enqueue("recipient@example.com", email)
        verifyNoInteractions(emailDeliveryService)
    }
}
