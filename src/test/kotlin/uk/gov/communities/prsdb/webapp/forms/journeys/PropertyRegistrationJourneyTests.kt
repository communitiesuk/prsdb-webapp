package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

class PropertyRegistrationJourneyTests : JourneyTest() {
    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    lateinit var localAuthorityService: LocalAuthorityService

    @Mock
    lateinit var landlordService: LandlordService

    @Mock
    lateinit var addressLookupService: AddressLookupService

    @Mock
    lateinit var confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>

    @Mock
    lateinit var urlProvider: AbsoluteUrlProvider

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockPropertyRegistrationService = mock()
        localAuthorityService = mock()
        landlordService = mock()
        addressLookupService = mock()
        confirmationEmailSender = mock()
        urlProvider = mock()
    }

    @Nested
    inner class HandleAndSubmitTests {
        private lateinit var testJourney: PropertyRegistrationJourney

        private val principalName = "a-user-name"

        @BeforeEach
        fun beforeEach() {
            whenever(landlordService.retrieveLandlordByBaseUserId(any())).thenReturn(MockLandlordData.createLandlord())
            whenever(urlProvider.buildLandlordDashboardUri()).thenReturn(URI("https:gov.uk"))

            whenever(
                mockPropertyRegistrationService.registerPropertyAndReturnPropertyRegistrationNumber(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(RegistrationNumber(RegistrationNumberType.PROPERTY, 57))

            testJourney =
                PropertyRegistrationJourney(
                    validator = alwaysTrueValidator,
                    journeyDataService = mockJourneyDataService,
                    addressLookupService = addressLookupService,
                    propertyRegistrationService = mockPropertyRegistrationService,
                    localAuthorityService = localAuthorityService,
                    landlordService = landlordService,
                    confirmationEmailSender = confirmationEmailSender,
                    absoluteUrlProvider = urlProvider,
                    principalName = principalName,
                )
            setMockUser(principalName)
        }

        @Test
        fun `when tenants are added in the form but the property is not occupied, they are not saved to the database`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .propertyDefault(localAuthorityService)
                    .withTenants(3, 7)
                    .withOccupiedSetToFalse()
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.Declaration)

            // Assert
            verify(mockPropertyRegistrationService).registerPropertyAndReturnPropertyRegistrationNumber(
                any(),
                any(),
                any(),
                any(),
                any(),
                argThat { households -> households == 0 },
                argThat { tenants -> tenants == 0 },
                any(),
            )
        }

        @Test
        fun `when a custom property type is chosen and then replaced, the custom type is not saved to the database`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .propertyDefault(localAuthorityService)
                    .withPropertyType(PropertyType.OTHER, "Bungalow")
                    .withPropertyType(PropertyType.FLAT)
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.Declaration)

            // Assert
            verify(mockPropertyRegistrationService).registerPropertyAndReturnPropertyRegistrationNumber(
                any(),
                argThat { type -> type == PropertyType.FLAT },
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }

        @Test
        fun `when a licence is added and then the licence type is changed, only the correct licence number is saved`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .propertyDefault(localAuthorityService)
                    .withLicensingType(LicensingType.SELECTIVE_LICENCE, LicensingType.SELECTIVE_LICENCE.toString())
                    .withLicensingType(
                        LicensingType.HMO_MANDATORY_LICENCE,
                        LicensingType.HMO_MANDATORY_LICENCE.toString(),
                    ).build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.Declaration)

            // Assert
            verify(mockPropertyRegistrationService).registerPropertyAndReturnPropertyRegistrationNumber(
                any(),
                any(),
                argThat { type -> type == LicensingType.HMO_MANDATORY_LICENCE },
                argThat { licenceNumber -> licenceNumber == LicensingType.HMO_MANDATORY_LICENCE.toString() },
                any(),
                any(),
                any(),
                any(),
            )
        }

        @Test
        fun `when a licence is added and then licensing is removed, no licence number is saved`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .propertyDefault(localAuthorityService)
                    .withLicensingType(LicensingType.SELECTIVE_LICENCE, LicensingType.SELECTIVE_LICENCE.toString())
                    .withLicensingType(LicensingType.NO_LICENSING)
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.Declaration)

            // Assert
            verify(mockPropertyRegistrationService).registerPropertyAndReturnPropertyRegistrationNumber(
                any(),
                any(),
                argThat { type -> type == LicensingType.NO_LICENSING },
                argThat { licenceNumber -> licenceNumber.isNullOrBlank() },
                any(),
                any(),
                any(),
                any(),
            )
        }

        private fun completeStep(
            stepId: RegisterPropertyStepId,
            pageData: PageData = mapOf(),
        ) {
            testJourney.completeStep(
                stepPathSegment = stepId.urlPathSegment,
                pageData = pageData,
                subPageNumber = null,
                principal = mock(),
            )
        }
    }
}
