package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
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

    private val propertyOwnershipId = 1L

    @Nested
    inner class LoadJourneyDataIfNotLoadedTests {
        @Test
        fun `when there is journey data in session, it's not loaded from the database`() {
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mapOf("any-key" to "any-value"))

            createPropertyComplianceJourney()

            org.mockito.kotlin
                .verify(mockJourneyDataService, never())
                .loadJourneyDataIntoSession(any<FormContext>())
        }

        @Test
        fun `when there isn't journey data in session, it's loaded from the database`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(emptyMap())
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

            createPropertyComplianceJourney(propertyOwnership.id)

            org.mockito.kotlin
                .verify(mockJourneyDataService)
                .loadJourneyDataIntoSession(propertyOwnership.incompleteComplianceForm!!)
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
                JourneyDataBuilder()
                    .withCheckMatchedEpcResult(true)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedEpcDetails = MockEpcData.createEpcDataModel()
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(expectedEpcDetails)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder()
                    .withEpcStatus(HasEpc.YES)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .withEpcNotAutomatched()
                    .withAllowCheckMatchedEpcToBeBypassed(false)
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

            val expectedEpcDetails = MockEpcData.createEpcDataModel()
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(expectedEpcDetails)

            val originalJourneyData =
                JourneyDataBuilder()
                    .withEpcStatus(HasEpc.YES)
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

            val expectedUpdatedJourneyData = JourneyDataBuilder().withEpcStatus(HasEpc.YES).build()

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
            // Arrange
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.NO))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcMissing.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcExemptionReason if hasCert value is NOT_REQUIRED`() {
            // Arrange
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

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
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )

            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            val originalJourneyData =
                JourneyDataBuilder()
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withCheckMatchedEpcResult(false)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder()
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .withAllowCheckMatchedEpcToBeBypassed(false)
                    .withEpcNotAutomatched()
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
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(epcDetails)

            val originalJourneyData =
                JourneyDataBuilder()
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
        fun `handleAndSubmit updates journeyData with looked up EPC results with null and bypasses checkMatchedEpc if no EPC is found`() {
            val originalJourneyData =
                JourneyDataBuilder()
                    .withCheckMatchedEpcResult(false)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(CURRENT_EPC_CERTIFICATE_NUMBER))
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder()
                    .withEpcLookupCertificateNumber(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
                    .withNullLookedUpEpcDetails()
                    .withEpcNotAutomatched()
                    .withAllowCheckMatchedEpcToBeBypassed(true)
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
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

            val expectedEpcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
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
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

            val epcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
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
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

            val epcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
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
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

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
        fun `checkAndSubmitHandleSubmitAndRedirect deletes the corresponding compliance form`() {
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))

            completeStep(PropertyComplianceStepId.CheckAndSubmit)

            org.mockito.kotlin
                .verify(mockPropertyOwnershipService)
                .deleteIncompleteComplianceForm(propertyOwnershipId)
        }
    }

    private fun createPropertyComplianceJourney(propertyOwnershipId: Long = 1L) =
        PropertyComplianceJourney(
            validator = AlwaysTrueValidator(),
            journeyDataService = mockJourneyDataService,
            propertyOwnershipService = mockPropertyOwnershipService,
            epcLookupService = mockEpcLookupService,
            propertyOwnershipId = propertyOwnershipId,
        )

    private fun completeStep(
        stepId: PropertyComplianceStepId,
        pageData: PageData = mapOf(),
    ): ModelAndView =
        createPropertyComplianceJourney(propertyOwnershipId).completeStep(
            stepPathSegment = stepId.urlPathSegment,
            formData = pageData,
            subPageNumber = null,
            principal = mock(),
        )

    private fun callNextActionAndReturnNextStepId(
        currentStepId: PropertyComplianceStepId,
        journeyData: JourneyData,
    ): PropertyComplianceStepId? =
        createPropertyComplianceJourney(propertyOwnershipId)
            .sections
            .flatMap { section -> section.tasks }
            .flatMap { task -> task.steps }
            .single { it.id == currentStepId }
            .nextAction
            .invoke(journeyData, null)
            .first
}
