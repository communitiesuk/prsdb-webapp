package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult

class StepLifecycleOrchestratorTest {
    @Test
    fun `when used like a naive constructor, StepLifecycleOrchestrator returns a corresponding orchestrator`() {
        // Arrange
        val internalStep = mock<JourneyStep.InternalStep<*, *, *>>()
        val requestableStep = mock<JourneyStep.RequestableStep<*, *, *>>()

        // Act
        val notionalOrchestrator = StepLifecycleOrchestrator(internalStep)
        val visitableOrchestrator = StepLifecycleOrchestrator(requestableStep)

        // Assert
        assertTrue(notionalOrchestrator is StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator)
        assertTrue(visitableOrchestrator is StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator)
    }

    @Test
    fun `when visitable step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `when visitable step is reachable, getStepModelAndView calls all step methods in the correct order and returns the content and view`() {
        // Arrange
        val stepConfig = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(true)
        val contentMap = mapOf("key" to "value")
        val templateName = "templateName"
        whenever(stepConfig.getPageVisitContent()).thenReturn(contentMap)
        whenever(stepConfig.chooseTemplate()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).getPageVisitContent()
        myInOrder.verify(stepConfig).chooseTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when visitable step is unreachable, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when invalid data is posted, postStepModelAndView calls step methods in the correct order and returns error content and view`() {
        // Arrange
        val stepConfig = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(true)
        whenever(stepConfig.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val contentMap = mapOf("key" to "value", "error" to "content")
        val templateName = "templateName"
        whenever(stepConfig.getInvalidSubmissionContent(anyOrNull())).thenReturn(contentMap)
        whenever(stepConfig.chooseTemplate()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).validateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).getInvalidSubmissionContent(anyOrNull())
        myInOrder.verify(stepConfig).chooseTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when valid data is posted, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep.RequestableStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(false)
        whenever(stepConfig.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getNextDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).validateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).submitFormData(anyOrNull())
        myInOrder.verify(stepConfig).getNextDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when notional step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep.InternalStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `when notional step is reachable, getStepModelAndView calls all step methods in the correct order and returns the next destination`() {
        // Arrange
        val stepConfig = mock<JourneyStep.InternalStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.attemptToReachStep()).thenReturn(true)
        val nextUrl = "nextUrl"
        whenever(stepConfig.getNextDestination()).thenReturn(Destination.ExternalUrl(nextUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).attemptToReachStep()
        myInOrder.verify(stepConfig).getNextDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$nextUrl")
    }
}
