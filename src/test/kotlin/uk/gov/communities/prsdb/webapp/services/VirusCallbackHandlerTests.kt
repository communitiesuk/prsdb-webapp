package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import kotlin.toString

class VirusCallbackHandlerTests {
    private lateinit var virusCallbackHandler: VirusCallbackHandler

    private lateinit var emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        propertyOwnershipRepository = mock()
        virusCallbackHandler =
            VirusCallbackHandler(
                emailNotificationService,
                absoluteUrlProvider,
                propertyOwnershipRepository,
                virusMonitoringEmail,
            )
    }

    companion object {
        @JvmStatic
        fun certificateTestParameters(): List<Array<Any>> =
            listOf(
                arrayOf(CertificateType.GasSafetyCert, "A gas safety certificate", "gas safety certificate", "gas safety certificate"),
                arrayOf(CertificateType.Eicr, "An EICR", "Electrical Installation Condition Report (EICR)", "EICR"),
            )
    }

    @ParameterizedTest
    @MethodSource("certificateTestParameters")
    fun `handleCallback for monitoring email sends email to the monitoring team`(
        testType: CertificateType,
        expectedSubject: String,
        expectedHeading: String,
        expectedBody: String,
    ) {
        // Arrange
        val (ownershipId, expectedEmail) =
            arrangeOwnedPropertyUploadCallback(
                expectedSubject,
                expectedHeading,
                expectedBody,
                "test@example.com",
            )

        // Act
        val callbackData = OwnerEmailCallbackData(ownershipId, testType)
        val encodedCallbackData = Json.encodeToString(callbackData)
        virusCallbackHandler.handleCallback(
            VirusScanCallback(mock(), CallbackType.SendVirusMonitoringEmail, encodedCallbackData),
        )

        // Assert
        assertEmailSentToAddress(virusMonitoringEmail, expectedEmail)
    }

    @ParameterizedTest
    @MethodSource("certificateTestParameters")
    fun `handleCallback for send owner email sends email to landlord`(
        testType: CertificateType,
        expectedSubject: String,
        expectedHeading: String,
        expectedBody: String,
    ) {
        // Arrange
        val landlordEmail = "landlord@example.com"
        val (ownershipId, expectedEmail) =
            arrangeOwnedPropertyUploadCallback(
                expectedSubject,
                expectedHeading,
                expectedBody,
                landlordEmail,
            )

        // Act
        val callbackData = OwnerEmailCallbackData(ownershipId, testType)
        val encodedCallbackData = Json.encodeToString(callbackData)
        virusCallbackHandler.handleCallback(
            VirusScanCallback(mock(), CallbackType.SendEmailToOwner, encodedCallbackData),
        )

        // Assert
        assertEmailSentToAddress(landlordEmail, expectedEmail)
    }

    private fun arrangeOwnedPropertyUploadCallback(
        subjectCertificateType: String,
        headingCertificateType: String,
        bodyCertificateType: String,
        emailAddress: String,
    ): Pair<Long, VirusScanUnsuccessfulEmail> {
        val registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 37L)

        val ownership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = MockLandlordData.createLandlord(email = emailAddress),
                address = MockLandlordData.createAddress(singleLineAddress = "123 Main St, Anytown"),
                registrationNumber = RegistrationNumber(registrationNumber.type, registrationNumber.number),
            )

        val complianceUri = URI("http://example.com/compliance/1")
        whenever(absoluteUrlProvider.buildComplianceInformationUri(ownership.id)).thenReturn(complianceUri)

        whenever(propertyOwnershipRepository.findByIdAndIsActiveTrue(ownership.id)).thenReturn(ownership)

        return Pair(
            ownership.id,
            VirusScanUnsuccessfulEmail(
                subjectCertificateType,
                headingCertificateType,
                bodyCertificateType,
                "123 Main St, Anytown",
                registrationNumber.toString(),
                complianceUri,
            ),
        )
    }

    private fun assertEmailSentToAddress(
        emailAddress: String,
        expectedEmail: VirusScanUnsuccessfulEmail,
    ) {
        val emailModelCaptor = argumentCaptor<VirusScanUnsuccessfulEmail>()
        val emailAddressCaptor = argumentCaptor<String>()

        verify(emailNotificationService).sendEmail(emailAddressCaptor.capture(), emailModelCaptor.capture())

        assertEquals(expectedEmail, emailModelCaptor.firstValue)
        assertEquals(emailAddress, emailAddressCaptor.firstValue)
    }
}
