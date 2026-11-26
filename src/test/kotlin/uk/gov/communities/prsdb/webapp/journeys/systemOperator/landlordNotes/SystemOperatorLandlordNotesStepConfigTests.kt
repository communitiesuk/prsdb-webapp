package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SystemOperatorNotesFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class SystemOperatorLandlordNotesStepConfigTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockState: UpdateSystemOperatorLandlordNotesJourneyState

    private val landlordId = 123L

    private lateinit var stepConfig: SystemOperatorLandlordNotesStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = SystemOperatorLandlordNotesStepConfig(mockLandlordService)
        stepConfig.urlPath = SystemOperatorLandlordNotesStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
    }

    @Test
    fun `afterStepIsReached prefills the notes from the landlord record`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.updateSystemOperatorNotes("Existing notes")
        whenever(mockState.notesInitialised).thenReturn(null)
        whenever(mockState.landlordId).thenReturn(landlordId)
        whenever(mockLandlordService.retrieveLandlordById(landlordId)).thenReturn(landlord)

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).addStepData(
            SystemOperatorLandlordNotesStep.ROUTE_SEGMENT,
            SystemOperatorNotesFormModel().apply { notes = "Existing notes" }.toPageData(),
        )
        verify(mockState).notesInitialised = true
    }

    @Test
    fun `afterStepIsReached when the landlord has no notes marks the state as initialised only`() {
        whenever(mockState.notesInitialised).thenReturn(null)
        whenever(mockState.landlordId).thenReturn(landlordId)
        whenever(mockLandlordService.retrieveLandlordById(landlordId)).thenReturn(MockLandlordData.createIndividualLandlord())

        stepConfig.afterStepIsReached(mockState)

        verify(mockState, never()).addStepData(any(), any())
        verify(mockState).notesInitialised = true
    }

    @Test
    fun `afterStepIsReached does not re-initialise when already initialised`() {
        whenever(mockState.notesInitialised).thenReturn(true)

        stepConfig.afterStepIsReached(mockState)

        verifyNoInteractions(mockLandlordService)
        verify(mockState, never()).addStepData(any(), any())
    }

    @Test
    fun `mode returns COMPLETE when form model is present`() {
        whenever(mockState.getStepData(SystemOperatorLandlordNotesStep.ROUTE_SEGMENT)).thenReturn(mapOf("notes" to "Some notes"))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when form model is not present`() {
        whenever(mockState.getStepData(SystemOperatorLandlordNotesStep.ROUTE_SEGMENT)).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }
}
