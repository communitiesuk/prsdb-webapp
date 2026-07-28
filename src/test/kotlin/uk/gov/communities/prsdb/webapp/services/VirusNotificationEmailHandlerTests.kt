package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import java.net.URI

class VirusNotificationEmailHandlerTests {
    private lateinit var virusNotificationEmailHandler: VirusNotificationEmailHandler

    private lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository
    private lateinit var individualLandlordRepository: IndividualLandlordRepository
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        propertyOwnershipRepository = mock()
        individualLandlordRepository = mock()
        savedJourneyStateRepository = mock()
        virusNotificationEmailHandler =
            VirusNotificationEmailHandler(
                emailNotificationService,
                absoluteUrlProvider,
                propertyOwnershipRepository,
                individualLandlordRepository,
                savedJourneyStateRepository,
                virusMonitoringEmail,
            )
    }

    companion object {
        @JvmStatic
        fun certificateTestParameters(): List<Array<Any>> =
            listOf(
                arrayOf(CertificateType.GasSafetyCert, "gas safety certificate"),
                arrayOf(CertificateType.Eicr, "EICR"),
                arrayOf(CertificateType.Eic, "EIC"),
            )

        @JvmStatic
        fun incompletePropertyParameters(): List<Array<Any>> =
            listOf(
                arrayOf(CertificateType.GasSafetyCert, "gas safety certificate"),
                arrayOf(CertificateType.Eicr, "EICR"),
                arrayOf(CertificateType.Eic, "EIC"),
            )
    }

    @ParameterizedTest
    @MethodSource("certificateTestParameters")
    fun `handleCallback for monitoring email sends email to the monitoring team`(
        testType: CertificateType,
        expectedBody: String,
    ) {
        // Arrange
        val (ownershipId, expectedEmail) =
            arrangeOwnedPropertyUploadCallback(
                expectedBody,
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
    @MethodSource("certificateTestParameters")
    fun `handleCallback for send owner email sends email to every landlord on the property`(
        testType: CertificateType,
        expectedBody: String,
    ) {
        // Arrange
        val landlordEmails = listOf("landlord1@example.com", "landlord2@example.com", "landlord3@example.com")
        val (ownershipId, expectedEmail) =
            arrangeOwnedPropertyUploadCallback(
                expectedBody,
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

    private val dashboardUri = URI("https://landlord.example.com/dashboard")

    private fun expectedCertType(certType: CertificateType) =
        when (certType) {
            CertificateType.GasSafetyCert -> "gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    private fun arrangeIncompletePropertyCallback(certType: CertificateType): VirusScanUnsuccessfulEmail {
        val landlord = MockLandlordData.createIndividualLandlord(name = "Jane Smith", email = "jane@example.com")
        whenever(individualLandlordRepository.findByBaseUser_Id("subject-1")).thenReturn(landlord)
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                journeyId = "journey-1",
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress("1 Main St, Anytown"),
            )
        whenever(savedJourneyStateRepository.findByJourneyIdAndUser_Id("journey-1", "subject-1")).thenReturn(
            savedJourneyState,
        )
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        return VirusScanUnsuccessfulEmail(
            certificateType = expectedCertType(certType),
            recipientName = "Jane Smith",
            propertyAddress = "1 Main St, Anytown",
            registerRentalPropertyURL = dashboardUri,
        )
    }

    @ParameterizedTest
    @MethodSource("incompletePropertyParameters")
    fun `handleCallback for incomplete property emails the registering landlord`(
        certType: CertificateType,
        expectedCertString: String,
    ) {
        val expectedEmail = arrangeIncompletePropertyCallback(certType)
        val data = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, "subject-1")
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
    @MethodSource("incompletePropertyParameters")
    fun `handleCallback for incomplete-property monitoring email sends to the monitoring team`(
        certType: CertificateType,
        expectedCertString: String,
    ) {
        val expectedEmail = arrangeIncompletePropertyCallback(certType)
        val inner = EmailNotificationData.IncompletePropertyEmailNotification("journey-1", certType, "subject-1")
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
        whenever(individualLandlordRepository.findByBaseUser_Id("subject-1")).thenReturn(null)
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, "subject-1")
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
    ): Pair<Long, VirusScanUnsuccessfulEmail> {
        val ownership =
            MockLandlordData.createPropertyOwnership(
                landlords = emailAddresses.mapTo(mutableSetOf()) { MockLandlordData.createIndividualLandlord(email = it) },
                address = MockLandlordData.createAddress(singleLineAddress = "123 Main St, Anytown"),
            )

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        whenever(propertyOwnershipRepository.findByIdAndIsActiveTrue(ownership.id)).thenReturn(ownership)

        return Pair(
            ownership.id,
            VirusScanUnsuccessfulEmail(
                certificateType = bodyCertificateType,
                recipientName = "name",
                propertyAddress = "123 Main St, Anytown",
                registerRentalPropertyURL = dashboardUri,
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
