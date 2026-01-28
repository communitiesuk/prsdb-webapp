package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

class JourneyControllerHelperTests {
    private val stepRouteSegment = "test-step"
    private val stepLifecycleOrchestrator = mock<VisitableStepLifecycleOrchestrator>()
    private val journeyId = "testJourneyId"
    private val initializeJourney: () -> String = { journeyId }
    private val formData = mapOf("field" to "value")

    @Test
    fun `handleGetRequest returns getStepModelAndView when journey is valid and step exists`() {
        // Arrange
        val buildJourneyRoutingMap = { mapOf(stepRouteSegment to stepLifecycleOrchestrator) }

        val modelAndView = ModelAndView("testView")
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(modelAndView)

        // Act
        val result = JourneyControllerHelper.handleGetRequest(buildJourneyRoutingMap, stepRouteSegment, initializeJourney)

        // Assert
        assertSame(modelAndView, result)
        verify(stepLifecycleOrchestrator).getStepModelAndView()
    }

    @Test
    fun `handleGetRequest throws 404 error when step does not exist in journey map`() {
        // Arrange
        val buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator> = { emptyMap() }

        // Act & Assert
        val exception =
            assertThrows<ResponseStatusException> {
                JourneyControllerHelper.handleGetRequest(buildJourneyRoutingMap, stepRouteSegment, initializeJourney)
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `handleGetRequest redirects with journey ID when NoSuchJourneyException is thrown`() {
        // Arrange
        val buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator> = { throw NoSuchJourneyException() }

        // Act
        val result = JourneyControllerHelper.handleGetRequest(buildJourneyRoutingMap, stepRouteSegment, initializeJourney)

        // Assert
        val expectedRedirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
        assertEquals("redirect:$expectedRedirectUrl", result.viewName)
    }

    @Test
    fun `handlePostRequest returns postStepModelAndView when journey is valid and step exists`() {
        // Arrange
        val buildJourneyRoutingMap = { mapOf(stepRouteSegment to stepLifecycleOrchestrator) }

        val modelAndView = ModelAndView("testView")
        whenever(stepLifecycleOrchestrator.postStepModelAndView(formData)).thenReturn(modelAndView)

        // Act
        val result =
            JourneyControllerHelper.handlePostRequest(
                buildJourneyRoutingMap,
                stepRouteSegment,
                initializeJourney,
                formData,
            )

        // Assert
        assertSame(modelAndView, result)
        verify(stepLifecycleOrchestrator).postStepModelAndView(formData)
    }

    @Test
    fun `handlePostRequest throws 404 error when step does not exist in journey map`() {
        // Arrange
        val buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator> = { emptyMap() }

        // Act & Assert
        val exception =
            assertThrows<ResponseStatusException> {
                JourneyControllerHelper.handlePostRequest(buildJourneyRoutingMap, stepRouteSegment, initializeJourney, formData)
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `handlePostRequest redirects with journey ID when NoSuchJourneyException is thrown`() {
        // Arrange
        val buildJourneyRoutingMap: () -> Map<String, StepLifecycleOrchestrator> = { throw NoSuchJourneyException() }

        // Act
        val result =
            JourneyControllerHelper.handlePostRequest(
                buildJourneyRoutingMap,
                stepRouteSegment,
                initializeJourney,
                formData,
            )

        // Assert
        val expectedRedirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
        assertEquals("redirect:$expectedRedirectUrl", result.viewName)
    }
}
