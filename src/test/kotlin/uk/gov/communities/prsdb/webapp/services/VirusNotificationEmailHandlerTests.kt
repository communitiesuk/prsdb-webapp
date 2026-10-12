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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
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
import java.net.URI
import java.util.Optional

class VirusNotificationEmailHandlerTests {
    private lateinit var virusNotificationEmailHandler: VirusNotificationEmailHandler

    private lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository
    private lateinit var landlordRepository: LandlordRepository
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository
    private lateinit var featureFlagManager: FeatureFlagManager

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        propertyOwnershipRepository = mock()
        landlordRepository = mock()
        organisationalLandlordUserRepository = mock()
        savedJourneyStateRepository = mock()
        lettingAgentAccessRepository = mock()
        featureFlagManager = mock()
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri())
            .thenReturn(URI("https://www.prsd.gov.uk/landlord/dashboard"))
        virusNotificationEmailHandler =
            VirusNotificationEmailHandler(
                emailNotificationService,
                absoluteUrlProvider,
                propertyOwnershipRepository,
                landlordRepository,
                organisationalLandlordUserRepository,
                savedJourneyStateRepository,
                lettingAgentAccessRepository,
                featureFlagManager,
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
                recipientName = "Monitoring Team",
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
            eq(expectedEmail.copy(recipientName = lettingAgentEmail)),
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
            eq(expectedEmail.copy(recipientName = lettingAgentEmail)),
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
                recipientName = orgLandlord.name,
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
            eq(expectedEmail.copy(recipientName = lettingAgentEmail)),
        )
    }

    @Test
    fun `handleCallback does not email letting agent when delegation feature is disabled`() {
        // Arrange
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(CertificateType.GasSafetyCert),
                listOf("landlord1@example.com"),
            )
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(ownershipId))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(invitedEmail = "agent@example.com"))

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService, never()).sendEmail(any(), any())
        verify(lettingAgentAccessRepository, never()).findByPropertyOwnershipId(ownershipId)
    }

    @Test
    fun `handleCallback does not email letting agent for joint landlords when delegation feature is disabled`() {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com", "landlord2@example.com")
        val (ownershipId, expectedEmail, landlords) =
            arrangeOwnedPropertyUploadCallback(
                expectedCertType(CertificateType.GasSafetyCert),
                landlordEmails,
            )
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(ownershipId))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(invitedEmail = "agent@example.com"))

        // Act
        val callbackData = EmailNotificationData.OwnerEmailNotification(ownershipId, CertificateType.GasSafetyCert)
        val encodedCallbackData = Json.encodeToString<EmailNotificationData>(callbackData)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(mock(), encodedCallbackData),
        )

        // Assert
        assertEmailSentToLandlords(landlords, expectedEmail)
        verify(emailNotificationService, never()).sendEmail(any(), any())
        verify(lettingAgentAccessRepository, never()).findByPropertyOwnershipId(ownershipId)
    }

    private fun expectedCertType(certType: CertificateType) =
        when (certType) {
            CertificateType.GasSafetyCert -> "Gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    private fun arrangeSavedJourneyStateForUsers(
        journeyId: String,
        baseUserIds: Collection<String>,
        singleLineAddress: String = "1 Main St, Anytown",
    ) {
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = journeyId,
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(singleLineAddress),
            )
        whenever(savedJourneyStateRepository.findFirstByJourneyIdAndUser_IdIn(journeyId, baseUserIds)).thenReturn(
            savedJourneyState,
        )
    }

    private fun expectedIncompletePropertyEmail(
        certType: CertificateType,
        recipientName: String,
    ) = VirusScanUnsuccessfulEmail(
        certificateType = expectedCertType(certType),
        recipientName = recipientName,
        propertyAddress = "1 Main St\nAnytown",
        landlordDashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
    )

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete property emails an individual landlord`(certType: CertificateType) {
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
                name = "Jane Smith",
                email = "jane@example.com",
            )
        doReturn(Optional.of(landlord)).whenever(landlordRepository).findById(7L)
        arrangeSavedJourneyStateForUsers(journeyId = "journey-1", baseUserIds = listOf("subject-1"))
        val expectedEmail = expectedIncompletePropertyEmail(certType, "Jane Smith")

        val data = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, landlordId = 7L)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(
                mock(),
                Json.encodeToString<EmailNotificationData>(data),
            ),
        )

        verify(emailNotificationService).sendEmailToLandlord(eq(landlord), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmail(any(), any())
    }

    @Test
    fun `handleCallback for incomplete property emails an organisational landlord`() {
        val baseUser = MockPrsdbUserData.createPrsdbUser("org-subuser-1")
        val orgLandlord =
            MockLandlordData.createOrgLandlord(
                baseUser = baseUser,
                name = "Org Landlord",
                registrantEmail = "org-admin@example.com",
            )
        doReturn(Optional.of(orgLandlord)).whenever(landlordRepository).findById(9L)
        whenever(organisationalLandlordUserRepository.findByOrganisationalLandlord(orgLandlord))
            .thenReturn(
                listOf(
                    MockLandlordData.createOrganisationalLandlordUser(
                        baseUser = baseUser,
                        organisationalLandlord = orgLandlord,
                    ),
                ),
            )
        arrangeSavedJourneyStateForUsers(journeyId = "journey-1", baseUserIds = listOf("org-subuser-1"))
        val expectedEmail = expectedIncompletePropertyEmail(CertificateType.GasSafetyCert, "Org Landlord")

        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.GasSafetyCert, landlordId = 9L)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(
                mock(),
                Json.encodeToString<EmailNotificationData>(data),
            ),
        )

        verify(emailNotificationService).sendEmailToLandlord(eq(orgLandlord), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmail(any(), any())
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete-property monitoring email sends to the monitoring team`(certType: CertificateType) {
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
                name = "Jane Smith",
                email = "jane@example.com",
            )
        doReturn(Optional.of(landlord)).whenever(landlordRepository).findById(7L)
        arrangeSavedJourneyStateForUsers(journeyId = "journey-1", baseUserIds = listOf("subject-1"))
        val expectedEmail = expectedIncompletePropertyEmail(certType, "Monitoring Team")

        val inner = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, landlordId = 7L)
        val data = EmailNotificationData.VirusMonitoringEmailNotification(inner)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(
                mock(),
                Json.encodeToString<EmailNotificationData>(data),
            ),
        )

        verify(emailNotificationService).sendEmail(eq(virusMonitoringEmail), eq(expectedEmail))
        verify(emailNotificationService, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `handleCallback for incomplete property throws when the landlord cannot be found`() {
        doReturn(Optional.empty<Landlord>()).whenever(landlordRepository).findById(7L)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, landlordId = 7L)
        assertThrows<IllegalStateException> {
            virusNotificationEmailHandler.handleCallback(
                VirusScanCallback(
                    mock(),
                    Json.encodeToString<EmailNotificationData>(data),
                ),
            )
        }
    }

    @Test
    fun `handleCallback for incomplete property throws when the saved journey state cannot be found`() {
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
            )
        doReturn(Optional.of(landlord)).whenever(landlordRepository).findById(7L)
        whenever(savedJourneyStateRepository.findFirstByJourneyIdAndUser_IdIn("journey-1", listOf("subject-1"))).thenReturn(null)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, landlordId = 7L)
        assertThrows<IllegalStateException> {
            virusNotificationEmailHandler.handleCallback(
                VirusScanCallback(
                    mock(),
                    Json.encodeToString<EmailNotificationData>(data),
                ),
            )
        }
    }

    private fun arrangeOwnedPropertyUploadCallback(
        bodyCertificateType: String,
        emailAddresses: List<String>,
        recipientName: String = "name",
    ): Triple<Long, VirusScanUnsuccessfulEmail, List<Landlord>> =
        arrangeOwnedPropertyUploadCallbackForLandlords(
            bodyCertificateType,
            emailAddresses.map { MockLandlordData.createIndividualLandlord(email = it) },
            recipientName,
        )

    private fun arrangeOwnedPropertyUploadCallbackForLandlords(
        bodyCertificateType: String,
        landlords: List<Landlord>,
        recipientName: String = "name",
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
                recipientName = recipientName,
                propertyAddress = "123 Main St\nAnytown",
                landlordDashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
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
                eq(expectedEmail.copy(recipientName = landlord.name)),
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
