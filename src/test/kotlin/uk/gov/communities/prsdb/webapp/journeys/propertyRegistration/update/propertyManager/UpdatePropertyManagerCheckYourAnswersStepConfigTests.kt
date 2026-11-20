package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyManagerTask
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
class UpdatePropertyManagerCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdatePropertyManagerJourneyState

    @Mock
    private lateinit var mockPropertyManagerTask: PropertyManagerTask

    @Mock
    private lateinit var mockPropertyManagerStep: PropertyManagerStep

    private val stepConfig = UpdatePropertyManagerCheckYourAnswersStepConfig()

    @Test
    fun `getStepSpecificContent shows a no property manager row that links back to the question`() {
        // Arrange
        whenever(mockState.propertyManagerTask).thenReturn(mockPropertyManagerTask)
        whenever(mockPropertyManagerTask.propertyManagerStep).thenReturn(mockPropertyManagerStep)
        whenever(mockPropertyManagerStep.isStepReachable).thenReturn(true)
        whenever(mockPropertyManagerStep.urlPath).thenReturn(PropertyManagerStep.ROUTE_SEGMENT)
        whenever(mockPropertyManagerStep.currentJourneyId).thenReturn("journey-id")

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("forms.update.checkPropertyManager.pageTitle", content["title"])
        assertEquals("forms.update.checkPropertyManager.summaryName", content["summaryName"])
        assertNull(content["insetText"])
        assertEquals(true, content["showWarning"])
        assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])

        @Suppress("UNCHECKED_CAST")
        val summaryList = content["summaryListData"] as List<SummaryListRowViewModel>
        assertEquals(1, summaryList.size)
        assertEquals("registerProperty.propertyManager.usingPropertyManager.fieldSetHeading", summaryList[0].fieldHeading)
        assertEquals("registerProperty.propertyManager.usingPropertyManager.radios.no", summaryList[0].fieldValue)
        assertEquals(
            Destination(mockPropertyManagerStep).toUrlStringOrNull(),
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
