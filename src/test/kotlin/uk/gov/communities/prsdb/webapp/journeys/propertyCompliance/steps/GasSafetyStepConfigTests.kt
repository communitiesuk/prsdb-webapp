package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class GasSafetyStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockGasSafetyState: GasSafetyState

    @Mock
    lateinit var mockGasSafetyStep: GasSafetyStep

    val routeSegment = GasSafetyStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockGasSafetyState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.formModelOrNull).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockGasSafetyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when hasCert is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = GasSafetyFormModel().apply { hasCert = null }
        whenever(mockGasSafetyState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockGasSafetyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_CERTIFICATE when hasCert is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = GasSafetyFormModel().apply { hasCert = true }
        whenever(mockGasSafetyState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockGasSafetyState)

        // Assert
        assertEquals(GasSafetyMode.HAS_CERTIFICATE, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when hasCert is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = GasSafetyFormModel().apply { hasCert = false }
        whenever(mockGasSafetyState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockGasSafetyState)

        // Assert
        assertEquals(GasSafetyMode.NO_CERTIFICATE, result)
    }

    private fun setupStepConfig(): GasSafetyStepConfig {
        val stepConfig = GasSafetyStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
