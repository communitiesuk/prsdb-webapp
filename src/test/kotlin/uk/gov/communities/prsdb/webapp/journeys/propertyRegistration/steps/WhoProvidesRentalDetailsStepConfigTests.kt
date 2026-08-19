package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.WhoProvidesDetailsState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class WhoProvidesRentalDetailsStepConfigTests {
    @Mock
    lateinit var mockState: WhoProvidesDetailsState

    private val routeSegment = WhoProvidesRentalDetailsStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns LANDLORD_PROVIDES when whoProvides is LANDLORD`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("whoProvides" to "LANDLORD"))

        val result = stepConfig.mode(mockState)

        assertEquals(WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES, result)
    }

    @Test
    fun `mode returns LETTING_AGENT_PROVIDES when whoProvides is LETTING_AGENT`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("whoProvides" to "LETTING_AGENT"))

        val result = stepConfig.mode(mockState)

        assertEquals(WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES, result)
    }

    private fun setupStepConfig(): WhoProvidesRentalDetailsStepConfig {
        val stepConfig = WhoProvidesRentalDetailsStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
