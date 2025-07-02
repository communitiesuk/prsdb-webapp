package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.NONEXISTENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailBulletPointList
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyPageDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.net.URI
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

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    private val mockMessageSource = MockMessageSource()

    @Mock
    private lateinit var mockFullComplianceEmailService: EmailNotificationService<FullPropertyComplianceConfirmationEmail>

    @Mock
    private lateinit var mockPartialComplianceEmailService: EmailNotificationService<PartialPropertyComplianceConfirmationEmail>

    @Mock
    private lateinit var mockUrlProvider: AbsoluteUrlProvider

    @Nested
    inner class LoadJourneyDataIfNotLoadedTests {
        @Test
        fun `when there is journey data in session, it's not loaded from the database`() {
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(mapOf("any-key" to "any-value"))

            createPropertyComplianceJourney()

            verify(mockJourneyDataService, never())
                .loadJourneyDataIntoSession(any<FormContext>())
        }

        @Test
        fun `when there isn't journey data in session, it's loaded from the database`() {
            val propertyOwnership = MockLandlordData.createPropertyOwnership()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(emptyMap())
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

            createPropertyComplianceJourney(propertyOwnership.id)

            verify(mockJourneyDataService)
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
        fun `submit updates epcDetails in journeyData if getHasEPC value is YES`() {
            // Arrange
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

            val originalJourneyData = JourneyPageDataBuilder.beforePropertyComplianceEpc().build()
            whenever(mockJourneyDataService.getJourneyDataFromSession())
                .thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withEpcStatus(HasEpc.YES)
                    .withAutoMatchedEpcDetails(expectedEpcDetails)
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES), stubPropertyOwnership = false)

            // Assert
            // addToJourneyDataIntoSession gets called in Journey.completeStep and also in epcStepHandleSubmitAndRedirect
            verify(mockJourneyDataService, times(2)).addToJourneyDataIntoSession(anyOrNull())
            verify(mockJourneyDataService).addToJourneyDataIntoSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `submit redirects to EpcNotAutomatched (nextAction) if getHasEPC is YES the property does not have a uprn`() {
            // Arrange
            val propertyOwnership =
                MockLandlordData
                    .createPropertyOwnership(
                        id = propertyOwnershipId,
                        property =
                            MockLandlordData.createProperty(
                                address =
                                    MockLandlordData.createAddress(
                                        uprn = null,
                                    ),
                            ),
                    )
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            // Act
            val redirectModelAndView =
                completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES), stubPropertyOwnership = false)

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcNotAutoMatched.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to CheckAutomatchedEpc (nextAction) if hasCert is YES and automatched EPC details are in the session`() {
            // Arrange
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

            val automatchedEpcDetails = MockEpcData.createEpcDataModel()
            whenever(mockEpcLookupService.getEpcByUprn(uprn)).thenReturn(automatchedEpcDetails)

            // Act
            val redirectModelAndView =
                completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES), stubPropertyOwnership = false)

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckAutoMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcNotAutomatched if hasCert value is YES and automatched EPC details are not in the session`() {
            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.YES))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcNotAutoMatched.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcMissing (nextAction) if hasCert value is NO`() {
            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.NO))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcMissing.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `submit redirects to EpcExemptionReason (nextAction) if hasCert value is NOT_REQUIRED`() {
            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EPC, mapOf("hasCert" to HasEpc.NOT_REQUIRED))

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcExemptionReason.urlPathSegment}", redirectModelAndView.viewName)
        }
    }

    @Nested
    inner class EpcLookupTests {
        @Test
        fun `handleAndSubmit updates journeyData with EPC details`() {
            // Arrange
            val expectedEpcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            val originalJourneyData = JourneyPageDataBuilder.beforePropertyComplianceEpcLookup().build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .build()

            // Act
            completeStep(
                PropertyComplianceStepId.EpcLookup,
                mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER),
                stubPropertyOwnership = false,
            )

            // Assert
            verify(mockJourneyDataService).addToJourneyDataIntoSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit resets CheckMatchedEpc if a new EPC is found`() {
            // Arrange
            val expectedEpcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(CURRENT_EPC_CERTIFICATE_NUMBER))
                    .withCheckMatchedEpcResult(false)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(expectedEpcDetails)
                    .withResetCheckMatchedEpcResult()
                    .build()

            // Act
            completeStep(
                PropertyComplianceStepId.EpcLookup,
                mapOf("certificateNumber" to SUPERSEDED_EPC_CERTIFICATE_NUMBER),
                stubPropertyOwnership = false,
            )

            // Assert
            // addToJourneyDataIntoSession gets called in Journey.completeStep and when adding the looked up EPC details
            verify(mockJourneyDataService, times(2)).addToJourneyDataIntoSession(any())
            verify(mockJourneyDataService).addToJourneyDataIntoSession(expectedUpdatedJourneyData)
            // setJourneyDataInSession gets called when resetting CheckMatchedEpc
            verify(mockJourneyDataService).setJourneyDataInSession(any())
        }

        @Test
        fun `handleAndSubmit does not reset CheckMatchedEpc if it finds an EPC which is already in journeyData`() {
            // Arrange
            val epcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(epcDetails)

            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withEpcLookupCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
                    .withCheckMatchedEpcResult(false)
                    .withLookedUpEpcDetails(epcDetails)
                    .build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            // Act
            completeStep(
                PropertyComplianceStepId.EpcLookup,
                mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER),
                stubPropertyOwnership = false,
            )

            // Assert
            // addToJourneyDataIntoSession gets called to update the lookedUpEpc certificate number and the lookedUpEpc details
            verify(mockJourneyDataService, times(2)).addToJourneyDataIntoSession(any())
            // setJourneyDataInSession does not get called as CheckMatchedEpc is not reset
            verify(mockJourneyDataService, never()).setJourneyDataInSession(any())
        }

        @Test
        fun `handleAndSubmit updates journeyData with looked up EPC results with null if no EPC is found`() {
            // Arrange
            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withCheckMatchedEpcResult(false)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(CURRENT_EPC_CERTIFICATE_NUMBER))
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val expectedUpdatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withResetCheckMatchedEpcResult()
                    .withEpcLookupCertificateNumber(NONEXISTENT_EPC_CERTIFICATE_NUMBER)
                    .withNullLookedUpEpcDetails()
                    .build()

            // Act
            completeStep(
                PropertyComplianceStepId.EpcLookup,
                mapOf("certificateNumber" to NONEXISTENT_EPC_CERTIFICATE_NUMBER),
                stubPropertyOwnership = false,
            )

            // Assert
            verify(mockJourneyDataService).addToJourneyDataIntoSession(expectedUpdatedJourneyData)
        }

        @Test
        fun `handleAndSubmit redirects to checkMatchedEpc if the looked up EPC is found and is the latest available`() {
            // Arrange
            val expectedEpcDetails =
                MockEpcData.createEpcDataModel(
                    certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )

            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(expectedEpcDetails)

            // Act
            val redirectModelAndView =
                completeStep(
                    PropertyComplianceStepId.EpcLookup,
                    mapOf("certificateNumber" to CURRENT_EPC_CERTIFICATE_NUMBER),
                )

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `nextAction returns CheckMatchedEpc if the looked up EPC is found and is the latest available`() {
            // Arrange
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
            assertEquals(
                PropertyComplianceStepId.CheckMatchedEpc,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcLookup, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns EpcSuperseded if the looked up EPC is not the latest available`() {
            // Arrange
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
    inner class CheckAutoMatchedEpcTests {
        @Test
        fun `nextAction returns EpcLookup if matchedEpc is not correct`() {
            val updatedJourneyData = JourneyDataBuilder().withCheckAutoMatchedEpcResult(false).build()

            assertEquals(
                PropertyComplianceStepId.EpcLookup,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckAutoMatchedEpc, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns the first Landlord Responsibilities step the accepted EPC is in date with a high enough rating`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckAutoMatchedEpcResult(true)
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel())
                    .build()

            assertEquals(
                PropertyComplianceStepId.FireSafetyDeclaration,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckAutoMatchedEpc, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns the EPC Expiry Check if the accepted EPC is out of date`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckAutoMatchedEpcResult(true)
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(expiryDate = LocalDate(2020, 1, 1)))
                    .build()

            assertEquals(
                PropertyComplianceStepId.EpcExpiryCheck,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckAutoMatchedEpc, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns the MEES Exemption CHeck the accepted EPC is in date but has a low energy rating`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckAutoMatchedEpcResult(true)
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(energyRating = "F"))
                    .build()

            assertEquals(
                PropertyComplianceStepId.MeesExemptionCheck,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckAutoMatchedEpc, updatedJourneyData),
            )
        }
    }

    @Nested
    inner class CheckMatchedEpcTests {
        @Test
        fun `submit redirects to EpcLookup if matchedEpc is not correct`() {
            // Act
            val redirectModelAndView =
                completeStep(
                    PropertyComplianceStepId.CheckMatchedEpc,
                    mapOf("matchedEpcIsCorrect" to "false"),
                )

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.EpcLookup.urlPathSegment}", redirectModelAndView.viewName)
        }

        @Test
        fun `nextAction returns null if matchedEpc is not correct`() {
            val updatedJourneyData = JourneyDataBuilder().withCheckMatchedEpcResult(false).build()

            assertNull(callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckMatchedEpc, updatedJourneyData))
        }

        @Test
        fun `nextAction returns the first Landlord Responsibilities step the accepted EPC is in date with a high enough rating`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckMatchedEpcResult(true)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel())
                    .build()

            assertEquals(
                PropertyComplianceStepId.FireSafetyDeclaration,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckMatchedEpc, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns the EPC Expiry Check if the accepted EPC is out of date`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckMatchedEpcResult(true)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(expiryDate = LocalDate(2020, 1, 1)))
                    .build()

            assertEquals(
                PropertyComplianceStepId.EpcExpiryCheck,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckMatchedEpc, updatedJourneyData),
            )
        }

        @Test
        fun `nextAction returns the MEES Exemption CHeck the accepted EPC is in date but has a low energy rating`() {
            val updatedJourneyData =
                JourneyDataBuilder()
                    .withCheckMatchedEpcResult(true)
                    .withLookedUpEpcDetails(MockEpcData.createEpcDataModel(energyRating = "F"))
                    .build()

            assertEquals(
                PropertyComplianceStepId.MeesExemptionCheck,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.CheckMatchedEpc, updatedJourneyData),
            )
        }
    }

    @Nested
    inner class EpcSupersededTests {
        @Test
        fun `handleSubmitAndRedirect looks up the latest certificate by certificate number`() {
            // Arrange
            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(
                            certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                            latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                        ),
                    ).build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(MockEpcData.createEpcDataModel(certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER))

            // Act
            completeStep(PropertyComplianceStepId.EpcSuperseded, emptyMap(), stubPropertyOwnership = false)

            // Assert
            verify(mockEpcLookupService).getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER)
        }

        @Test
        fun `handleSubmitAndRedirect resets the CheckMatchedEpc in the session`() {
            // Arrange
            val supersededEPC =
                MockEpcData.createEpcDataModel(
                    certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                    latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                )

            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withCheckMatchedEpcResult(false)
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(supersededEPC)
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val updatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withResetCheckMatchedEpcResult()
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcSuperseded, emptyMap(), stubPropertyOwnership = false)

            // Assert
            verify(mockJourneyDataService).setJourneyDataInSession(updatedJourneyData)
        }

        @Test
        fun `handleSubmitAndRedirect updates the the looked up EPC details in the session`() {
            // Arrange
            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(
                            certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                            latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                        ),
                    ).build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)

            val latestEpc = MockEpcData.createEpcDataModel(certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER)

            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(latestEpc)

            val updatedJourneyData =
                JourneyDataBuilder(initialJourneyData = originalJourneyData)
                    .withEpcSuperseded()
                    .withLookedUpEpcDetails(latestEpc)
                    .build()

            // Act
            completeStep(PropertyComplianceStepId.EpcSuperseded, emptyMap(), stubPropertyOwnership = false)

            // Assert
            verify(mockJourneyDataService).addToJourneyDataIntoSession(updatedJourneyData)
        }

        @Test
        fun `handleSubmitAndRedirect redirects to CheckMatchedEpc`() {
            // Arrange
            val originalJourneyData =
                JourneyPageDataBuilder
                    .beforePropertyComplianceEpcLookup()
                    .withEpcLookupCertificateNumber(SUPERSEDED_EPC_CERTIFICATE_NUMBER)
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(SUPERSEDED_EPC_CERTIFICATE_NUMBER),
                    ).build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(originalJourneyData)
            whenever(mockEpcLookupService.getEpcByCertificateNumber(CURRENT_EPC_CERTIFICATE_NUMBER))
                .thenReturn(MockEpcData.createEpcDataModel(certificateNumber = CURRENT_EPC_CERTIFICATE_NUMBER))

            // Act
            val redirectModelAndView = completeStep(PropertyComplianceStepId.EpcSuperseded, emptyMap(), stubPropertyOwnership = false)

            // Assert
            assertEquals("redirect:${PropertyComplianceStepId.CheckMatchedEpc.urlPathSegment}", redirectModelAndView.viewName)
        }
    }

    @Nested
    inner class EpcExpiryCheckTests {
        @Test
        fun `nextAction returns EpcExpired if tenancyStartedBeforeExpiry is false`() {
            // Arrange
            val filteredJourneyData =
                JourneyDataBuilder()
                    .withEpcExpiryCheckStep(false)
                    .build()

            // Act, Assert
            assertEquals(
                PropertyComplianceStepId.EpcExpired,
                callNextActionAndReturnNextStepId(PropertyComplianceStepId.EpcExpiryCheck, filteredJourneyData),
            )
        }

        @Test
        fun `nextAction returns FireSafetyDeclaration if tenancyStartedBeforeExpiry is true and the energy rating is good`() {
            // Arrange
            val filteredJourneyData =
                JourneyDataBuilder()
                    .withCheckAutoMatchedEpcResult(true)
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(energyRating = "C"))
                    .withEpcExpiryCheckStep(true)
                    .build()

            // Act, Assert
            assertEquals(
                PropertyComplianceStepId.FireSafetyDeclaration,
                callNextActionAndReturnNextStepId(
                    PropertyComplianceStepId.EpcExpiryCheck,
                    filteredJourneyData,
                ),
            )
        }

        @Test
        fun `nextAction returns MeesExemptionCheck if tenancyStartedBeforeExpiry is true and the energy rating is poor`() {
            // Arrange
            val filteredJourneyData =
                JourneyDataBuilder()
                    .withEpcExpiryCheckStep(true)
                    .build()

            val sessionJourneyData =
                JourneyDataBuilder()
                    .withCheckAutoMatchedEpcResult(true)
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(energyRating = "F"))
                    .build()
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(sessionJourneyData)

            // Act, Assert
            assertEquals(
                PropertyComplianceStepId.MeesExemptionCheck,
                callNextActionAndReturnNextStepId(
                    PropertyComplianceStepId.EpcExpiryCheck,
                    filteredJourneyData,
                    stubPropertyOwnership = false,
                ),
            )
        }
    }

    @Nested
    inner class CheckAndSubmitHandleSubmitAndRedirectTests {
        private val expectedLandlordDashboardUri = URI("landlord-dashboard-uri")
        private val expectedComplianceInfoUri = URI("compliance-info-uri")

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `checkAndSubmitHandleSubmitAndRedirect creates a compliance record, deletes the corresponding form context and redirects to the confirmation page`() {
            val propertyOwnershipId = 1L
            setUpMocks()

            val returnedModelAndView =
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
                    anyOrNull(),
                )
            verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyOwnershipId)
            verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyOwnershipId)
            assertEquals("redirect:$CONFIRMATION_PATH_SEGMENT", returnedModelAndView.viewName)
        }

        @Test
        fun `checkAndSubmitHandleSubmitAndRedirect sends a fully compliant confirmation email for a compliant property`() {
            val compliantPropertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
            setUpMocks(compliantPropertyCompliance, isPropertyCompliant = true)

            val expectedCompliantMessageKeys = PropertyComplianceConfirmationMessageKeys(compliantPropertyCompliance).compliantMsgKeys
            val expectedCompliantMessages = expectedCompliantMessageKeys.map { MockMessageSource.getMockMessage(it) }

            val expectedEmailModel =
                FullPropertyComplianceConfirmationEmail(
                    compliantPropertyCompliance.propertyOwnership.property.address.singleLineAddress,
                    EmailBulletPointList(expectedCompliantMessages),
                    expectedLandlordDashboardUri.toString(),
                )

            completeStep(
                PropertyComplianceStepId.CheckAndSubmit,
                JourneyDataBuilder().withCheckedAnswers().build(),
                compliantPropertyCompliance.propertyOwnership.id,
                stubPropertyOwnership = false,
            )

            verify(mockFullComplianceEmailService)
                .sendEmail(compliantPropertyCompliance.propertyOwnership.primaryLandlord.email, expectedEmailModel)
        }

        @Test
        fun `checkAndSubmitHandleSubmitAndRedirect sends a partially compliant confirmation email for non-compliant property`() {
            val nonCompliantPropertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()
            setUpMocks(nonCompliantPropertyCompliance, isPropertyCompliant = false)

            val expectedMessageKeys = PropertyComplianceConfirmationMessageKeys(nonCompliantPropertyCompliance)
            val expectedCompliantMessages = expectedMessageKeys.compliantMsgKeys.map { MockMessageSource.getMockMessage(it) }
            val expectedNonCompliantMessages = expectedMessageKeys.nonCompliantMsgKeys.map { MockMessageSource.getMockMessage(it) }

            val expectedEmailModel =
                PartialPropertyComplianceConfirmationEmail(
                    nonCompliantPropertyCompliance.propertyOwnership.property.address.singleLineAddress,
                    EmailBulletPointList(expectedCompliantMessages),
                    EmailBulletPointList(expectedNonCompliantMessages),
                    expectedComplianceInfoUri.toString(),
                )

            completeStep(
                PropertyComplianceStepId.CheckAndSubmit,
                JourneyDataBuilder().withCheckedAnswers().build(),
                nonCompliantPropertyCompliance.propertyOwnership.id,
                stubPropertyOwnership = false,
            )

            verify(mockPartialComplianceEmailService)
                .sendEmail(nonCompliantPropertyCompliance.propertyOwnership.primaryLandlord.email, expectedEmailModel)
        }

        private fun setUpMocks(
            propertyCompliance: PropertyCompliance = PropertyComplianceBuilder.createWithInDateCerts(),
            isPropertyCompliant: Boolean = true,
        ) {
            whenever(mockJourneyDataService.getJourneyDataFromSession())
                .thenReturn(JourneyPageDataBuilder.beforePropertyComplianceCheckAnswers().build())

            whenever(
                mockPropertyComplianceService.createPropertyCompliance(
                    eq(propertyCompliance.propertyOwnership.id),
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
                    anyOrNull(),
                ),
            ).thenReturn(propertyCompliance)

            if (isPropertyCompliant) {
                whenever(mockUrlProvider.buildLandlordDashboardUri()).thenReturn(expectedLandlordDashboardUri)
            } else {
                whenever(mockUrlProvider.buildComplianceInformationUri(propertyCompliance.propertyOwnership.id))
                    .thenReturn(expectedComplianceInfoUri)
            }
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
            epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
            messageSource = mockMessageSource,
            fullPropertyComplianceConfirmationEmailService = mockFullComplianceEmailService,
            partialPropertyComplianceConfirmationEmailService = mockPartialComplianceEmailService,
            urlProvider = mockUrlProvider,
            isChangingAnswer = false,
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
        stubPropertyOwnership: Boolean = true,
    ): PropertyComplianceStepId? {
        if (stubPropertyOwnership) {
            whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
                .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyOwnershipId))
        }

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
