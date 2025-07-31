package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.time.LocalDate
import kotlin.String
import kotlin.test.assertNotEquals

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
    inner class UpdateGasSafetyStepTests {
        @Test
        fun `submit redirects to IssueDate if hasNewCertificate is true`() {
            // Arrange
            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateGasSafety,
                    pageData = mapOf("hasNewCertificate" to true),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment}",
                redirectModelAndView.viewName,
            )
        }

        @Test
        fun `submit redirects to ExemptionReason if hasNewCertificate is false`() {
            // Arrange
            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateGasSafety,
                    pageData = mapOf("hasNewCertificate" to false),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment}",
                redirectModelAndView.viewName,
            )
        }

        @Test
        fun `can reach the gas safety missing page if certificate was originally missing`() {
            // Arrange
            val missingGasSafetyPropertyCompliance =
                MockPropertyComplianceData.createPropertyCompliance()

            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(missingGasSafetyPropertyCompliance)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyDataBuilder()
                    .withExistingCompliance()
                    .withNewGasSafetyCertStatus(null)
                    .build(),
            )

            // Act
            val redirectModelAndView =
                getModelAndViewForStep(
                    stepId = PropertyComplianceStepId.GasSafetyExemptionMissing,
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "forms/gasSafetyExemptionMissingForm",
                redirectModelAndView.viewName,
            )
        }

        @Test
        fun `submitting with a missing update value does not reach the gas safety missing step`() {
            // Arrange
            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                try {
                    completeStep(
                        stepId = PropertyComplianceStepId.UpdateGasSafety,
                        pageData = mapOf("hasNewCertificate" to null),
                        propertyOwnershipId = propertyOwnershipId,
                    )
                } catch (_: Exception) {
                    null
                }

            // Assert
            assertNotEquals(
                "redirect:${PropertyComplianceStepId.GasSafetyExemptionMissing.urlPathSegment}",
                redirectModelAndView?.viewName,
            )
        }
    }

    @Nested
    inner class UpdateComplianceRecordTests {
        @Test
        fun `complete check your gas safety answers submits a new certificate to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                PropertyComplianceUpdateModel(
                    gasSafetyCertUpdate =
                        GasSafetyCertUpdateModel(
                            s3Key = "property_${propertyOwnershipId}_gas_safety_certificate.png",
                            issueDate = LocalDate.now(),
                            engineerNum = "1234321",
                        ),
                )
            expectedUpdateModel.gasSafetyCertUpdate!!
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyDataBuilder()
                    .withExistingCompliance()
                    .withNewGasSafetyCertStatus(true)
                    .withGasSafetyIssueDate(expectedUpdateModel.gasSafetyCertUpdate.issueDate!!)
                    .withGasSafeEngineerNum(expectedUpdateModel.gasSafetyCertUpdate.engineerNum!!)
                    .withOriginalGasSafetyCertName("file.png")
                    .withGasSafetyCertUploadConfirmation()
                    .build(),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)).thenReturn(
                originalPropertyCompliance,
            )

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                    pageData = mapOf(),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)}",
                redirectModelAndView.viewName,
            )

            val updateCaptor = argumentCaptor<PropertyComplianceUpdateModel>()
            verify(mockPropertyComplianceService).updatePropertyCompliance(
                propertyOwnershipId = eq(propertyOwnershipId),
                update = updateCaptor.capture(),
                any(),
            )

            assertEquals(
                expectedUpdateModel,
                updateCaptor.firstValue,
            )
        }

        @Test
        fun `complete check your gas safety answers submits a new exemption to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                PropertyComplianceUpdateModel(
                    gasSafetyCertUpdate =
                        GasSafetyCertUpdateModel(
                            exemptionReason = GasSafetyExemptionReason.OTHER,
                            exemptionOtherReason = "Other reason for exemption",
                        ),
                )
            expectedUpdateModel.gasSafetyCertUpdate!!
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyDataBuilder()
                    .withExistingCompliance()
                    .withNewGasSafetyCertStatus(false)
                    .withGasSafetyCertExemptionReason(expectedUpdateModel.gasSafetyCertUpdate.exemptionReason!!)
                    .withGasSafetyCertExemptionOtherReason(expectedUpdateModel.gasSafetyCertUpdate.exemptionOtherReason!!)
                    .withGasSafetyCertExemptionConfirmation()
                    .build(),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                    pageData = mapOf(),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)}",
                redirectModelAndView.viewName,
            )

            val updateCaptor = argumentCaptor<PropertyComplianceUpdateModel>()
            verify(mockPropertyComplianceService).updatePropertyCompliance(
                propertyOwnershipId = eq(propertyOwnershipId),
                update = updateCaptor.capture(),
                any(),
            )

            assertEquals(
                expectedUpdateModel,
                updateCaptor.firstValue,
            )
        }
    }

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
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

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
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

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
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

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
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.UpdateEpc, mapOf("hasNewCertificate" to false))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcExemptionReason.urlPathSegment}", redirectModelAndView.viewName)
        }
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

    private fun getModelAndViewForStep(
        stepId: PropertyComplianceStepId,
        submittedPageData: PageData? = null,
        propertyOwnershipId: Long = 1L,
    ): ModelAndView =
        createPropertyComplianceUpdateJourney(propertyOwnershipId, stepName = stepId.urlPathSegment).getModelAndViewForStep(
            submittedPageData = submittedPageData,
        )
}
