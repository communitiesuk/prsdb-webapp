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
            val expectedUpdatedJourneyData =
                journeyDataBuilder
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
