package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class InitialiseOwnershipUpdateStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: UpdateOwnershipJourneyState

    @Mock
    lateinit var mockOwnershipStructureTask: OwnershipStructureTask

    @Mock
    lateinit var mockFreeholdersTask: FreeholdersTask

    @Mock
    lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    private val propertyId = 123L

    @Test
    fun `afterStepIsReached seeds freeholders and superior landlords keyed from 1 in id order and sets next ids`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val freeholder1 = MockLandlordData.createFreeholder(propertyOwnership, name = "Alice", email = "alice@example.com").withId(1)
        val freeholder2 = MockLandlordData.createFreeholder(propertyOwnership, name = "Bob").withId(2)
        val superiorLandlord = MockLandlordData.createSuperiorLandlord(propertyOwnership, name = "Carol").withId(3)
        propertyOwnership.freeholders.add(freeholder2)
        propertyOwnership.freeholders.add(freeholder1)
        propertyOwnership.superiorLandlords.add(superiorLandlord)
        setupUninitialisedMockState()
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockFreeholdersTask).freeholdersMap =
            mapOf(
                1 to FreeholderDataModel.fromFreeholder(freeholder1),
                2 to FreeholderDataModel.fromFreeholder(freeholder2),
            )
        verify(mockFreeholdersTask).nextFreeholderId = 3
        verify(mockSuperiorLandlordsTask).superiorLandlordsMap =
            mapOf(1 to SuperiorLandlordDataModel.fromSuperiorLandlord(superiorLandlord))
        verify(mockSuperiorLandlordsTask).nextSuperiorLandlordId = 2
        verify(mockState).ownershipPartiesInitialised = true
    }

    @Test
    fun `afterStepIsReached seeds empty maps when there are no associated parties`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        setupUninitialisedMockState()
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockFreeholdersTask).freeholdersMap = emptyMap()
        verify(mockFreeholdersTask).nextFreeholderId = 1
        verify(mockSuperiorLandlordsTask).superiorLandlordsMap = emptyMap()
        verify(mockSuperiorLandlordsTask).nextSuperiorLandlordId = 1
        verify(mockState).ownershipPartiesInitialised = true
    }

    @Test
    fun `afterStepIsReached does nothing when already initialised`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.ownershipPartiesInitialised).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockPropertyOwnershipService)
        verifyNoInteractions(mockOwnershipStructureTask)
    }

    private fun setupStepConfig(): InitialiseOwnershipUpdateStepConfig {
        val stepConfig = InitialiseOwnershipUpdateStepConfig(mockPropertyOwnershipService)
        stepConfig.urlPath = InitialiseOwnershipUpdateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun setupUninitialisedMockState() {
        whenever(mockState.ownershipPartiesInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.ownershipStructureTask).thenReturn(mockOwnershipStructureTask)
        whenever(mockOwnershipStructureTask.freeholdersTask).thenReturn(mockFreeholdersTask)
        whenever(mockOwnershipStructureTask.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
    }

    private fun <T : Any> T.withId(id: Long): T = apply { ReflectionTestUtils.setField(this, "id", id) }
}
