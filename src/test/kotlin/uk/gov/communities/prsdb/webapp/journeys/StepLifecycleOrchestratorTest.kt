package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

class StepLifecycleOrchestratorTest {
    @Test
    fun `when visitable step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = VisitableStepLifecycleOrchestrator(step)
        whenever(step.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(step.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `when visitable step is reachable, getStepModelAndView calls all step methods in the correct order and returns the content and view`() {
        // Arrange
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = VisitableStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(true)
        val contentMap = mapOf("key" to "value")
        val templateName = "templateName"
        whenever(step.getPageVisitContent()).thenReturn(contentMap)
        whenever(step.chooseTemplate()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).getPageVisitContent()
        myInOrder.verify(step).chooseTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when visitable step is unreachable, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = VisitableStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(step.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when invalid data is posted, postStepModelAndView calls step methods in the correct order and returns error content and view`() {
        // Arrange
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = VisitableStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(true)
        whenever(step.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val contentMap = mapOf("key" to "value", "error" to "content")
        val templateName = "templateName"
        whenever(step.getInvalidSubmissionContent(anyOrNull())).thenReturn(contentMap)
        whenever(step.chooseTemplate()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).validateSubmittedData(anyOrNull())
        myInOrder.verify(step).getInvalidSubmissionContent(anyOrNull())
        myInOrder.verify(step).chooseTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when valid data is posted, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = VisitableStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(false)
        whenever(step.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val redirectUrl = "redirectUrl"
        whenever(step.getNextDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).validateSubmittedData(anyOrNull())
        myInOrder.verify(step).submitFormData(anyOrNull())
        myInOrder.verify(step).getNextDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when internal step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val step = mock<JourneyStep.InternalStep<*, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = RedirectingStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(step.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `when internal step is reachable, getStepModelAndView calls all step methods in the correct order and returns the next destination`() {
        // Arrange
        val step = mock<JourneyStep.InternalStep<*, *>>()
        val myInOrder = inOrder(step)
        val orchestrator = RedirectingStepLifecycleOrchestrator(step)
        whenever(step.attemptToReachStep()).thenReturn(true)
        val nextUrl = "nextUrl"
        whenever(step.getNextDestination()).thenReturn(Destination.ExternalUrl(nextUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(step).attemptToReachStep()
        myInOrder.verify(step).getNextDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$nextUrl")
    }
}
