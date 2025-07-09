package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

class VirusAlertSenderTests {
    private lateinit var virusAlertSender: VirusAlertSender

    private lateinit var emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private val virusMonitoringEmail = "support@example.com"

    @BeforeEach
    fun setup() {
        emailNotificationService = mock()
        absoluteUrlProvider = mock()
        virusAlertSender = VirusAlertSender(emailNotificationService, absoluteUrlProvider, virusMonitoringEmail)
    }

    @Test
    fun `sendAlerts sends email to landlord and virus monitoring`() {
        // Arrange
        val fileNameInfo = PropertyFileNameInfo(1L, FileCategory.GasSafetyCert, "file.txt")
        val landlordEmail = "landlord@example.com"
        val registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 37L)

        val ownership =
            MockLandlordData.createPropertyOwnership(
                primaryLandlord = MockLandlordData.createLandlord(email = landlordEmail),
                property =
                    MockLandlordData.createProperty(
                        address = MockLandlordData.createAddress(singleLineAddress = "123 Main St, Anytown"),
                    ),
                registrationNumber = RegistrationNumber(registrationNumber.type, registrationNumber.number),
            )

        val complianceUri = URI("http://example.com/compliance/1")
        whenever(absoluteUrlProvider.buildComplianceInformationUri(ownership.id)).thenReturn(complianceUri)

        val expectedEmail =
            VirusScanUnsuccessfulEmail(
                "gas safety certificate",
                "gas compliance certificate",
                "123 Main St, Anytown",
                registrationNumber.toString(),
                complianceUri,
            )

        // Act
        virusAlertSender.sendAlerts(ownership, fileNameInfo)

        // Assert
        val emailModelCaptor = argumentCaptor<VirusScanUnsuccessfulEmail>()
        val emailAddressCaptor = argumentCaptor<String>()
        verify(emailNotificationService, times(2)).sendEmail(emailAddressCaptor.capture(), emailModelCaptor.capture())

        assertEquals(expectedEmail, emailModelCaptor.firstValue)
        assertEquals(landlordEmail, emailAddressCaptor.firstValue)

        assertEquals(expectedEmail, emailModelCaptor.secondValue)
        assertEquals(virusMonitoringEmail, emailAddressCaptor.secondValue)
    }
}
