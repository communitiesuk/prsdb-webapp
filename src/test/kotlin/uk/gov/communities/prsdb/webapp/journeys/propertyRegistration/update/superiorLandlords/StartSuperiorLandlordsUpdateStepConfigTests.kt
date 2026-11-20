package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class StartSuperiorLandlordsUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdateSuperiorLandlordsJourneyState

    @Mock
    private lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var mockAnyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    private val propertyId = 123L

    private lateinit var stepConfig: StartSuperiorLandlordsUpdateStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = StartSuperiorLandlordsUpdateStepConfig(mockPropertyOwnershipService)
    }

    @Test
    fun `afterStepIsReached seeds the superior landlords in id order and answers yes to the ownership chain question`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, ownershipType = OwnershipType.LEASEHOLD_ONLY)
        val second = MockLandlordData.createSuperiorLandlord(propertyOwnership, name = "Second", id = 20)
        val first = MockLandlordData.createSuperiorLandlord(propertyOwnership, name = "First", email = "first@example.com", id = 10)
        propertyOwnership.superiorLandlords.addAll(listOf(second, first))
        whenever(mockState.superiorLandlordsInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockState.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
        whenever(mockSuperiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(mockAnyoneInOwnershipChainStep)
        whenever(mockAnyoneInOwnershipChainStep.stepConfig)
            .thenReturn(AnyoneInOwnershipChainStepConfig().apply { urlPath = AnyoneInOwnershipChainStep.ROUTE_SEGMENT })

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockSuperiorLandlordsTask).superiorLandlordsMap =
            mapOf(
                1 to SuperiorLandlordDataModel.fromSuperiorLandlord(first),
                2 to SuperiorLandlordDataModel.fromSuperiorLandlord(second),
            )
        verify(mockSuperiorLandlordsTask).nextSuperiorLandlordId = 3
        verify(mockState).addStepData(
            AnyoneInOwnershipChainStep.ROUTE_SEGMENT,
            AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true).toPageData(),
        )
        verify(mockState).superiorLandlordsInitialised = true
    }

    @Test
    fun `afterStepIsReached only marks the state as initialised when the property has no superior landlords`() {
        whenever(mockState.superiorLandlordsInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyId, ownershipType = OwnershipType.LEASEHOLD_ONLY))

        stepConfig.afterStepIsReached(mockState)

        verify(mockState, never()).addStepData(any(), any())
        verify(mockState, never()).superiorLandlordsTask
        verify(mockState).superiorLandlordsInitialised = true
    }

    @Test
    fun `afterStepIsReached does not re-initialise when already initialised`() {
        whenever(mockState.superiorLandlordsInitialised).thenReturn(true)

        stepConfig.afterStepIsReached(mockState)

        verifyNoInteractions(mockPropertyOwnershipService)
        verify(mockState, never()).addStepData(any(), any())
    }

    @Test
    fun `mode is complete once the superior landlords have been initialised`() {
        whenever(mockState.superiorLandlordsInitialised).thenReturn(true)

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is null before the superior landlords have been initialised`() {
        whenever(mockState.superiorLandlordsInitialised).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }
}
