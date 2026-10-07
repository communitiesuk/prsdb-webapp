package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordEmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.adminsOnly
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.anyOrgLandlordUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser

class EmailNotificationServiceExtensionsTests {
    private lateinit var emailSender: EmailNotificationService<LandlordEmailTemplateModel>

    @BeforeEach
    fun setup() {
        emailSender = mock()
    }

    private class TestLandlordEmail(
        override val orgRolesToSendTo: List<OrganisationalLandlordUserRole>,
    ) : LandlordEmailTemplateModel {
        override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    @Test
    fun `sendEmailToLandlord sends to the individual landlord's email`() {
        val landlord = createIndividualLandlord(email = "individual@example.com")
        val email = TestLandlordEmail(anyOrgLandlordUser)

        emailSender.sendEmailToLandlord(landlord, email)

        verify(emailSender).sendEmail(eq("individual@example.com"), eq(email))
    }

    @Test
    fun `sendEmailToLandlord sends only to administrators for an adminsOnly orgRolesToSendTo`() {
        val organisationalLandlord = createOrgLandlord(registrantEmail = "admin@example.com")
        createOrganisationalLandlordUser(
            organisationalLandlord = organisationalLandlord,
            email = "editor@example.com",
            role = OrganisationalLandlordUserRole.EDITOR,
        )
        val email = TestLandlordEmail(adminsOnly)

        emailSender.sendEmailToLandlord(organisationalLandlord, email)

        verify(emailSender).sendEmail(eq("admin@example.com"), eq(email))
        verify(emailSender, never()).sendEmail(eq("editor@example.com"), any())
    }

    @Test
    fun `sendEmailToLandlord sends to all users for an anyOrgLandlordUser orgRolesToSendTo`() {
        val organisationalLandlord = createOrgLandlord(registrantEmail = "admin@example.com")
        createOrganisationalLandlordUser(
            organisationalLandlord = organisationalLandlord,
            email = "editor@example.com",
            role = OrganisationalLandlordUserRole.EDITOR,
        )
        val email = TestLandlordEmail(anyOrgLandlordUser)

        emailSender.sendEmailToLandlord(organisationalLandlord, email)

        verify(emailSender).sendEmail(eq("admin@example.com"), eq(email))
        verify(emailSender).sendEmail(eq("editor@example.com"), eq(email))
    }

    @Test
    fun `sendEmailToLandlord throws when no recipients match the orgRolesToSendTo`() {
        val organisationalLandlord = createOrgLandlord(registrantRole = OrganisationalLandlordUserRole.EDITOR)
        val email = TestLandlordEmail(adminsOnly)

        assertThrows<NoLandlordEmailRecipientsException> {
            emailSender.sendEmailToLandlord(organisationalLandlord, email)
        }

        verify(emailSender, never()).sendEmail(any(), any())
    }
}
