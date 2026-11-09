package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser

class EmailNotificationServiceTests {
    private class TestEmail : EmailTemplateModel {
        override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    private val emailDeliveryService: EmailDeliveryService = mock()
    private val emailQueueService: EmailQueueService = mock()
    private val featureFlagManager: FeatureFlagManager = mock()
    private val emailNotificationService =
        EmailNotificationService<EmailTemplateModel>(emailDeliveryService, emailQueueService, featureFlagManager)

    private fun deliveredRecipients(): List<String> {
        val recipientCaptor = argumentCaptor<String>()
        verify(emailDeliveryService, atLeast(0)).deliverEmail(recipientCaptor.capture(), any())
        return recipientCaptor.allValues
    }

    @Test
    fun `sendEmail delivers the email synchronously when asynchronous sending is disabled`() {
        whenever(featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)).thenReturn(false)
        val email = TestEmail()

        emailNotificationService.sendEmail("recipient@example.com", email)

        verify(emailDeliveryService).deliverEmail("recipient@example.com", email)
        verifyNoInteractions(emailQueueService)
    }

    @Test
    fun `sendEmail queues the email when asynchronous sending is enabled`() {
        whenever(featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)).thenReturn(true)
        val email = TestEmail()

        emailNotificationService.sendEmail("recipient@example.com", email)

        verify(emailQueueService).enqueue("recipient@example.com", email)
        verifyNoInteractions(emailDeliveryService)
    }

    @Test
    fun `sendEmailToLandlord sends to the individual landlord's email`() {
        val landlord = createIndividualLandlord(email = "individual@example.com")
        val email = TestEmail()

        emailNotificationService.sendEmailToLandlord(landlord, email)

        verify(emailDeliveryService).deliverEmail("individual@example.com", email)
        assertEquals(listOf("individual@example.com"), deliveredRecipients())
    }

    @Test
    fun `sendEmailToLandlord sends only to the organisation's administrators`() {
        val organisationalLandlord = createOrgLandlord(registrantEmail = "admin@example.com")
        createOrganisationalLandlordUser(
            organisationalLandlord = organisationalLandlord,
            email = "editor@example.com",
            role = OrganisationalLandlordUserRole.EDITOR,
        )
        val email = TestEmail()

        emailNotificationService.sendEmailToLandlord(organisationalLandlord, email)

        verify(emailDeliveryService).deliverEmail("admin@example.com", email)
        assertEquals(listOf("admin@example.com"), deliveredRecipients())
    }

    @Test
    fun `sendEmailToLandlord sends to every administrator in the organisation`() {
        val organisationalLandlord = createOrgLandlord(registrantEmail = "admin-one@example.com")
        createOrganisationalLandlordUser(
            organisationalLandlord = organisationalLandlord,
            email = "admin-two@example.com",
            role = OrganisationalLandlordUserRole.ADMIN,
        )
        val email = TestEmail()

        emailNotificationService.sendEmailToLandlord(organisationalLandlord, email)

        verify(emailDeliveryService).deliverEmail("admin-one@example.com", email)
        verify(emailDeliveryService).deliverEmail("admin-two@example.com", email)
        assertEquals(setOf("admin-one@example.com", "admin-two@example.com"), deliveredRecipients().toSet())
    }

    @Test
    fun `sendEmailToLandlord throws when the organisation has no administrators`() {
        val organisationalLandlord = createOrgLandlord(registrantRole = OrganisationalLandlordUserRole.EDITOR)
        val email = TestEmail()

        assertThrows<NoLandlordEmailRecipientsException> {
            emailNotificationService.sendEmailToLandlord(organisationalLandlord, email)
        }

        verifyNoInteractions(emailDeliveryService)
        verifyNoInteractions(emailQueueService)
    }
}
