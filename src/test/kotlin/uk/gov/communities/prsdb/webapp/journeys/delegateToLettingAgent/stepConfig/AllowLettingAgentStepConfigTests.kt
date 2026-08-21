package uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyState
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentEmailService
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AllowLettingAgentStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockDelegateToLettingAgentService: DelegateToLettingAgentService

    @Mock
    lateinit var mockDelegateToLettingAgentEmailService: DelegateToLettingAgentEmailService

    @Mock
    lateinit var mockJourneyState: DelegateToLettingAgentJourneyState

    @Mock
    lateinit var mockLandlord: Landlord

    private fun createStepConfig() =
        AllowLettingAgentStepConfig(mockUserToLandlordService, mockDelegateToLettingAgentService, mockDelegateToLettingAgentEmailService).apply {
            urlPath = AllowLettingAgentStep.ROUTE_SEGMENT
            validator = AlwaysTrueValidator()
        }

    @Test
    fun `enrichSubmittedDataBeforeValidation injects the landlord email`() {
        val stepConfig = createStepConfig()

        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(mockLandlord)
        whenever(mockLandlord.email).thenReturn("landlord@example.com")

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockJourneyState, emptyMap())

        assertEquals("landlord@example.com", result["landlordEmail"])
    }

    @Test
    fun `chooseTemplate returns the allow letting agent form template`() {
        val stepConfig = createStepConfig()

        assertEquals("forms/allowLettingAgentForm", stepConfig.chooseTemplate(mockJourneyState))
    }

    @Test
    fun `afterStepDataIsAdded stores the submitted email address against the property in the session`() {
        val stepConfig = createStepConfig()

        whenever(mockJourneyState.getStepData(AllowLettingAgentStep.ROUTE_SEGMENT))
            .thenReturn(mapOf("emailAddress" to "agent@example.com"))
        whenever(mockJourneyState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        verify(mockDelegateToLettingAgentService).addDelegatedLettingAgentToSession(PROPERTY_OWNERSHIP_ID, "agent@example.com")
    }

    @Test
    fun `afterStepDataIsAdded sends delegation emails`() {
        val stepConfig = createStepConfig()

        whenever(mockJourneyState.getStepData(AllowLettingAgentStep.ROUTE_SEGMENT))
            .thenReturn(mapOf("emailAddress" to "agent@example.com"))
        whenever(mockJourneyState.propertyOwnershipId).thenReturn(PROPERTY_OWNERSHIP_ID)

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        verify(mockDelegateToLettingAgentEmailService).sendDelegationEmails(PROPERTY_OWNERSHIP_ID, "agent@example.com")
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        val stepConfig = createStepConfig()
        val defaultDestination = Destination.ExternalUrl("/landlord/property-details/$PROPERTY_OWNERSHIP_ID")

        val result = stepConfig.resolveNextDestination(mockJourneyState, defaultDestination)

        verify(mockJourneyState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 1L
    }
}
