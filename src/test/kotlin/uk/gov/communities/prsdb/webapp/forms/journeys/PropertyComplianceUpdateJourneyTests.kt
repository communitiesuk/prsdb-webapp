package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toKotlinLocalDate
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
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
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
    inner class UpdateGasSafetyComplianceRecordTests {
        @Test
        fun `complete check your gas safety answers submits a new certificate to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                PropertyComplianceUpdateModel(
                    gasSafetyCertUpdate =
                        GasSafetyCertUpdateModel(
                            fileUploadId = 1L,
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
                    .withGasCertFileUploadId(expectedUpdateModel.gasSafetyCertUpdate.fileUploadId!!)
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
    inner class UpdateEICRStepTests {
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
                    stepId = PropertyComplianceStepId.UpdateEICR,
                    pageData = mapOf("hasNewCertificate" to true),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyComplianceStepId.EicrIssueDate.urlPathSegment}",
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
                    stepId = PropertyComplianceStepId.UpdateEICR,
                    pageData = mapOf("hasNewCertificate" to false),
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "redirect:${PropertyComplianceStepId.EicrExemptionReason.urlPathSegment}",
                redirectModelAndView.viewName,
            )
        }

        @Test
        fun `can reach the EICR missing page if certificate was originally missing`() {
            // Arrange
            val missingEicrPropertyCompliance =
                MockPropertyComplianceData.createPropertyCompliance()

            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(missingEicrPropertyCompliance)

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyPageDataBuilder.beforePropertyComplianceEicrUpdate(
                    gasSafetyIssueDate = LocalDate.now(),
                    gasSafeEngineerNumber = "1234567",
                    gasCertificatefileUploadId = 2L,
                )
                    .withNewEicrStatus(null)
                    .build(),
            )

            // Act
            val redirectModelAndView =
                getModelAndViewForStep(
                    stepId = PropertyComplianceStepId.EicrExemptionMissing,
                    propertyOwnershipId = propertyOwnershipId,
                )

            // Assert
            assertEquals(
                "forms/eicrExemptionMissingForm",
                redirectModelAndView.viewName,
            )
        }

        @Test
        fun `submitting with a missing update value does not reach the EICR missing step`() {
            // Arrange
            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                try {
                    completeStep(
                        stepId = PropertyComplianceStepId.UpdateEICR,
                        pageData = mapOf("hasNewCertificate" to null),
                        propertyOwnershipId = propertyOwnershipId,
                    )
                } catch (_: Exception) {
                    null
                }

            // Assert
            assertNotEquals(
                "redirect:${PropertyComplianceStepId.EicrExemptionMissing.urlPathSegment}",
                redirectModelAndView?.viewName,
            )
        }
    }

    @Nested
    inner class UpdateEICRComplianceRecordTests {
        @Test
        fun `complete check your EICR answers submits a new certificate to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                PropertyComplianceUpdateModel(
                    gasSafetyCertUpdate =
                        GasSafetyCertUpdateModel(
                            fileUploadId = 2L,
                            issueDate = LocalDate.now(),
                            engineerNum = "1234567",
                        ),
                    eicrUpdate =
                        EicrUpdateModel(
                            fileUploadId = 1L,
                            issueDate = LocalDate.now(),
                        ),
                )
            expectedUpdateModel.eicrUpdate!!
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyPageDataBuilder
                    .beforePropertyComplianceEicrUpdate(
                        gasSafetyIssueDate = LocalDate.now(),
                        gasSafeEngineerNumber = "1234567",
                        gasCertificatefileUploadId = 2L,
                    )
                    .withNewEicrStatus(true)
                    .withEicrIssueDate(expectedUpdateModel.eicrUpdate.issueDate!!)
                    .withEicrUploadId(expectedUpdateModel.eicrUpdate.fileUploadId!!)
                    .withEicrUploadConfirmation()
                    .build(),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)).thenReturn(
                originalPropertyCompliance,
            )

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
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
        fun `complete check your EICR answers submits a new exemption to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                PropertyComplianceUpdateModel(
                    gasSafetyCertUpdate =
                        GasSafetyCertUpdateModel(
                            fileUploadId = 2L,
                            issueDate = LocalDate.now(),
                            engineerNum = "1234567",
                        ),
                    eicrUpdate =
                        EicrUpdateModel(
                            exemptionReason = EicrExemptionReason.OTHER,
                            exemptionOtherReason = "Other reason for exemption",
                        ),
                )
            expectedUpdateModel.eicrUpdate!!
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                JourneyPageDataBuilder
                    .beforePropertyComplianceEicrUpdate(
                        gasSafetyIssueDate = LocalDate.now(),
                        gasSafeEngineerNumber = "1234567",
                        gasCertificatefileUploadId = 2L,
                    )
                    .withNewEicrStatus(false)
                    .withEicrExemptionReason(expectedUpdateModel.eicrUpdate.exemptionReason!!)
                    .withEicrExemptionOtherReason(expectedUpdateModel.eicrUpdate.exemptionOtherReason!!)
                    .withEicrExemptionConfirmation()
                    .build(),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(
                mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId),
            ).thenReturn(originalPropertyCompliance)

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
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

    @Nested
    inner class UpdateEpcComplianceRecordTests {
        @Test
        fun `complete check your EPC answers submits a new certificate to the database`() {
            // Arrange
            val epcUrl = "www.example-epc-url.com"
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(any())).thenReturn(epcUrl)

            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                createExpectedEpcUpdateModel(
                    url = epcUrl,
                    expiryDate = LocalDate.now().plusYears(1),
                    tenancyStartedBeforeExpiry = null,
                    energyRating = "C",
                    exemptionReason = null,
                    meesExemptionReason = null,
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                createJourneyDataForEpcUpdate(true, expectedUpdateModel.epcUpdate!!),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)).thenReturn(
                originalPropertyCompliance,
            )

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
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
        fun `complete check your EPC answers submits a new certificate and MEES exemption to the database`() {
            // Arrange
            val epcUrl = "www.example-epc-url.com"
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(any())).thenReturn(epcUrl)

            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                createExpectedEpcUpdateModel(
                    url = epcUrl,
                    expiryDate = LocalDate.now().plusYears(1),
                    tenancyStartedBeforeExpiry = null,
                    energyRating = "G",
                    exemptionReason = null,
                    meesExemptionReason = MeesExemptionReason.WALL_INSULATION,
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                createJourneyDataForEpcUpdate(true, expectedUpdateModel.epcUpdate!!),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)).thenReturn(
                originalPropertyCompliance,
            )

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
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
        fun `complete check your EPC answers submits a new EPC exemption to the database`() {
            // Arrange
            val propertyOwnershipId = 123L
            val expectedUpdateModel =
                createExpectedEpcUpdateModel(
                    url = null,
                    expiryDate = null,
                    tenancyStartedBeforeExpiry = null,
                    energyRating = null,
                    exemptionReason = EpcExemptionReason.LISTED_BUILDING,
                    meesExemptionReason = null,
                )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                createJourneyDataForEpcUpdate(false, expectedUpdateModel.epcUpdate!!),
            )

            val originalPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
            whenever(mockPropertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)).thenReturn(
                originalPropertyCompliance,
            )

            // Act
            val redirectModelAndView =
                completeStep(
                    stepId = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
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

    private fun createExpectedEpcUpdateModel(
        url: String?,
        expiryDate: LocalDate?,
        tenancyStartedBeforeExpiry: Boolean?,
        energyRating: String?,
        exemptionReason: EpcExemptionReason?,
        meesExemptionReason: MeesExemptionReason?,
    ) = PropertyComplianceUpdateModel(
        gasSafetyCertUpdate =
            GasSafetyCertUpdateModel(
                fileUploadId = 2L,
                issueDate = LocalDate.now(),
                engineerNum = "1234567",
            ),
        eicrUpdate =
            EicrUpdateModel(
                fileUploadId = 1L,
                issueDate = LocalDate.now(),
            ),
        epcUpdate =
            EpcUpdateModel(
                url,
                expiryDate,
                tenancyStartedBeforeExpiry,
                energyRating,
                exemptionReason,
                meesExemptionReason,
            ),
    )

    private fun createJourneyDataForEpcUpdate(
        newEpcStatus: Boolean,
        epcUpdateModel: EpcUpdateModel,
    ): JourneyData {
        return if (newEpcStatus && epcUpdateModel.meesExemptionReason != null) {
            JourneyPageDataBuilder
                .beforePropertyComplianceEpcUpdate(
                    gasSafetyIssueDate = LocalDate.now(),
                    gasSafeEngineerNumber = "1234567",
                    gasCertificatefileUploadId = 2L,
                    eicrIssueDate = LocalDate.now(),
                    eicrFileUploadId = 1L,
                )
                .withNewEpcStatus(newEpcStatus)
                .withAutoMatchedEpcDetails(
                    epcDetails =
                        MockEpcData.createEpcDataModel(
                            expiryDate = epcUpdateModel.expiryDate!!.toKotlinLocalDate(),
                            energyRating = epcUpdateModel.energyRating!!,
                        ),
                )
                .withCheckAutoMatchedEpcResult(true)
                .withMeesExemptionCheckStep(true)
                .withMeesExemptionReasonStep(epcUpdateModel.meesExemptionReason!!)
                .withMeesExemptionConfirmationStep()
                .build()
        } else if (newEpcStatus) {
            JourneyPageDataBuilder
                .beforePropertyComplianceEpcUpdate(
                    gasSafetyIssueDate = LocalDate.now(),
                    gasSafeEngineerNumber = "1234567",
                    gasCertificatefileUploadId = 2L,
                    eicrIssueDate = LocalDate.now(),
                    eicrFileUploadId = 1L,
                )
                .withNewEpcStatus(newEpcStatus)
                .withAutoMatchedEpcDetails(
                    epcDetails =
                        MockEpcData.createEpcDataModel(
                            expiryDate = epcUpdateModel.expiryDate!!.toKotlinLocalDate(),
                            energyRating = epcUpdateModel.energyRating!!,
                        ),
                )
                .withCheckAutoMatchedEpcResult(true)
                .build()
        } else {
            JourneyPageDataBuilder
                .beforePropertyComplianceEpcUpdate(
                    gasSafetyIssueDate = LocalDate.now(),
                    gasSafeEngineerNumber = "1234567",
                    gasCertificatefileUploadId = 2L,
                    eicrIssueDate = LocalDate.now(),
                    eicrFileUploadId = 1L,
                )
                .withNewEpcStatus(newEpcStatus)
                .withEpcExemptionReason(epcUpdateModel.exemptionReason!!)
                .withEpcExemptionConfirmationStep()
                .build()
        }
    }
}
