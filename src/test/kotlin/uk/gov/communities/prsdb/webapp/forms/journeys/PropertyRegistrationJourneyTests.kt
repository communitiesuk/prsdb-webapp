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
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class PropertyRegistrationJourneyTests {
    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    lateinit var localAuthorityService: LocalAuthorityService

    @Mock
    lateinit var addressLookupService: AddressLookupService

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockPropertyRegistrationService = mock()
        localAuthorityService = mock()
        addressLookupService = mock()
    }

    @Nested
    inner class HandleAndSubmitTests {
        private lateinit var testJourney: PropertyRegistrationJourney

        private val principalName = "a-user-name"

        @BeforeEach
        fun beforeEach() {
            whenever(
                mockPropertyRegistrationService.registerProperty(
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
                )
            JourneyTestHelper.setMockUser(principalName)
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
            completeStep(RegisterPropertyStepId.CheckAnswers)

            // Assert
            verify(mockPropertyRegistrationService).registerProperty(
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
            completeStep(RegisterPropertyStepId.CheckAnswers)

            // Assert
            verify(mockPropertyRegistrationService).registerProperty(
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
                    .withLicensing(LicensingType.SELECTIVE_LICENCE, LicensingType.SELECTIVE_LICENCE.toString())
                    .withLicensing(
                        LicensingType.HMO_MANDATORY_LICENCE,
                        LicensingType.HMO_MANDATORY_LICENCE.toString(),
                    ).build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.CheckAnswers)

            // Assert
            verify(mockPropertyRegistrationService).registerProperty(
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
                    .withLicensing(LicensingType.SELECTIVE_LICENCE, LicensingType.SELECTIVE_LICENCE.toString())
                    .withLicensing(LicensingType.NO_LICENSING)
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            completeStep(RegisterPropertyStepId.CheckAnswers)

            // Assert
            verify(mockPropertyRegistrationService).registerProperty(
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
                formData = pageData,
                subPageNumber = null,
                principal = mock(),
            )
        }
    }
}
