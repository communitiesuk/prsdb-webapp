package uk.gov.communities.prsdb.webapp.journeys.example

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator

class DestinationTests {
    @Test
    fun `VisitableStep Destination with just a step returns a redirect for the current step and journey`() {
        // Arrange
        val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        val journeyId = "test-journey-id"
        val routeSegment = "test-segment"

        whenever(mockStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockStep.routeSegment).thenReturn(routeSegment)

        // Act
        val destination = Destination(mockStep)
        val modelAndView = destination.toModelAndView()
        val finalUrl = resolveModelAndViewToRedirectUrl(modelAndView)

        // Assert
        Assertions.assertEquals(finalUrl, "$routeSegment?journeyId=$journeyId")
    }

    @Test
    fun `VisitableStep Destination with explicit journeyId returns a redirect for the specified step and journey`() {
        // Arrange
        val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        val journeyId = "explicit-journey-id"
        val routeSegment = "explicit-segment"

        whenever(mockStep.routeSegment).thenReturn(routeSegment)

        // Act
        val destination = Destination.VisitableStep(mockStep, journeyId)
        val modelAndView = destination.toModelAndView()
        val finalUrl = resolveModelAndViewToRedirectUrl(modelAndView)

        // Assert
        Assertions.assertEquals(finalUrl, "$routeSegment?journeyId=$journeyId")
    }

    @Test
    fun `VisitableStep Destination for an unreachable step returns null URL`() {
        // Arrange
        val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        val journeyId = "test-journey-id"
        val routeSegment = "test-segment"

        whenever(mockStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockStep.routeSegment).thenReturn(routeSegment)
        whenever(mockStep.isStepReachable).thenReturn(false)

        // Act
        val destination = Destination(mockStep)
        val urlString = destination.toUrlStringOrNull()

        // Assert
        Assertions.assertNull(urlString)
    }

    @Test
    fun `VisitableStep Destination for a reachable step returns correct URL`() {
        // Arrange
        val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        val journeyId = "test-journey-id"
        val routeSegment = "test-segment"

        whenever(mockStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockStep.routeSegment).thenReturn(routeSegment)
        whenever(mockStep.isStepReachable).thenReturn(true)

        // Act
        val destination = Destination(mockStep)
        val urlString = destination.toUrlStringOrNull()

        // Assert
        Assertions.assertEquals("$routeSegment?journeyId=$journeyId", urlString)
    }

    @Test
    fun `ExternalUrl Destination returns a redirect to the specified URL with parameters`() {
        // Arrange
        val externalUrl = "http://example.com"
        val params = mapOf("param1" to "value1", "param2" to "value2")

        // Act
        val destination = Destination.ExternalUrl(externalUrl, params)
        val modelAndView = destination.toModelAndView()

        // Assert
        Assertions.assertEquals("redirect:$externalUrl", modelAndView.viewName)
        Assertions.assertEquals("value1", modelAndView.model["param1"])
        Assertions.assertEquals("value2", modelAndView.model["param2"])
    }

    @Test
    fun `ExternalUrl Destination without parameters returns a redirect to the specified URL`() {
        // Arrange
        val externalUrl = "http://example.com"

        // Act
        val destination = Destination.ExternalUrl(externalUrl)
        val modelAndView = destination.toModelAndView()

        // Assert
        Assertions.assertEquals("redirect:$externalUrl", modelAndView.viewName)
        Assertions.assertTrue(modelAndView.model.isEmpty())
    }

    @Test
    fun `ExternalUrl Destination returns correct URL string with parameters`() {
        // Arrange
        val externalUrl = "http://example.com"
        val params = mapOf("param1" to "value1", "param2" to "value2")

        // Act
        val destination = Destination.ExternalUrl(externalUrl, params)
        val urlString = destination.toUrlStringOrNull()

        // Assert
        Assertions.assertEquals("http://example.com?param1=value1&param2=value2", urlString)
    }

    @Test
    fun `Template Destination returns the correct template name and content`() {
        // Arrange
        val templateName = "test-template"
        val content = mapOf("key1" to "value1", "key2" to "value2")

        // Act
        val destination = Destination.Template(templateName, content)
        val modelAndView = destination.toModelAndView()

        // Assert
        Assertions.assertEquals(templateName, modelAndView.viewName)
        Assertions.assertEquals("value1", modelAndView.model["key1"])
        Assertions.assertEquals("value2", modelAndView.model["key2"])
    }

    @Test
    fun `Template Destination with empty content returns the correct template name`() {
        // Arrange
        val templateName = "test-template"

        // Act
        val destination = Destination.Template(templateName)
        val modelAndView = destination.toModelAndView()

        // Assert
        Assertions.assertEquals(templateName, modelAndView.viewName)
        Assertions.assertTrue(modelAndView.model.isEmpty())
    }

