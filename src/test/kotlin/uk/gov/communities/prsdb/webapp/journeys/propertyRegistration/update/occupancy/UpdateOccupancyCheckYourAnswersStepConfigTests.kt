package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class UpdateOccupancyCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    private val stepConfig = UpdateOccupancyCheckYourAnswersStepConfig(OccupancyDetailsHelper())

    @Nested
    inner class GetStepSpecificContentTests {
        @BeforeEach
        fun setUp() {
            whenever(mockState.occupied).thenReturn(mockOccupiedStep)
            whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
            whenever(mockState.getCyaJourneyId(mockOccupiedStep)).thenReturn("cya-journey-id")
        }

        @Test
        fun `content exposes the check-your-answers page fields`() {
            whenever(mockOccupancyFormModel.occupied).thenReturn(true)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals(true, content["insetText"])
            assertEquals(true, content["showWarning"])
            assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])
            assertEquals("forms.update.checkOccupancy.notOccupied.summaryName", content["summaryName"])
        }

        @Test
        fun `summary list contains a single occupancy status row`() {
            whenever(mockOccupancyFormModel.occupied).thenReturn(true)

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
            assertEquals(1, summaryList.size)
            assertEquals("forms.checkPropertyAnswers.tenancyDetails.occupied", summaryList[0].fieldHeading)
        }
    }

    @Nested
    inner class ResolveNextDestinationTests {
        @Test
        fun `resolveNextDestination returns the default destination without deleting the journey`() {
            val defaultDestination = Destination.Nowhere()

            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            assertSame(defaultDestination, result)
            verify(mockState, never()).deleteJourney()
        }
    }
}
