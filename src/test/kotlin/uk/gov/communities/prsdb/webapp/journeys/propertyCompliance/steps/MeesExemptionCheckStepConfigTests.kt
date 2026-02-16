package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.ExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class MeesExemptionCheckStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockEpcState: EpcState

    val routeSegment = MeesExemptionCheckStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when propertyHasExemption is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_EXEMPTION when propertyHasExemption is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("propertyHasExemption" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(ExemptionMode.HAS_EXEMPTION, result)
    }

    @Test
    fun `mode returns NO_EXEMPTION when propertyHasExemption is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("propertyHasExemption" to false))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(ExemptionMode.NO_EXEMPTION, result)
    }

    private fun setupStepConfig(): MeesExemptionCheckStepConfig {
        val stepConfig = MeesExemptionCheckStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
