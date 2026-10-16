package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.WhoProvidesDetailsState
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class LettingAgentEmailStepConfigTests {
    @Mock
    lateinit var mockCurrentUserService: CurrentUserService

    @Mock
    lateinit var mockJourneyState: WhoProvidesDetailsState

    @Test
    fun `enrichSubmittedDataBeforeValidation injects the current user's email`() {
        val stepConfig = LettingAgentEmailStepConfig(mockCurrentUserService)
        stepConfig.urlPath = LettingAgentEmailStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn("landlord@example.com")

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockJourneyState, emptyMap())

        assertEquals("landlord@example.com", result["currentUserEmail"])
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation injects the acting sub-user email for an organisational user`() {
        val stepConfig = LettingAgentEmailStepConfig(mockCurrentUserService)
        stepConfig.urlPath = LettingAgentEmailStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockCurrentUserService.getCurrentEmail()).thenReturn("sub-user@example.com")

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockJourneyState, emptyMap())

        assertEquals("sub-user@example.com", result["currentUserEmail"])
    }

    @Test
    fun `chooseTemplate returns the letting agent email form template`() {
        val stepConfig = LettingAgentEmailStepConfig(mockCurrentUserService)

        assertEquals("forms/lettingAgentEmailForm", stepConfig.chooseTemplate(mockJourneyState))
    }
}
