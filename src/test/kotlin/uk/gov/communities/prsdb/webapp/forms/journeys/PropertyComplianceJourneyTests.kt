package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class PropertyComplianceJourneyTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEpcLookupService: EpcLookupService

    private lateinit var journeyDataBuilder: JourneyDataBuilder

    private val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    private val principalName = "a-user-name"

    private val propertyOwnershipId = 1.toLong()

    private lateinit var testJourney: PropertyComplianceJourney

    @BeforeEach
    fun setup() {
        testJourney =
            PropertyComplianceJourney(
                validator = alwaysTrueValidator,
                journeyDataService = mockJourneyDataService,
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyOwnershipId = propertyOwnershipId,
                epcLookupService = mockEpcLookupService,
                principalName = principalName,
            )

        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Nested
    inner class EpcTests {
        private val uprn = 1324.toLong()

        @Test
        fun `submit updates epcDetails and checkMatchedEpc in journeyData and redirects to checkMatchedEpc if new EPC is found`() {
            val propertyOwnership =
                MockLandlordData
                    .createPropertyOwnership(
                        id = propertyOwnershipId,
                        property =
                            MockLandlordData.createProperty(
                                address =
                                    MockLandlordData.createAddress(
                                        uprn = uprn,
                                    ),
                            ),
                    )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val originalJourneyData =
                journeyDataBuilder
                    .withCheckMatchedEpcResult(true)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(expectedEpcDetails)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(mock())
                    .withEpcStatus(HasEpc.YES)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES))

            // Assert
            // setJourneyDataInSession gets called in Journey.completeStep and also in epcStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(2)).setJourneyDataInSession(anyOrNull())
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit does not update journeyData if it finds an EPC which is already in journeyData and redirects to checkMatchedEpc`() {
            val propertyOwnership =
                MockLandlordData
                    .createPropertyOwnership(
                        id = propertyOwnershipId,
                        property =
                            MockLandlordData.createProperty(
                                address =
                                    MockLandlordData.createAddress(
                                        uprn = uprn,
                                    ),
                            ),
                    )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(expectedEpcDetails)

            val originalJourneyData =
                journeyDataBuilder
                    .withCheckMatchedEpcResult(true)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
            // setJourneyDataInSession gets called in Journey.completeStep but not in epcStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(1)).setJourneyDataInSession(anyOrNull())
        }

        @Test
        fun `submit updates journeyData with looked up EPC results with null if no EPC is found and redirects to EpcNotAutoMatched`() {
            val propertyOwnership =
                MockLandlordData
                    .createPropertyOwnership(
                        id = propertyOwnershipId,
                        property =
                            MockLandlordData.createProperty(
                                address =
                                    MockLandlordData.createAddress(
                                        uprn = uprn,
                                    ),
                            ),
                    )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            val expectedUpdatedJourneyData = journeyDataBuilder.withEpcStatus(HasEpc.YES).build()

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcNotAutoMatched.urlPathSegment}", redirectModelAndView.viewName)
            // setJourneyDataInSession gets called in Journey.completeStep and also in epcLookupStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(2)).setJourneyDataInSession(anyOrNull())
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `submit redirects to EpcMissing if hasCert value is NO`() {
            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.NO))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcMissing.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcExemptionReason if hasCert value is NOT_REQUIRED`() {
            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.NOT_REQUIRED))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcExemptionReason.urlPathSegment}", redirectModelAndView.viewName)
        }
    }

    @Nested
    inner class EpcLookupTests {
        @Test
        fun `handleAndSubmit updates journeyData with EPC details and resets checkMatchedEpc answer if a new EPC is found`() {
            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            val originalJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withCheckMatchedEpcResult(false)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(mock())
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER))

            // Assert
            // setJourneyDataInSession gets called in Journey.completeStep and also in epcLookupStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(2)).setJourneyDataInSession(anyOrNull())
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit does not update journeyData if it finds an EPC which is already in journeyData`() {
            // Arrange
            val epcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(epcDetails)

            val originalJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withCheckMatchedEpcResult(false)
                    .withLookedUpEpcDetails(epcDetails)
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER))

            // Assert
            // setJourneyDataInSession gets called in Journey.completeStep but not in epcLookupStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(1)).setJourneyDataInSession(anyOrNull())
        }

        @Test
        fun `handleAndSubmit updates journeyData with looked up EPC results with null if no EPC is found`() {
            val originalJourneyData =
                journeyDataBuilder
                    .withCheckMatchedEpcResult(false)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(CURRENT_EPC_CERTIFICATE_NUMBER))
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(mock())
                    .withEpcLookupCertificateNumber(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
                    .withNullLookedUpEpcDetails()
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to NONEXISTENT_EPC_CERTIFICATE_NUMBER))

            // Assert
            // setJourneyDataInSession gets called in Journey.completeStep and also in epcLookupStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(2)).setJourneyDataInSession(anyOrNull())
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit redirects to checkMatchedEpc if the looked up EPC is found and is the latest available`() {
            // Arrange
            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )

            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            // Act
            val redirectModelAndView =
                completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `nextAction returns null if the looked up EPC is found and is the latest available`() {
            // Arrange
            val epcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(epcDetails)
                    .build()

            // Act, Assert
            assertNull(callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcLookup, updatedJourneyData))
        }

        @Test
        fun `nextAction returns EpcSuperseded if the looked up EPC is not the latest available`() {
            // Arrange
            val epcDetails =
                EpcDataModel(
                    certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(epcDetails)
                    .build()

            // Act, Assert
            assertEquals(
                PropertyComplianceStepId.EpcSuperseded,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcLookup, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns EpcNotFound if the looked up EPC is not found`() {
            // Arrange
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
                    .withNullLookedUpEpcDetails()
                    .build()

            // Act, Assert
            assertEquals(
                PropertyComplianceStepId.EpcNotFound,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcLookup, updatedJourneyData),
            )
        }
    }

    private fun completeStep(
        stepId: PropertyComplianceStepId,
        pageData: PageData = mapOf(),
    ): ModelAndView =
        testJourney.completeStep(
            stepPathSegment = stepId.urlPathSegment,
            formData = pageData,
            subPageNumber = null,
            principal = mock(),
        )

    private fun callNextActionAndReturnNextStepId(
        currentStepId: PropertyComplianceStepId,
        journeyData: JourneyData,
    ): PropertyComplianceStepId? =
        testJourney
            .sections
            .flatMap { section -> section.tasks }
            .flatMap { task -> task.steps }
            .single { it.id == currentStepId }
            .nextAction
            .invoke(journeyData, null)
            .first
}
