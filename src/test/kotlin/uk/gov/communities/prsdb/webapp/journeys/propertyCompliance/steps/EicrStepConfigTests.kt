package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EicrStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockEicrState: EicrState

    @Mock
    lateinit var mockEicrStep: EicrStep

    val routeSegment = EicrStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEicrState.eicrStep).thenReturn(mockEicrStep)
        whenever(mockEicrStep.formModelOrNull).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when hasCert is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = EicrFormModel().apply { hasCert = null }
        whenever(mockEicrState.eicrStep).thenReturn(mockEicrStep)
        whenever(mockEicrStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_CERTIFICATE when hasCert is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = EicrFormModel().apply { hasCert = true }
        whenever(mockEicrState.eicrStep).thenReturn(mockEicrStep)
        whenever(mockEicrStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertEquals(EicrMode.HAS_CERTIFICATE, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when hasCert is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val formModel = EicrFormModel().apply { hasCert = false }
        whenever(mockEicrState.eicrStep).thenReturn(mockEicrStep)
        whenever(mockEicrStep.formModelOrNull).thenReturn(formModel)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertEquals(EicrMode.NO_CERTIFICATE, result)
    }

    private fun setupStepConfig(): EicrStepConfig {
        val stepConfig = EicrStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
