package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertContains

@ExtendWith(MockitoExtension::class)
class PropertyComplianceJourneyTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEpcLookupService: EpcLookupService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Nested
    inner class LoadJourneyDataIfNotLoadedTests {
        @Test
        fun `when there is journey data in session, it's not loaded from the database`() {
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mapOf("any-key" to "any-value"))

            createPropertyComplianceJourney()

            verify(mockJourneyDataService, never()).loadJourneyDataIntoSession(any<FormContext>())
        }

        @Test
        fun `when there isn't journey data in session, it's loaded from the database`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(emptyMap())
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

            createPropertyComplianceJourney(propertyOwnership.id)

            verify(mockJourneyDataService).loadJourneyDataIntoSession(propertyOwnership.incompleteComplianceForm!!)
        }

        @Test
        fun `when there isn't journey data in session or the database, an error is thrown`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership(incompleteComplianceForm = null)
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(emptyMap())
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

            val errorThrown = assertThrows<ResponseStatusException> { createPropertyComplianceJourney(propertyOwnership.id) }
            assertContains(errorThrown.message, "Property ownership ${propertyOwnership.id} does not have an incomplete compliance form")
        }
    }

    @Nested
    inner class EpcLookupTests {
        @Test
        fun `handleAndSubmit updates journeyData with looked up EPC results if an EPC is found`() {
            val expectedEpcDetails =
                EpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    singleLineAddress = "123 Test Street, Flat 1, Test Town, TT1 1TT",
                    energyRating = "C",
                    expiryDate = LocalDate(2027, 1, 5),
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            val expectedUpdatedJourneyData =
                JourneyDataBuilder()
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER))

            // Assert
            verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit updates journeyData with looked up EPC results with null if no EPC is found`() {
            val expectedUpdatedJourneyData =
                JourneyDataBuilder()
                    .withEpcLookupCertificateNumber(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
                    .withNullLookedUpEpcDetails()
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcLookup, mapOf("certificateNumber" to NONEXISTENT_EPC_CERTIFICATE_NUMBER))

            // Assert
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
                JourneyDataBuilder()
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
                JourneyDataBuilder()
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
                JourneyDataBuilder()
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

    @Nested
    inner class CheckAndSubmitHandleSubmitAndRedirectTests {
        @Test
        fun `checkAndSubmitHandleSubmitAndRedirect creates a compliance record and deletes the corresponding form context`() {
            val propertyOwnershipId = 1L
            whenever(mockJourneyDataService.getJourneyDataFromSession())
                .thenReturn(JourneyPageDataBuilder.beforePropertyComplianceCheckAnswers().build())

            completeStep(
                PropertyComplianceStepId.CheckAndSubmit,
                JourneyDataBuilder().withCheckedAnswers().build(),
                propertyOwnershipId,
                stubPropertyOwnership = false,
            )

            verify(mockPropertyComplianceService)
                .createPropertyCompliance(
                    eq(propertyOwnershipId),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                )
            verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyOwnershipId)
        }
    }

    private fun createPropertyComplianceJourney(propertyOwnershipId: Long = 1L) =
        PropertyComplianceJourney(
            validator = AlwaysTrueValidator(),
            journeyDataService = mockJourneyDataService,
            propertyOwnershipService = mockPropertyOwnershipService,
            epcLookupService = mockEpcLookupService,
            propertyComplianceService = mockPropertyComplianceService,
            propertyOwnershipId = propertyOwnershipId,
        )

    private fun completeStep(
        stepId: PropertyComplianceStepId,
        pageData: PageData = mapOf(),
        propertyOwnershipId: Long = 1L,
        stubPropertyOwnership: Boolean = true,
    ): ModelAndView {
        if (stubPropertyOwnership) {
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))
        }

        return createPropertyComplianceJourney(propertyOwnershipId).completeStep(
            stepPathSegment = stepId.urlPathSegment,
            formData = pageData,
            subPageNumber = null,
            principal = mock(),
        )
    }

    private fun callNextActionAndReturnNextStepId(
        currentStepId: PropertyComplianceStepId,
        journeyData: JourneyData,
        propertyOwnershipId: Long = 1L,
    ): PropertyComplianceStepId? {
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

        return createPropertyComplianceJourney(propertyOwnershipId)
            .sections
            .flatMap { section -> section.tasks }
            .flatMap { task -> task.steps }
            .single { it.id == currentStepId }
            .nextAction
            .invoke(journeyData, null)
            .first
    }
}
