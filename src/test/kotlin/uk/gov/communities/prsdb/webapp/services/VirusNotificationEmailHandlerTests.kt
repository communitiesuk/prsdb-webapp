package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPrsdbUserData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.util.Optional

class VirusNotificationEmailHandlerTests {
    private lateinit var virusNotificationEmailHandler: VirusNotificationEmailHandler

    private lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository
    private lateinit var landlordRepository: LandlordRepository
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository
    private lateinit var landlordUserEmailService: LandlordUserEmailService

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        propertyOwnershipRepository = mock()
        landlordRepository = mock()
        organisationalLandlordUserRepository = mock()
        savedJourneyStateRepository = mock()
        lettingAgentAccessRepository = mock()
        landlordUserEmailService = mock()
        virusNotificationEmailHandler =
            VirusNotificationEmailHandler(
                emailNotificationService,
                propertyOwnershipRepository,
                landlordRepository,
                organisationalLandlordUserRepository,
                savedJourneyStateRepository,
                lettingAgentAccessRepository,
                landlordUserEmailService,
                virusMonitoringEmail,
            )
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for monitoring email sends email to the monitoring team`(testType: CertificateType) {
        // Arrange
        val (ownershipId, expectedEmail) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(testType),
                listOf("test@example.com"),
            )

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, testType)
        val encodedCallbackData =
            Json.encodeToString<EmailNotificationData>(
                EmailNotificationData.VirusMonitoringEmailNotification(callbackData),
            )
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToAddress(listOf(virusMonitoringEmail), expectedEmail)
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for send owner email sends email to every landlord on the property`(testType: CertificateType) {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(testType),
                landlordEmails,
            )

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, testType)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `handleCallback for send owner email sends email to landlords and letting agent if present`() {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com")
        val lettingAgentEmail = "agent@example.com"
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(CertificateType.GasSafetyCert),
                landlordEmails,
            )
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(ownershipId))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(invitedEmail = lettingAgentEmail))

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService).sendEmail(
            eq(lettingAgentEmail),
            eq(expectedEmail),
        )
    }

    @Test
    fun `handleCallback for send owner email sends email to all individual joint landlords and the letting agent`() {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val lettingAgentEmail = "agent@example.com"
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(CertificateType.GasSafetyCert),
                landlordEmails,
            )
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(ownershipId))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(invitedEmail = lettingAgentEmail))

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService).sendEmail(
            eq(lettingAgentEmail),
            eq(expectedEmail),
        )
    }

    @Test
    fun `handleCallback for send owner email sends email to a sole organisational landlord`() {
        // Arrange
        val orgEmail = "org-registrant@example.com"
        val orgLandlord = MockLandlordData.createOrgLandlord(registrantEmail = orgEmail)
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallbackForLandlords(
                expectedCertType(CertificateType.GasSafetyCert),
                listOf(orgLandlord),
            )

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `handleCallback for send owner email sends email to individual and organisational joint landlords and the letting agent`() {
        // Arrange
        val individualLandlord = MockLandlordData.createIndividualLandlord(email = "landlord1@example.com")
        val orgLandlord = MockLandlordData.createOrgLandlord(registrantEmail = "org-registrant@example.com")
        val lettingAgentEmail = "agent@example.com"
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallbackForLandlords(
                expectedCertType(CertificateType.GasSafetyCert),
                listOf(individualLandlord, orgLandlord),
            )
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(ownershipId))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(invitedEmail = lettingAgentEmail))

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService).sendEmail(
            eq(lettingAgentEmail),
            eq(expectedEmail),
        )
    }

    private fun expectedCertType(certType: CertificateType) =
        when (certType) {
            CertificateType.GasSafetyCert -> "Gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    private fun createSavedJourneyState(
        journeyId: String,
        userId: String,
    ) = MockSavedJourneyStateData.createSavedJourneyState(
        journeyId = journeyId,
        serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress("1 Main St, Anytown"),
        baseUser = MockPrsdbUserData.createPrsdbUser(userId),
    )

    private fun arrangeSavedJourneyStateForUser(
        journeyId: String,
        userId: String,
    ) {
        whenever(savedJourneyStateRepository.findByJourneyIdAndUser_Id(journeyId, userId))
            .thenReturn(createSavedJourneyState(journeyId, userId))
    }

    private fun arrangeSavedJourneyStateForUsers(
        journeyId: String,
        baseUserIds: Collection<String>,
        savedJourneyStateUserId: String = baseUserIds.first(),
    ) {
        whenever(savedJourneyStateRepository.findFirstByJourneyIdAndUser_IdIn(journeyId, baseUserIds))
            .thenReturn(createSavedJourneyState(journeyId, savedJourneyStateUserId))
    }

    private fun arrangeUserDetails(
        userId: String,
        name: String,
        email: String,
    ) {
        whenever(landlordUserEmailService.getUserDetailsByBaseUserId(userId))
            .thenReturn(LandlordUserDetails(name = name, email = email))
    }

    private fun expectedIncompletePropertyEmail(certType: CertificateType) =
        VirusScanUnsuccessfulEmail(
            certificateType = expectedCertType(certType),
            propertyAddress = "1 Main St\nAnytown",
        )

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete property with a userId emails that user`(certType: CertificateType) {
        // Arrange
        arrangeSavedJourneyStateForUser(journeyId = "journey-1", userId = "subject-1")
        arrangeUserDetails("subject-1", "Jane Smith", "jane@example.com")
        val expectedEmail = expectedIncompletePropertyEmail(certType)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                "journey-1",
                certType,
                landlordId = 7L,
                userId = "subject-1",
            )

        // Act
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
        )

        // Assert
        verify(emailNotificationService).sendEmail(eq("jane@example.com"), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmailToLandlord(any(), any())
        verify(landlordRepository, never()).findById(any())
    }

    @Test
    fun `handleCallback for incomplete property with a userId throws when the saved journey state cannot be found`() {
        // Arrange
        whenever(savedJourneyStateRepository.findByJourneyIdAndUser_Id("journey-1", "subject-1")).thenReturn(null)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                "journey-1",
                CertificateType.Eicr,
                landlordId = 7L,
                userId = "subject-1",
            )

        // Act, Assert
        assertThrows<IllegalStateException> {
            virusNotificationEmailHandler.handleCallback(
                VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
            )
        }
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete-property monitoring email sends to the monitoring team`(certType: CertificateType) {
        // Arrange
        arrangeSavedJourneyStateForUser(journeyId = "journey-1", userId = "subject-1")
        val expectedEmail = expectedIncompletePropertyEmail(certType)
        val inner =
            EmailNotificationData.IncompletePropertyEmailNotification(
                "journey-1",
                certType,
                landlordId = 7L,
                userId = "subject-1",
            )
        val data = EmailNotificationData.VirusMonitoringEmailNotification(inner)

        // Act
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
        )

        // Assert
        verify(emailNotificationService).sendEmail(eq(virusMonitoringEmail), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmailToLandlord(any(), any())
    }

    // TODO PDJB-1919: Remove the legacy (no userId) tests below once all saved notifications have a userId
    @Test
    fun `handleCallback for legacy incomplete property notification without a userId emails the individual landlord user`() {
        // Arrange
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
                name = "Jane Smith",
                email = "jane@example.com",
            )
        doReturn(Optional.of(landlord)).whenever(landlordRepository).findById(7L)
        arrangeSavedJourneyStateForUsers(journeyId = "journey-1", baseUserIds = listOf("subject-1"))
        arrangeUserDetails("subject-1", "Jane Smith", "jane@example.com")
        val expectedEmail = expectedIncompletePropertyEmail(CertificateType.Eicr)
        val data = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, landlordId = 7L)

        // Act
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
        )

        // Assert
        verify(emailNotificationService).sendEmail(eq("jane@example.com"), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `handleCallback for legacy incomplete property notification without a userId emails only the org user who owns the journey`() {
        // Arrange
        val firstUser = MockPrsdbUserData.createPrsdbUser("org-subuser-1")
        val secondUser = MockPrsdbUserData.createPrsdbUser("org-subuser-2")
        val orgLandlord =
            MockLandlordData.createOrgLandlord(
                baseUser = firstUser,
                name = "Org Landlord",
                registrantEmail = "org-admin@example.com",
            )
        doReturn(Optional.of(orgLandlord)).whenever(landlordRepository).findById(9L)
        whenever(organisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord))
            .thenReturn(
                listOf(
                    MockLandlordData.createOrganisationalLandlordUser(baseUser = firstUser, organisationalLandlord = orgLandlord),
                    MockLandlordData.createOrganisationalLandlordUser(baseUser = secondUser, organisationalLandlord = orgLandlord),
                ),
            )
        arrangeSavedJourneyStateForUsers(
            journeyId = "journey-1",
            baseUserIds = listOf("org-subuser-1", "org-subuser-2"),
            savedJourneyStateUserId = "org-subuser-2",
        )
        arrangeUserDetails("org-subuser-2", "Second User", "second@example.com")
        val expectedEmail = expectedIncompletePropertyEmail(CertificateType.GasSafetyCert)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.GasSafetyCert, landlordId = 9L)

        // Act
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
        )

        // Assert
        verify(emailNotificationService).sendEmail(eq("second@example.com"), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `handleCallback for legacy incomplete property notification throws when the landlord cannot be found`() {
        // Arrange
        doReturn(Optional.empty<Landlord>()).whenever(landlordRepository).findById(7L)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, landlordId = 7L)

        // Act, Assert
        assertThrows<IllegalStateException> {
            virusNotificationEmailHandler.handleCallback(
                VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
            )
        }
    }

    @Test
    fun `handleCallback for legacy incomplete property notification throws when the saved journey state cannot be found`() {
        // Arrange
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
            )
        doReturn(Optional.of(landlord)).whenever(landlordRepository).findById(7L)
        whenever(savedJourneyStateRepository.findFirstByJourneyIdAndUser_IdIn("journey-1", listOf("subject-1"))).thenReturn(null)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, landlordId = 7L)

        // Act, Assert
        assertThrows<IllegalStateException> {
            virusNotificationEmailHandler.handleCallback(
                VirusScanCallback(mock(), Json.encodeToString<EmailNotificationData>(data)),
            )
        }
    }

    private fun arrangeOwnedPropertyUploadCallback(
        bodyCertificateType: String,
        emailAddresses: List<String>,
    ): Triple<Long, VirusScanUnsuccessfulEmail, List<Landlord>> =
        arrangeOwnedPropertyUploadCallbackForLandlords(
            bodyCertificateType,
            emailAddresses.map { MockLandlordData.createIndividualLandlord(email = it) },
        )

    private fun arrangeOwnedPropertyUploadCallbackForLandlords(
        bodyCertificateType: String,
        landlords: List<Landlord>,
    ): Triple<Long, VirusScanUnsuccessfulEmail, List<Landlord>> {
        val ownership =
            MockLandlordData.createPropertyOwnership(
                landlords = landlords.toMutableSet(),
                address = MockLandlordData.createAddress(singleLineAddress = "123 Main St, Anytown"),
                isOccupied = true,
            )

        whenever(propertyOwnershipRepository.findByIdAndIsActiveTrue(ownership.id)).thenReturn(ownership)

        return Triple(
            ownership.id,
            VirusScanUnsuccessfulEmail(
                certificateType = bodyCertificateType,
                propertyAddress = "123 Main St\nAnytown",
            ),
            landlords,
        )
    }

    private fun assertEmailSentToLandlords(
        landlords: List<Landlord>,
        expectedEmail: VirusScanUnsuccessfulEmail,
    ) {
        landlords.forEach { landlord ->
            verify(emailNotificationService).sendEmailToLandlord(
                eq(landlord),
                eq(expectedEmail),
            )
        }
    }

    private fun assertEmailSentToAddress(
        emailAddresses: List<String>,
        expectedEmail: VirusScanUnsuccessfulEmail,
    ) {
        val emailModelCaptor = argumentCaptor<VirusScanUnsuccessfulEmail>()
        val emailAddressCaptor = argumentCaptor<String>()

        verify(emailNotificationService, times(emailAddresses.size)).sendEmail(
            emailAddressCaptor.capture(),
            emailModelCaptor.capture(),
        )

        emailAddresses.forEachIndexed { ind, emailAddress ->
            assertEquals(expectedEmail, emailModelCaptor.allValues[ind])
            assertEquals(emailAddresses[ind], emailAddressCaptor.allValues[ind])
        }
    }
}
