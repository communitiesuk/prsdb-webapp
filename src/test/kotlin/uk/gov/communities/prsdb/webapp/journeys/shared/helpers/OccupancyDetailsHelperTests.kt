package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

@ExtendWith(MockitoExtension::class)
class OccupancyDetailsHelperTests {
    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    private val occupancyDetailsHelper = OccupancyDetailsHelper()

    @BeforeEach
    fun setUp() {
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockState.getCyaJourneyId(mockOccupiedStep)).thenReturn("child-journey-id")
    }

    @Test
    fun `getOccupancyStatusOnlySummaryList returns a single occupancy status row when occupied`() {
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        val rows = occupancyDetailsHelper.getOccupancyStatusOnlySummaryList(mockState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.occupied", rows.single().fieldHeading)
        assertEquals(true, rows.single().fieldValue)
    }

    @Test
    fun `getOccupancyStatusOnlySummaryList returns a single occupancy status row when unoccupied`() {
        whenever(mockOccupancyFormModel.occupied).thenReturn(false)

        val rows = occupancyDetailsHelper.getOccupancyStatusOnlySummaryList(mockState)

        assertEquals(1, rows.size)
        assertEquals("forms.checkPropertyAnswers.tenancyDetails.occupied", rows.single().fieldHeading)
        assertEquals(false, rows.single().fieldValue)
    }
}
