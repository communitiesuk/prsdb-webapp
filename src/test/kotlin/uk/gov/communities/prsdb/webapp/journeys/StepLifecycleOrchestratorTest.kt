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
    fun `when step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val innerStep = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(innerStep)
        val orchestrator = StepLifecycleOrchestrator(innerStep)
        whenever(innerStep.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(innerStep.getUnreachableStepRedirect()).thenReturn(redirectUrl)

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(innerStep).beforeIsStepReachable()
        myInOrder.verify(innerStep).isStepReachable
        myInOrder.verify(innerStep).getUnreachableStepRedirect()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when step is reachable, getStepModelAndView calls all step methods in the correct order and returns the content and view`() {
        // Arrange
        val innerStep = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(innerStep)
        val orchestrator = StepLifecycleOrchestrator(innerStep)
        whenever(innerStep.isStepReachable).thenReturn(true)
        val contentMap = mapOf("key" to "value")
        val templateName = "templateName"
        whenever(innerStep.getPageVisitContent()).thenReturn(contentMap)
        whenever(innerStep.chooseTemplate()).thenReturn(templateName)

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(innerStep).beforeIsStepReachable()
        myInOrder.verify(innerStep).isStepReachable
        myInOrder.verify(innerStep).afterIsStepReached()
        myInOrder.verify(innerStep).beforeGetStepContent()
        myInOrder.verify(innerStep).getPageVisitContent()
        myInOrder.verify(innerStep).afterGetStepContent()
        myInOrder.verify(innerStep).beforeGetTemplate()
        myInOrder.verify(innerStep).chooseTemplate()
        myInOrder.verify(innerStep).afterGetTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when step is unreachable, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val innerStep = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(innerStep)
        val orchestrator = StepLifecycleOrchestrator(innerStep)
        whenever(innerStep.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(innerStep.getUnreachableStepRedirect()).thenReturn(redirectUrl)

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(innerStep).beforeIsStepReachable()
        myInOrder.verify(innerStep).isStepReachable
        myInOrder.verify(innerStep).getUnreachableStepRedirect()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when invalid data is posted, postStepModelAndView calls step methods in the correct order and returns error content and view`() {
        // Arrange
        val innerStep = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(innerStep)
        val orchestrator = StepLifecycleOrchestrator(innerStep)
        whenever(innerStep.isStepReachable).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(true)
        whenever(innerStep.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val contentMap = mapOf("key" to "value", "error" to "content")
        val templateName = "templateName"
        whenever(innerStep.getInvalidSubmissionContent(anyOrNull())).thenReturn(contentMap)
        whenever(innerStep.chooseTemplate()).thenReturn(templateName)

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(innerStep).beforeIsStepReachable()
        myInOrder.verify(innerStep).isStepReachable
        myInOrder.verify(innerStep).afterIsStepReached()
        myInOrder.verify(innerStep).beforeValidateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).validateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).afterValidateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).beforeGetStepContent()
        myInOrder.verify(innerStep).getInvalidSubmissionContent(anyOrNull())
        myInOrder.verify(innerStep).afterGetStepContent()
        myInOrder.verify(innerStep).beforeGetTemplate()
        myInOrder.verify(innerStep).chooseTemplate()
        myInOrder.verify(innerStep).afterGetTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when valid data is posted, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val innerStep = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(innerStep)
        val orchestrator = StepLifecycleOrchestrator(innerStep)
        whenever(innerStep.isStepReachable).thenReturn(true)

        whenever(innerStep.beforeValidateSubmittedData(anyOrNull())).thenReturn(mapOf())

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(false)
        whenever(innerStep.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val redirectUrl = "redirectUrl"
        whenever(innerStep.determineRedirect()).thenReturn(redirectUrl)

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(innerStep).beforeIsStepReachable()
        myInOrder.verify(innerStep).isStepReachable
        myInOrder.verify(innerStep).afterIsStepReached()
        myInOrder.verify(innerStep).beforeValidateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).validateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).afterValidateSubmittedData(anyOrNull())
        myInOrder.verify(innerStep).beforeSubmitFormData()
        myInOrder.verify(innerStep).submitFormData(anyOrNull())
        myInOrder.verify(innerStep).afterSubmitFormData()
        myInOrder.verify(innerStep).beforeDetermineRedirect()
        myInOrder.verify(innerStep).determineRedirect()
        myInOrder.verify(innerStep).afterDetermineRedirect()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }
}
