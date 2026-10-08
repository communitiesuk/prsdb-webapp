package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.DelegateToLettingAgentInvitationWithDeadlineEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyRegistrationData

class PropertyRegistrationServicePersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var jointLandlordInvitationRepository: JointLandlordInvitationRepository

    @Autowired
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @MockitoBean
    private lateinit var jointLandlordInvitationEmailSender: EmailNotificationService<JointLandlordInvitationEmail>

    @MockitoBean
    private lateinit var jointLandlordInvitationConfirmationEmailSender: EmailNotificationService<JointLandlordInvitationConfirmationEmail>

    @MockitoBean
    private lateinit var lettingAgentInvitationEmailSender: EmailNotificationService<DelegateToLettingAgentInvitationWithDeadlineEmail>

    @MockitoBean
    private lateinit var mockConfirmationService: PropertyRegistrationConfirmationService

    private val registrationData =
        MockPropertyRegistrationData.createPropertyRegistrationDataModel(
            addressModel = AddressDataModel(singleLineAddress = "1 Example Road, EG1 2AB", postcode = "EG1 2AB"),
            jointLandlordEmails = listOf(JOINT_LANDLORD_EMAIL),
            lettingAgentEmail = LETTING_AGENT_EMAIL,
        )

    @BeforeEach
    fun setMockUser() {
        JourneyTestHelper.setMockUser(USER_ID)
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `registerProperty sends no emails when the transaction it is part of rolls back`() {
        // Act
        val propertyOwnership =
            TransactionTemplate(transactionManager).execute { transaction ->
                propertyRegistrationService.registerProperty(registrationData).also { transaction.setRollbackOnly() }
            }!!

        // Assert
        verifyNoEmailsSent()
        assertTrue(propertyOwnershipRepository.findById(propertyOwnership.id).isEmpty)
        assertTrue(jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyOwnership.id).isEmpty())
        assertNull(lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnership.id))
    }

    @Test
    fun `registerProperty sends its emails once the transaction it is part of commits`() {
        // Act
        TransactionTemplate(transactionManager).executeWithoutResult {
            propertyRegistrationService.registerProperty(registrationData)
            verifyNoEmailsSent()
        }

        // Assert
        verify(confirmationEmailSender).sendEmail(eq(LANDLORD_EMAIL), any())
        verify(lettingAgentInvitationEmailSender).sendEmail(eq(LETTING_AGENT_EMAIL), any())
        verify(jointLandlordInvitationEmailSender).sendEmail(eq(JOINT_LANDLORD_EMAIL), any())
        verify(jointLandlordInvitationConfirmationEmailSender).sendEmail(eq(LANDLORD_EMAIL), any())
    }

    @Test
    fun `registerProperty keeps the registration and its invitations when an email fails to send`() {
        // Arrange
        doThrow(RuntimeException("Email failed to send")).whenever(jointLandlordInvitationEmailSender).sendEmail(any(), any())

        // Act
        val propertyOwnership = propertyRegistrationService.registerProperty(registrationData)

        // Assert
        assertTrue(propertyOwnershipRepository.findById(propertyOwnership.id).isPresent)
        val invitedEmails = jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyOwnership.id).map { it.invitedEmail }
        assertEquals(listOf(JOINT_LANDLORD_EMAIL), invitedEmails)
        assertEquals(LETTING_AGENT_EMAIL, lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnership.id)?.invitedEmail)
        verify(confirmationEmailSender).sendEmail(eq(LANDLORD_EMAIL), any())
        verify(lettingAgentInvitationEmailSender).sendEmail(eq(LETTING_AGENT_EMAIL), any())
        verify(jointLandlordInvitationConfirmationEmailSender).sendEmail(eq(LANDLORD_EMAIL), any())
    }

    private fun verifyNoEmailsSent() =
        verifyNoInteractions(
            confirmationEmailSender,
            lettingAgentInvitationEmailSender,
            jointLandlordInvitationEmailSender,
            jointLandlordInvitationConfirmationEmailSender,
        )

    companion object {
        private const val USER_ID = "urn:fdc:gov.uk:2022:UVWXY"
        private const val LANDLORD_EMAIL = "alex.surname@example.com"
        private const val JOINT_LANDLORD_EMAIL = "joint.landlord@example.com"
        private const val LETTING_AGENT_EMAIL = "letting.agent@example.com"
    }
}
