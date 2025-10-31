package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.journeys.example.Destination

class StepLifecycleOrchestratorTest {
    @Test
    fun `when step is unreachable, getStepModelAndView calls all step methods in the correct order and returns a redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).beforeIsStepReachable()
        myInOrder.verify(stepConfig).isStepReachable
        myInOrder.verify(stepConfig).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when step is reachable, getStepModelAndView calls all step methods in the correct order and returns the content and view`() {
        // Arrange
        val stepConfig = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(true)
        val contentMap = mapOf("key" to "value")
        val templateName = "templateName"
        whenever(stepConfig.getPageVisitContent()).thenReturn(contentMap)
        whenever(stepConfig.chooseVisitDestination()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.getStepModelAndView()

        // Assert
        myInOrder.verify(stepConfig).beforeIsStepReachable()
        myInOrder.verify(stepConfig).isStepReachable
        myInOrder.verify(stepConfig).afterIsStepReached()
        myInOrder.verify(stepConfig).beforeGetStepContent()
        myInOrder.verify(stepConfig).getPageVisitContent()
        myInOrder.verify(stepConfig).afterGetStepContent()
        myInOrder.verify(stepConfig).beforeGetTemplate()
        myInOrder.verify(stepConfig).chooseVisitDestination()
        myInOrder.verify(stepConfig).afterGetTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when step is unreachable, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(false)
        val redirectUrl = "redirectUrl"
        whenever(stepConfig.getUnreachableStepDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).beforeIsStepReachable()
        myInOrder.verify(stepConfig).isStepReachable
        myInOrder.verify(stepConfig).getUnreachableStepDestination()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }

    @Test
    fun `when invalid data is posted, postStepModelAndView calls step methods in the correct order and returns error content and view`() {
        // Arrange
        val stepConfig = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(true)

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(true)
        whenever(stepConfig.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val contentMap = mapOf("key" to "value", "error" to "content")
        val templateName = "templateName"
        whenever(stepConfig.getInvalidSubmissionContent(anyOrNull())).thenReturn(contentMap)
        whenever(stepConfig.chooseVisitDestination()).thenReturn(Destination.Template(templateName))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).beforeIsStepReachable()
        myInOrder.verify(stepConfig).isStepReachable
        myInOrder.verify(stepConfig).afterIsStepReached()
        myInOrder.verify(stepConfig).beforeValidateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).validateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).afterValidateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).beforeGetStepContent()
        myInOrder.verify(stepConfig).getInvalidSubmissionContent(anyOrNull())
        myInOrder.verify(stepConfig).afterGetStepContent()
        myInOrder.verify(stepConfig).beforeGetTemplate()
        myInOrder.verify(stepConfig).chooseVisitDestination()
        myInOrder.verify(stepConfig).afterGetTemplate()

        assertEquals(modelAndView.model, contentMap)
        assertEquals(modelAndView.viewName, templateName)
    }

    @Test
    fun `when valid data is posted, postStepModelAndView calls step methods in the correct order and returns redirect`() {
        // Arrange
        val stepConfig = mock<JourneyStep<*, *, *>>()
        val myInOrder = inOrder(stepConfig)
        val orchestrator = StepLifecycleOrchestrator(stepConfig)
        whenever(stepConfig.isStepReachable).thenReturn(true)

        whenever(stepConfig.beforeValidateSubmittedData(anyOrNull())).thenReturn(mapOf())

        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(false)
        whenever(stepConfig.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val redirectUrl = "redirectUrl"
        whenever(stepConfig.determineNextDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        val modelAndView = orchestrator.postStepModelAndView(mapOf())

        // Assert
        myInOrder.verify(stepConfig).beforeIsStepReachable()
        myInOrder.verify(stepConfig).isStepReachable
        myInOrder.verify(stepConfig).afterIsStepReached()
        myInOrder.verify(stepConfig).beforeValidateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).validateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).afterValidateSubmittedData(anyOrNull())
        myInOrder.verify(stepConfig).beforeSubmitFormData()
        myInOrder.verify(stepConfig).submitFormData(anyOrNull())
        myInOrder.verify(stepConfig).afterSubmitFormData()
        myInOrder.verify(stepConfig).beforeDetermineRedirect()
        myInOrder.verify(stepConfig).determineNextDestination()
        myInOrder.verify(stepConfig).afterDetermineRedirect()

        assertTrue(modelAndView.model.isEmpty())
        assertEquals(modelAndView.viewName, "redirect:$redirectUrl")
    }
}
