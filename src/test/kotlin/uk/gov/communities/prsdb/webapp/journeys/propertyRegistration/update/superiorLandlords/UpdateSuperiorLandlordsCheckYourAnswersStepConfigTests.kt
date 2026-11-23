package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class UpdateSuperiorLandlordsCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateSuperiorLandlordsJourneyState

    @Mock
    private lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var mockAnyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    private val stepConfig = UpdateSuperiorLandlordsCheckYourAnswersStepConfig()

    @Test
    fun `getStepSpecificContent shows a no ownership chain row that links back to the question`() {
        // Arrange
        whenever(mockState.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
        whenever(mockSuperiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(mockAnyoneInOwnershipChainStep)
        whenever(mockAnyoneInOwnershipChainStep.isStepReachable).thenReturn(true)
        whenever(mockAnyoneInOwnershipChainStep.urlPath).thenReturn(AnyoneInOwnershipChainStep.ROUTE_SEGMENT)
        whenever(mockAnyoneInOwnershipChainStep.currentJourneyId).thenReturn("journey-id")

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("forms.update.checkSuperiorLandlords.pageTitle", content["title"])
        assertEquals("forms.update.checkSuperiorLandlords.summaryName", content["summaryName"])
        assertEquals(true, content["showWarning"])
        assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])

        @Suppress("UNCHECKED_CAST")
        val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
        assertEquals(1, summaryList.size)
        assertEquals("registerProperty.superiorLandlords.checkAnswers.anyoneInOwnershipChain", summaryList[0].fieldHeading)
        assertEquals("commonText.no", summaryList[0].fieldValue)
        assertEquals(
            Destination(mockAnyoneInOwnershipChainStep).toUrlStringOrNull(),
            summaryList[0].actions.single().url,
        )
    }

    @Test
    fun `resolveNextDestination returns the default destination without deleting the journey`() {
        val defaultDestination = Destination.Nowhere()

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        assertSame(defaultDestination, result)
        verify(mockState, never()).deleteJourney()
    }
}
