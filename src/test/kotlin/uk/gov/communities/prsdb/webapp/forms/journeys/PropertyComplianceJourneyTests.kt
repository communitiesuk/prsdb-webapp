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
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
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
        fun `handleAndSubmit updates journeyData with looked up EPC results if an EPC is found`() {
            val validCertificateNumber = "0000-0000-0000-0554-8410"
            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = validCertificateNumber,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = validCertificateNumber,
                )
            val expectedUpdatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(validCertificateNumber)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()
            whenever(mockEpcLookupService.getEpcByCertificateNumber(validCertificateNumber))
                .thenReturn(expectedEpcDetails)

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to validCertificateNumber))

            // Assert
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit updates journeyData with looked up EPC results with null if no EPC is found`() {
            val nonExistentCertificateNumber = "0000-0000-0000-0554-8410"
            val expectedUpdatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(nonExistentCertificateNumber)
                    .withNullLookedUpEpcDetails()
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to nonExistentCertificateNumber))

            // Assert
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit redirects to checkMatchedEpc if the looked up EPC is found and is the latest available`() {
            // Arrange
            val currentCertificateNumber = "0000-0000-0000-0554-8410"
            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = currentCertificateNumber,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = currentCertificateNumber,
                )

            whenever(mockEpcLookupService.getEpcByCertificateNumber(currentCertificateNumber))
                .thenReturn(expectedEpcDetails)

            // Act
            val redirectModelAndView =
                completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to currentCertificateNumber))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `nextAction returns null if the looked up EPC is found and is the latest available`() {
            // Arrange
            val currentCertificateNumber = "0000-0000-0000-0554-8410"
            val epcDetails =
                EpcDataModel(
                    certificateNumber = currentCertificateNumber,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = currentCertificateNumber,
                )
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(currentCertificateNumber)
                    .withLookedUpEpcDetails(epcDetails)
                    .build()

            // Act, Assert
            assertNull(callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcLookup, updatedJourneyData))
        }

        @Test
        fun `nextAction returns EpcSuperseded if the looked up EPC is not the latest available`() {
            // Arrange
            val supersededCertificateNumber = "0000-0000-0000-0000-8410"
            val latestCertificateNumber = "0000-0000-0000-0554-8410"
            val epcDetails =
                EpcDataModel(
                    certificateNumber = supersededCertificateNumber,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = latestCertificateNumber,
                )
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(supersededCertificateNumber)
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
            val nonExistentCertificateNumber = "0000-0000-0000-0554-8410"
            val updatedJourneyData =
                journeyDataBuilder
                    .withEpcLookupCertificateNumber(nonExistentCertificateNumber)
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
