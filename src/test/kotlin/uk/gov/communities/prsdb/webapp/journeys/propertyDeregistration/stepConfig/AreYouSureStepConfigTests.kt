package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    @Test
    fun `mode returns null when form model is not present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when wantsToProceed is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to true))

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, result)
    }

    @Test
    fun `mode returns DOES_NOT_WANT_TO_PROCEED when wantsToProceed is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to false))

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.DOES_NOT_WANT_TO_PROCEED, result)
    }

    @Test
    fun `chooseTemplate returns info form when joint landlords flag is enabled`() {
        whenever(mockFeatureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/deregisterPropertyInfoForm", result)
    }

    @Test
    fun `chooseTemplate returns are you sure form when joint landlords flag is disabled`() {
        whenever(mockFeatureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(false)
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/areYouSureForm", result)
    }

    private fun setupStepConfig(): AreYouSureStepConfig {
        val stepConfig = AreYouSureStepConfig(mockPropertyOwnershipService, mockFeatureFlagManager)
        stepConfig.routeSegment = AreYouSureStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