    @Test
    fun `Template Destination withContent creates a new Destination with that ModelContent`() {
        // Arrange
        val templateName = "test-template"
        val initialContent = mapOf("key1" to "value1")
        val additionalContent = mapOf("key2" to "value2")

        // Act
        val destination = Destination.Template(templateName, initialContent)
        val updatedDestination = destination.withModelContent(additionalContent)
        val updatedModelAndView = updatedDestination.toModelAndView()
        val oldModelAndView = destination.toModelAndView()

        // Assert
        Assertions.assertEquals(templateName, updatedModelAndView.viewName)
        Assertions.assertEquals("value1", updatedModelAndView.model["key1"])
        Assertions.assertEquals("value2", updatedModelAndView.model["key2"])

        Assertions.assertEquals(templateName, oldModelAndView.viewName)
        Assertions.assertEquals("value1", oldModelAndView.model["key1"])
        Assertions.assertNull(oldModelAndView.model["key2"])
    }

    @Test
    fun `Template Destination converted to url returns null`() {
        // Arrange
        val templateName = "test-template"

        // Act
        val destination = Destination.Template(templateName)
        val urlString = destination.toUrlStringOrNull()

        // Assert
        Assertions.assertNull(urlString)
    }

    @Test
    fun `withModelContent on non-Template Destination returns the same Destination`() {
        // Arrange
        val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        val nonTemplateDestinations =
            listOf(
                Destination.VisitableStep(mockStep, "test-journey-id"),
                Destination.ExternalUrl("http://example.com"),
                Destination.NavigationalStep(mock()),
                Destination.Nowhere(),
            )

        val contentToAdd = mapOf("key" to "value")

        // Act & Assert
        nonTemplateDestinations.forEach { destination ->
            val updatedDestination = destination.withModelContent(contentToAdd)
            Assertions.assertSame(destination, updatedDestination)
        }

        Destination::class.sealedSubclasses.all { type ->
            type == Destination.Template::class || nonTemplateDestinations.any { type.isInstance(it) }
        }
    }

    @Test
    fun `NavigationalStep Destination calls through to a navigation step lifecycle orchestrator for that step`() {
        // Arrange
        val mockStep = mock<JourneyStep.InternalStep<*, *>>()
        val mockLifecycleOrchestrator = mock<StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator>()
        val modelAndView = ModelAndView()
        whenever(mockStep.lifecycleOrchestrator).thenReturn(mockLifecycleOrchestrator)
        whenever(mockLifecycleOrchestrator.getStepModelAndView()).thenReturn(modelAndView)

        // Act
        val destination = Destination.NavigationalStep(mockStep)
        val result = destination.toModelAndView()

        // Assert
        Assertions.assertSame(modelAndView, result)
    }

    @Test
    fun `Nowhere Destination throws when converted to  a ModelAndView`() {
        // Arrange
        val destination = Destination.Nowhere()

        // Act & Assert
        assertThrows<Exception> {
            destination.toModelAndView()
        }
    }

    @Test
    fun `Nowhere Destination withModelContent returns the same Nowhere Destination`() {
        // Arrange
        val contentToAdd = mapOf("key" to "value")
        val destination = Destination.Nowhere()

        // Act
        val updatedDestination = destination.withModelContent(contentToAdd)

        // Assert
        Assertions.assertSame(destination, updatedDestination)
    }

    @Test
    fun `Nowhere Destination converted to url returns null`() {
        // Act
        val destinationString = Destination.Nowhere().toUrlStringOrNull()

        // Assert
        Assertions.assertNull(destinationString)
    }

    @Test
    fun `Companion invoke returns the correct Destination type based on the JourneyStep provided`() {
        // Arrange
        val mockRequestableStep = mock<JourneyStep.RequestableStep<*, *, *>>()
        whenever(mockRequestableStep.currentJourneyId).thenReturn("journeyId")

        val mockInternalStep = mock<JourneyStep.InternalStep<*, *>>()

        // Act
        val visitableDestination = Destination(mockRequestableStep)
        val notionalDestination = Destination(mockInternalStep)

        // Assert
        Assertions.assertTrue(visitableDestination is Destination.VisitableStep)
        Assertions.assertTrue(notionalDestination is Destination.NavigationalStep)
    }

    private fun resolveModelAndViewToRedirectUrl(modelAndView: ModelAndView): String? {
        val viewName = modelAndView.viewName
        if (viewName == null) {
            return null
        }

        if (!viewName.startsWith("redirect:")) {
            return null
        }

        val redirectUrl = viewName.removePrefix("redirect:")
        val redirectView = RedirectView(redirectUrl)
        redirectView.setAttributesMap(modelAndView.model)
        return redirectView.url
    }
}
