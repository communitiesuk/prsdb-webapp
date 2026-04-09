package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EpcInDateAtStartOfTenancyCheckStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    private val routeSegment = EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns null when tenancyStartedBeforeExpiry is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(emptyMap())

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns IN_DATE when tenancyStartedBeforeExpiry is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to true))

        val result = stepConfig.mode(mockState)

        assertEquals(EpcInDateAtStartOfTenancyCheckMode.IN_DATE, result)
    }

    @Test
    fun `mode returns NOT_IN_DATE when tenancyStartedBeforeExpiry is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to false))

        val result = stepConfig.mode(mockState)

        assertEquals(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE, result)
    }

    private fun setupStepConfig(): EpcInDateAtStartOfTenancyCheckStepConfig {
        val stepConfig = EpcInDateAtStartOfTenancyCheckStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
