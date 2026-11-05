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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ConfirmRemoveFreeholderStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersDetailsState

    @Mock
    lateinit var collectionKeyParameterService: CollectionKeyParameterService

    private val firstFreeholder = FreeholderDataModel(detailsKnown = true, name = "First freeholder")
    private val secondFreeholder = FreeholderDataModel(detailsKnown = false)

    @Test
    fun `beforeAttemptingToReachStep returns true when the key is in the map and other freeholders remain`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder, 2 to secondFreeholder))

        assertTrue(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns true for the last freeholder when removing the last member is allowed`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder))
        whenever(mockState.allowRemovingLastMember).thenReturn(true)

        assertTrue(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `beforeAttemptingToReachStep returns false for the last freeholder when removing the last member is not allowed`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder))
        whenever(mockState.allowRemovingLastMember).thenReturn(false)

        assertFalse(stepConfig.beforeAttemptingToReachStep(mockState))
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
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder, 2 to secondFreeholder))

        assertFalse(stepConfig.beforeAttemptingToReachStep(mockState))
    }

    @Test
    fun `afterStepDataIsAdded removes the freeholder with the given key`() {
        val stepConfig = setupStepConfig()
        whenever(collectionKeyParameterService.getParameterOrNull()).thenReturn(1)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to firstFreeholder, 2 to secondFreeholder))

        stepConfig.afterStepDataIsAdded(mockState)

        val captor = argumentCaptor<Map<Int, FreeholderDataModel>>()
        verify(mockState).freeholdersMap = captor.capture()
        assertEquals(mapOf(2 to secondFreeholder), captor.firstValue)
    }

    @Test
    fun `mode is no members when there are no freeholders left`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())

        assertEquals(AnyMembers.NO_MEMBERS, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is some members when freeholders remain`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.freeholdersMap).thenReturn(mapOf(2 to secondFreeholder))

        assertEquals(AnyMembers.SOME_MEMBERS, stepConfig.mode(mockState))
    }

    private fun setupStepConfig(): ConfirmRemoveFreeholderStepConfig {
        val stepConfig = ConfirmRemoveFreeholderStepConfig(collectionKeyParameterService)
        stepConfig.urlPath = ConfirmRemoveFreeholderStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
