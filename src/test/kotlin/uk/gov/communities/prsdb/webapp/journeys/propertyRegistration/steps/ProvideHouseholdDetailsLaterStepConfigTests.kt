package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class ProvideHouseholdDetailsLaterStepConfigTests {
    @Test
    fun `chooseTemplate returns the households provide later template`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState())

        assertEquals("forms/provideHouseholdDetailsLaterForm", result)
    }

    private fun setupStepConfig(): ProvideHouseholdDetailsLaterStepConfig {
        val stepConfig = ProvideHouseholdDetailsLaterStepConfig()
        stepConfig.routeSegment = ProvideHouseholdDetailsLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun mockState(): HouseholdsAndTenantsState = org.mockito.kotlin.mock()
}
