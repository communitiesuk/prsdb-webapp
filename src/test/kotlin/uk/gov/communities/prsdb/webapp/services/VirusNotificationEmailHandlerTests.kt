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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
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
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository
    private lateinit var individualLandlordRepository: IndividualLandlordRepository
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository
    private lateinit var featureFlagManager: FeatureFlagManager

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        propertyOwnershipRepository = mock()
        individualLandlordRepository = mock()
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
                individualLandlordRepository,
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
        val (ownershipId, expectedEmail) =
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
        assertEmailSentToAddress(landlordEmails, expectedEmail)
    }

    @Test
    fun `handleCallback for send owner email sends email to landlords and letting agent if present`() {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com")
        val lettingAgentEmail = "agent@example.com"
        val (ownershipId, expectedEmail) =
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
        val emailModelCaptor = argumentCaptor<VirusScanUnsuccessfulEmail>()
        val emailAddressCaptor = argumentCaptor<String>()

        verify(emailNotificationService, times(2)).sendEmail(
            emailAddressCaptor.capture(),
            emailModelCaptor.capture(),
        )

        assertEquals("landlord1@example.com", emailAddressCaptor.allValues[0])
        assertEquals(expectedEmail, emailModelCaptor.allValues[0])

        assertEquals(lettingAgentEmail, emailAddressCaptor.allValues[1])
        assertEquals(expectedEmail.copy(recipientName = lettingAgentEmail), emailModelCaptor.allValues[1])
    }

    @Test
    fun `handleCallback does not email letting agent when delegation feature is disabled`() {
        // Arrange
        val (ownershipId, expectedEmail) =
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
        assertEmailSentToAddress(listOf("landlord1@example.com"), expectedEmail)
        verify(lettingAgentAccessRepository, never()).findByPropertyOwnershipId(ownershipId)
    }

    private fun expectedCertType(certType: CertificateType) =
        when (certType) {
            CertificateType.GasSafetyCert -> "Gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    private fun arrangeIncompletePropertyCallback(certType: CertificateType): VirusScanUnsuccessfulEmail {
        val landlord =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockPrsdbUserData.createPrsdbUser("subject-1"),
                name = "Jane Smith",
                email = "jane@example.com",
            )
        doReturn(Optional.of(landlord)).whenever(individualLandlordRepository).findById(7L)
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = "journey-1",
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress("1 Main St, Anytown"),
            )
        whenever(savedJourneyStateRepository.findByJourneyIdAndUser_Id("journey-1", "subject-1")).thenReturn(
            savedJourneyState,
        )
        return VirusScanUnsuccessfulEmail(
            certificateType = expectedCertType(certType),
            recipientName = "Jane Smith",
            propertyAddress = "1 Main St, Anytown",
            landlordDashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
        )
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete property emails the registering landlord`(certType: CertificateType) {
        val expectedEmail = arrangeIncompletePropertyCallback(certType)
        val data = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, 7L)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(
                mock(),
                Json.encodeToString<EmailNotificationData>(data),
            ),
        )

        val emailCaptor = argumentCaptor<EmailTemplateModel>()
        val addressCaptor = argumentCaptor<String>()
        verify(emailNotificationService).sendEmail(addressCaptor.capture(), emailCaptor.capture())
        assertEquals("jane@example.com", addressCaptor.firstValue)
        assertEquals(expectedEmail, emailCaptor.firstValue)
    }

    @ParameterizedTest
    @EnumSource(CertificateType::class)
    fun `handleCallback for incomplete-property monitoring email sends to the monitoring team`(certType: CertificateType) {
        val expectedEmail = arrangeIncompletePropertyCallback(certType).copy(recipientName = "Monitoring Team")
        val inner = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, 7L)
        val data = EmailNotificationData.VirusMonitoringEmailNotification(inner)
        virusNotificationEmailHandler.handleCallback(
            VirusScanCallback(
                mock(),
                Json.encodeToString<EmailNotificationData>(data),
            ),
        )

        val emailCaptor = argumentCaptor<EmailTemplateModel>()
        val addressCaptor = argumentCaptor<String>()
        verify(emailNotificationService).sendEmail(addressCaptor.capture(), emailCaptor.capture())
        assertEquals(virusMonitoringEmail, addressCaptor.firstValue)
        assertEquals(expectedEmail, emailCaptor.firstValue)
    }

    @Test
    fun `handleCallback for incomplete property throws when the landlord cannot be found`() {
        doReturn(Optional.empty<IndividualLandlord>()).whenever(individualLandlordRepository).findById(7L)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L)
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
    ): Pair<Long, VirusScanUnsuccessfulEmail> {
        val ownership =
            MockLandlordData.createPropertyOwnership(
                landlords = emailAddresses.mapTo(mutableSetOf()) { MockLandlordData.createIndividualLandlord(email = it) },
                address = MockLandlordData.createAddress(singleLineAddress = "123 Main St, Anytown"),
            )

        whenever(propertyOwnershipRepository.findByIdAndIsActiveTrue(ownership.id)).thenReturn(ownership)

        return Pair(
            ownership.id,
            VirusScanUnsuccessfulEmail(
                certificateType = bodyCertificateType,
                recipientName = recipientName,
                propertyAddress = "123 Main St, Anytown",
                landlordDashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
            ),
        )
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
