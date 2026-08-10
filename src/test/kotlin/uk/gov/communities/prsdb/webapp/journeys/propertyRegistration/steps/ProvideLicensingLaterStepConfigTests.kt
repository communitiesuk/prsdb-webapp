package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideLicensingLaterStepConfigTests {
    @Mock
    lateinit var mockState: LicensingState

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/provideLicensingLaterOccupiedForm", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/provideLicensingLaterUnoccupiedForm", result)
    }

    @Test
    fun `chooseTemplate throws IllegalStateException when isOccupied is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(null)

        assertThrows<IllegalStateException> { stepConfig.chooseTemplate(mockState) }
    }

    private fun setupStepConfig(): ProvideLicensingLaterStepConfig {
        val stepConfig = ProvideLicensingLaterStepConfig()
        stepConfig.urlPath = ProvideLicensingLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
