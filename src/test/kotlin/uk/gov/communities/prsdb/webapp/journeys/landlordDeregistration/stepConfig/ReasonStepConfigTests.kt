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
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordWithPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class ReasonStepConfigTests {
    @Mock
    lateinit var mockLandlordDeregistrationService: LandlordDeregistrationService

    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockSecurityContextService: SecurityContextService

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockNoPropertiesConfirmationEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockState: LandlordDeregistrationJourneyState

    private val baseUserId = "test-user-id"
    private val landlordEmail = "landlord@example.com"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `afterStepDataIsAdded deregisters the landlord`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockLandlordDeregistrationService).deregisterLandlord(baseUserId)
    }

    @Test
    fun `afterStepDataIsAdded sets session marker to true when landlord has properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(true)
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email when landlord has properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender).sendEmail(eq(landlordEmail), any<LandlordWithPropertiesDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepDataIsAdded sets session marker to false when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockLandlordDeregistrationService).addLandlordHadActivePropertiesToSession(false)
    }

    @Test
    fun `afterStepDataIsAdded does not send with-properties email when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockConfirmationEmailSender, never()).sendEmail(any(), any<LandlordWithPropertiesDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepDataIsAdded sends no-properties email when landlord has no properties`() {
        val stepConfig = setupStepConfig()
        setupMocksForNoProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(
            mockNoPropertiesConfirmationEmailSender,
        ).sendEmail(eq(landlordEmail), any<LandlordNoPropertiesDeregistrationConfirmationEmail>())
    }

    @Test
    fun `afterStepDataIsAdded refreshes security context`() {
        val stepConfig = setupStepConfig()
        setupMocksForWithProperties()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockSecurityContextService).refreshContext()
    }

    @Test
    fun `afterStepDataIsAdded deletes journey on resolveNextDestination`() {
        val stepConfig = setupStepConfig()

        stepConfig.resolveNextDestination(mockState, uk.gov.communities.prsdb.webapp.journeys.Destination.ExternalUrl("/test"))

        verify(mockState).deleteJourney()
    }

    private fun setupMocksForWithProperties() {
        JourneyTestHelper.setMockUser(baseUserId)
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(mockPropertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)).thenReturn(listOf(propertyOwnership))
    }

    private fun setupMocksForNoProperties() {
        JourneyTestHelper.setMockUser(baseUserId)
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(landlord)
        whenever(mockPropertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)).thenReturn(emptyList())
    }

    private fun setupStepConfig(): ReasonStepConfig {
        val stepConfig =
            ReasonStepConfig(
                mockLandlordDeregistrationService,
                mockLandlordService,
                mockPropertyOwnershipService,
                mockSecurityContextService,
                mockConfirmationEmailSender,
                mockNoPropertiesConfirmationEmailSender,
            )
        stepConfig.routeSegment = ReasonStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
