package uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes

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
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.SystemOperatorNotesJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SystemOperatorNotesFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class SystemOperatorPropertyNotesStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: SystemOperatorNotesJourneyState

    private val propertyOwnershipId = 123L

    private lateinit var stepConfig: SystemOperatorPropertyNotesStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = SystemOperatorPropertyNotesStepConfig(mockPropertyOwnershipService)
        stepConfig.urlPath = AbstractSystemOperatorNotesStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
    }

    @Test
    fun `afterStepIsReached prefills the notes from the property record`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        propertyOwnership.updateSystemOperatorNotes("Existing notes")
        whenever(mockState.notesInitialised).thenReturn(null)
        whenever(mockState.recordId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).addStepData(
            AbstractSystemOperatorNotesStep.ROUTE_SEGMENT,
            SystemOperatorNotesFormModel().apply { notes = "Existing notes" }.toPageData(),
        )
        verify(mockState).notesInitialised = true
    }

    @Test
    fun `afterStepIsReached when the property has no notes marks the state as initialised only`() {
        whenever(mockState.notesInitialised).thenReturn(null)
        whenever(mockState.recordId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership())

        stepConfig.afterStepIsReached(mockState)

        verify(mockState, never()).addStepData(any(), any())
        verify(mockState).notesInitialised = true
    }

    @Test
    fun `afterStepIsReached does not re-initialise when already initialised`() {
        whenever(mockState.notesInitialised).thenReturn(true)

        stepConfig.afterStepIsReached(mockState)

        verifyNoInteractions(mockPropertyOwnershipService)
        verify(mockState, never()).addStepData(any(), any())
    }

    @Test
    fun `mode returns COMPLETE when form model is present`() {
        whenever(mockState.getStepData(AbstractSystemOperatorNotesStep.ROUTE_SEGMENT)).thenReturn(mapOf("notes" to "Some notes"))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when form model is not present`() {
        whenever(mockState.getStepData(AbstractSystemOperatorNotesStep.ROUTE_SEGMENT)).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }
}
