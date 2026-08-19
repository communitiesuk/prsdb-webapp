package uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyState
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AllowLettingAgentStepConfigTests {
    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    lateinit var mockJourneyState: DelegateToLettingAgentJourneyState

    @Mock
    lateinit var mockLandlord: Landlord

    @Test
    fun `enrichSubmittedDataBeforeValidation injects the landlord email`() {
        val stepConfig = AllowLettingAgentStepConfig(mockUserToLandlordService)
        stepConfig.urlPath = AllowLettingAgentStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(mockLandlord)
        whenever(mockLandlord.email).thenReturn("landlord@example.com")

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockJourneyState, emptyMap())

        assertEquals("landlord@example.com", result["landlordEmail"])
    }

    @Test
    fun `chooseTemplate returns the allow letting agent form template`() {
        val stepConfig = AllowLettingAgentStepConfig(mockUserToLandlordService)

        assertEquals("forms/allowLettingAgentForm", stepConfig.chooseTemplate(mockJourneyState))
    }
}
