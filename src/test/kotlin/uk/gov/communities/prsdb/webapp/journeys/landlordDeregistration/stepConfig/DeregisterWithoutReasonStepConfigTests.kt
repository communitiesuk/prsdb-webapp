package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class DeregisterWithoutReasonStepConfigTests {
    @Mock
    lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>

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
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).deregisterLandlord(baseUserId)
    }

    @Test
    fun `afterStepIsReached sets session marker to false`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(false)
    }

    @Test
    fun `afterStepIsReached sends no-properties confirmation email`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockConfirmationEmailSender).sendEmail(eq(landlordEmail), any<LandlordNoPropertiesDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepIsReached refreshes security context`() {
        val stepConfig = setupStepConfig()
        setupMocks()

        stepConfig.afterStepIsReached(mockState)

        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        val stepConfig = setupStepConfig()

        stepConfig.resolveNextDestination(mockState, uk.gov.communities.prsdb.webapp.journeys.Destination.ExternalUrl("/test"))

        verify(mockState).deleteJourney()
    }

    private fun setupMocks() {
        JourneyTestHelper.setMockUser(baseUserId)
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
    }

    private fun setupStepConfig(): DeregisterWithoutReasonStepConfig =
        DeregisterWithoutReasonStepConfig(
            mockLandlordDeregistrationService,
            mockLandlordService,
            mockSecurityContextService,
            mockConfirmationEmailSender,
        )
}
