package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ConfirmRemoveSuperiorLandlordStepConfigTests {
    @Mock
    lateinit var mockState: SuperiorLandlordsDetailsState

    @Mock
    lateinit var collectionKeyParameterService: CollectionKeyParameterService

    private val firstSuperiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "First superior landlord")
    private val secondSuperiorLandlord = SuperiorLandlordDataModel(detailsKnown = false)

    @Test
    fun `beforeAttemptingToReachStep returns true when the key is in the map`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(1 to firstSuperiorLandlord, 2 to secondSuperiorLandlord))

        assertTrue(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns true for the last superior landlord`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(1 to firstSuperiorLandlord))

        assertTrue(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when the key is missing`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(null)

        assertFalse(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false when the key is not in the map`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(3)
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(1 to firstSuperiorLandlord, 2 to secondSuperiorLandlord))

        assertFalse(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `afterStepDataIsAdded removes the superior landlord with the given key`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(1 to firstSuperiorLandlord, 2 to secondSuperiorLandlord))

        stepConfig.afterStepDataIsAdded(mockState)

        val captor = argumentCaptor<Map<Int, SuperiorLandlordDataModel>>()
        verify(mockState).superiorLandlordsMap = captor.capture()
        assertEquals(mapOf(2 to secondSuperiorLandlord), captor.firstValue)
    }

    @Test
    fun `mode is no members when there are no superior landlords left`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())

        assertEquals(AnyMembers.NO_MEMBERS, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is some members when superior landlords remain`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(2 to secondSuperiorLandlord))

        assertEquals(AnyMembers.SOME_MEMBERS, stepConfig.mode(mockState))
    }

    private fun setupStepConfig(): ConfirmRemoveSuperiorLandlordStepConfig {
        val stepConfig = ConfirmRemoveSuperiorLandlordStepConfig(collectionKeyParameterService)
        stepConfig.urlPath = ConfirmRemoveSuperiorLandlordStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
