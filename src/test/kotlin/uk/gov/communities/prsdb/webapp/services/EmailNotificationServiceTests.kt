package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser

class EmailNotificationServiceTests {
    private class RecordingEmailSender : EmailNotificationService<EmailTemplateModel> {
        val sentEmails = mutableListOf<Pair<String, EmailTemplateModel>>()

        override fun sendEmail(
            recipientAddress: String,
            email: EmailTemplateModel,
        ) {
            sentEmails.add(recipientAddress to email)
        }
    }

    private class TestEmail : EmailTemplateModel {
        override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    private val emailSender = RecordingEmailSender()

    @Test
    fun `sendEmailToLandlord sends to the individual landlord's email`() {
        val landlord = createIndividualLandlord(email = "individual@example.com")
        val email = TestEmail()

        emailSender.sendEmailToLandlord(landlord, email)

        assertEquals(listOf("individual@example.com" to email), emailSender.sentEmails)
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

        emailSender.sendEmailToLandlord(organisationalLandlord, email)

        assertEquals(listOf("admin@example.com" to email), emailSender.sentEmails)
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

        emailSender.sendEmailToLandlord(organisationalLandlord, email)

        assertEquals(
            setOf("admin-one@example.com", "admin-two@example.com"),
            emailSender.sentEmails.map { it.first }.toSet(),
        )
        assertTrue(emailSender.sentEmails.all { it.second === email })
    }

    @Test
    fun `sendEmailToLandlord throws when the organisation has no administrators`() {
        val organisationalLandlord = createOrgLandlord(registrantRole = OrganisationalLandlordUserRole.EDITOR)
        val email = TestEmail()

        assertThrows<NoLandlordEmailRecipientsException> {
            emailSender.sendEmailToLandlord(organisationalLandlord, email)
        }

        assertTrue(emailSender.sentEmails.isEmpty())
    }
}
