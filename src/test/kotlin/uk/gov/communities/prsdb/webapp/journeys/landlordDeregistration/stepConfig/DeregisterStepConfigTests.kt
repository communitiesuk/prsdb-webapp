package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordWithPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class DeregisterStepConfigTests {
    @Mock
    lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockConfirmationWithPropertiesEmailSender:
        EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockConfirmationWithNoPropertiesEmailSender:
        EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockState: LandlordDeregistrationJourneyState

    private val baseUserId = "test-user-id"
    private val landlordEmail = "landlord@example.com"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `afterStepIsReached deregisters the landlord`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).deregisterLandlord(baseUserId)
    }

    @Test
    fun `afterStepIsReached sets session marker to true when landlord has properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(true)
    }

    @Test
    fun `afterStepIsReached sends with-properties confirmation email when landlord has properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockConfirmationWithPropertiesEmailSender).sendEmail(
            eq(landlordEmail),
            any<LandlordWithPropertiesDeregistrationConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached does not send no-properties email when landlord has properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockConfirmationWithNoPropertiesEmailSender, never()).sendEmail(
            any(),
            any<LandlordNoPropertiesDeregistrationConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached sets session marker to false when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(false)
    }

    @Test
    fun `afterStepIsReached sends no-properties confirmation email when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockConfirmationWithNoPropertiesEmailSender).sendEmail(
            eq(landlordEmail),
            any<LandlordNoPropertiesDeregistrationConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached does not send with-properties email when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockConfirmationWithPropertiesEmailSender, never()).sendEmail(
            any(),
            any<LandlordWithPropertiesDeregistrationConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepIsReached refreshes security context`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepIsReached(mockState)

        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        val stepConfig = setupStepConfig()

        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("/test"))

        verify(mockState).deleteJourney()
    }

    private fun setupMocksForWithProperties() {
        JourneyTestHelper.setMockUser(baseUserId)
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(mockPropertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId))
            .thenReturn(listOf(propertyOwnership))
    }

    private fun setupMocksForNoProperties() {
        JourneyTestHelper.setMockUser(baseUserId)
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
        whenever(mockPropertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId))
            .thenReturn(emptyList())
    }

    private fun setupStepConfig(): DeregisterStepConfig =
        DeregisterStepConfig(
            mockLandlordDeregistrationService,
            mockLandlordService,
            mockPropertyOwnershipService,
            mockSecurityContextService,
            mockConfirmationWithPropertiesEmailSender,
            mockConfirmationWithNoPropertiesEmailSender,
        )
}
