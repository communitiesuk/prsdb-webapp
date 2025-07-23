package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData

@ExtendWith(MockitoExtension::class)
class PropertyComplianceUpdateJourneyTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEpcLookupService: EpcLookupService

    private val propertyOwnershipId = 1L

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Nested
    inner class UpdateEpcStepTests {
        @Test
        fun `submit redirects to EpcNotAutomatched if hasNewCertificate is true and property does not have a uprn`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    id = propertyOwnershipId,
                    property =
                        MockLandlordData.createProperty(
                            address = MockLandlordData.createAddress(uprn = null),
                        ),
                )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
            whenever(mockPropertyComplianceService.getComplianceForProperty(propertyOwnershipId)).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.UpdateEpc, mapOf("hasNewCertificate" to true))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcNotAutoMatched.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to CheckAutoMatchedEpc if hasNewCertificate is true and automatched EPC details are in the session`() {
            // Arrange
            val uprn = 1324L
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    id = propertyOwnershipId,
                    property =
                        MockLandlordData.createProperty(
                            address = MockLandlordData.createAddress(uprn = uprn),
                        ),
                )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
            whenever(mockPropertyComplianceService.getComplianceForProperty(propertyOwnershipId)).thenReturn(originalPropertyCompliance)

            val automatchedEpcDetails = MockEpcData.createEpcDataModel()
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(automatchedEpcDetails)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.UpdateEpc, mapOf("hasNewCertificate" to true))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcNotAutomatched if hasNewCertificate is true and no automatched EPC details are found`() {
            // Arrange
            val uprn = 1324L
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    id = propertyOwnershipId,
                    property =
                        MockLandlordData.createProperty(
                            address = MockLandlordData.createAddress(uprn = uprn),
                        ),
                )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
            whenever(mockPropertyComplianceService.getComplianceForProperty(propertyOwnershipId)).thenReturn(originalPropertyCompliance)

            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(null)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.UpdateEpc, mapOf("hasNewCertificate" to true))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcNotAutoMatched.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcExemptionReason if hasNewCertificate is false`() {
            // Arrange
            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForProperty(propertyOwnershipId)).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.UpdateEpc, mapOf("hasNewCertificate" to false))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcExemptionReason.urlPathSegment}", redirectModelAndView.viewName)
        }

        private fun createPropertyComplianceUpdateJourney(
            propertyOwnershipId: Long = 1L,
            stepName: String,
        ) = PropertyComplianceUpdateJourney(
            validator = AlwaysTrueValidator(),
            journeyDataService = mockJourneyDataService,
            stepName = stepName,
            propertyOwnershipId = propertyOwnershipId,
            propertyOwnershipService = mockPropertyOwnershipService,
            propertyComplianceService = mockPropertyComplianceService,
            epcLookupService = mockEpcLookupService,
            epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
            checkingAnswersForStep = null,
        )

        private fun completeStep(
            stepId: PropertyComplianceStepId,
            pageData: PageData = mapOf(),
            propertyOwnershipId: Long = 1L,
        ): ModelAndView =
            createPropertyComplianceUpdateJourney(propertyOwnershipId, stepId.urlPathSegment).completeStep(
                stepPathSegment = stepId.urlPathSegment,
                formData = pageData,
                subPageNumber = null,
                principal = mock(),
            )
    }
}
