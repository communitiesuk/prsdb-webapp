package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.SystemOperatorNotesJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SystemOperatorNotesFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@ExtendWith(MockitoExtension::class)
class CompleteSystemOperatorLandlordNotesUpdateStepConfigTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: SystemOperatorNotesJourneyState

    @Mock
    private lateinit var mockNotesStep: SystemOperatorLandlordNotesStep

    private lateinit var stepConfig: CompleteSystemOperatorLandlordNotesUpdateStepConfig

    private val landlordId = 123L
    private val notes = "Updated on behalf of the landlord"
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteSystemOperatorLandlordNotesUpdateStepConfig(mockLandlordService)
    }

    @Test
    fun `afterStepIsReached updates the landlord's system operator notes`() {
        setUpState()

        stepConfig.afterStepIsReached(mockState)

        verify(mockLandlordService).updateSystemOperatorNotes(landlordId, notes, initialLastModifiedDate)
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        setUpState()
        whenever(mockLandlordService.updateSystemOperatorNotes(landlordId, notes, initialLastModifiedDate))
            .thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state`() {
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        verify(mockState).deleteJourney()
    }

    private fun setUpState() {
        whenever(mockState.recordId).thenReturn(landlordId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.notesStep).thenReturn(mockNotesStep)
        val formModel = SystemOperatorNotesFormModel()
        formModel.notes = notes
        whenever(mockNotesStep.formModel).thenReturn(formModel)
    }
}
