package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView

class JourneyStepDispatcherTests {
    private var previousObserver: ((String, Map<String, StepLifecycleOrchestrator>) -> ModelAndView?)? = null

    @BeforeEach
    fun setUp() {
        previousObserver = JourneyStepDispatcher.routingMapObserver
        JourneyStepDispatcher.routingMapObserver = null
        val request = MockHttpServletRequest()
        request.requestURI = "/base/task-route"
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        JourneyStepDispatcher.routingMapObserver = previousObserver
    }

    @Nested
    inner class HandleInitialisableRequest {
        @Test
        fun `dispatches to the routing map entry for the requested path`() {
            val orchestrator =
                StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(
                    mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
                )
            val expected = ModelAndView("view")
            val map = mapOf("task-route" to orchestrator)

            val result =
                JourneyStepDispatcher.handleInitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { map },
                    initialiseJourney = { "journey-id" },
                    dispatch = { expected },
                )

            assertEquals(expected, result)
        }

        @Test
        fun `returns the routing map observer's result instead of dispatching when it provides one`() {
            val orchestrator =
                StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(
                    mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
                )
            val map = mapOf("task-route" to orchestrator)
            val observed = ModelAndView("observed")
            var dispatched = false
            JourneyStepDispatcher.routingMapObserver = { stepPath, routingMap ->
                if (stepPath == "task-route" && routingMap == map) observed else null
            }

            val result =
                JourneyStepDispatcher.handleInitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { map },
                    initialiseJourney = { "journey-id" },
                    dispatch = {
                        dispatched = true
                        ModelAndView("view")
                    },
                )

            assertEquals(observed, result)
            assertFalse(dispatched)
        }

        @Test
        fun `dispatches as normal when the routing map observer returns null`() {
            val orchestrator =
                StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(
                    mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
                )
            val expected = ModelAndView("view")
            JourneyStepDispatcher.routingMapObserver = { _, _ -> null }

            val result =
                JourneyStepDispatcher.handleInitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { mapOf("task-route" to orchestrator) },
                    initialiseJourney = { "journey-id" },
                    dispatch = { expected },
                )

            assertEquals(expected, result)
        }

        @Test
        fun `throws 404 when the path is not in the routing map`() {
            val result =
                assertThrows<ResponseStatusException> {
                    JourneyStepDispatcher.handleInitialisableRequest(
                        rawStepPath = "/unknown",
                        createRoutingMap = { emptyMap() },
                        initialiseJourney = { "journey-id" },
                        dispatch = { ModelAndView("view") },
                    )
                }
            assertEquals(404, result.statusCode.value())
        }

        @Test
        fun `starts a new journey when no journey exists`() {
            val result =
                JourneyStepDispatcher.handleInitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { throw NoSuchJourneyException("none") },
                    initialiseJourney = { "new-journey-id" },
                    dispatch = { ModelAndView("view") },
                )

            assertEquals(
                "redirect:${JourneyStateService.urlWithJourneyState("task-route", "new-journey-id")}",
                result.viewName,
            )
        }

        @Test
        fun `throws 404 without initialising a journey when the path is empty`() {
            var initialiseCalled = false
            val result =
                assertThrows<ResponseStatusException> {
                    JourneyStepDispatcher.handleInitialisableRequest(
                        rawStepPath = "/",
                        createRoutingMap = { throw NoSuchJourneyException("none") },
                        initialiseJourney = {
                            initialiseCalled = true
                            "journey-id"
                        },
                        dispatch = { ModelAndView("view") },
                    )
                }
            assertEquals(404, result.statusCode.value())
            assertEquals(false, initialiseCalled)
        }
    }

    @Nested
    inner class HandleUninitialisableRequest {
        @Test
        fun `dispatches to the routing map entry for the requested path`() {
            val orchestrator =
                StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(
                    mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
                )
            val expected = ModelAndView("view")
            val map = mapOf("task-route" to orchestrator)

            val result =
                JourneyStepDispatcher.handleUninitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { map },
                    dispatch = { expected },
                    getRedirect = { ModelAndView("redirect:/base") },
                )

            assertEquals(expected, result)
        }

        @Test
        fun `returns the routing map observer's result instead of dispatching when it provides one`() {
            val orchestrator =
                StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(
                    mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
                )
            val observed = ModelAndView("observed")
            var dispatched = false
            JourneyStepDispatcher.routingMapObserver = { _, _ -> observed }

            val result =
                JourneyStepDispatcher.handleUninitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { mapOf("task-route" to orchestrator) },
                    dispatch = {
                        dispatched = true
                        ModelAndView("view")
                    },
                    getRedirect = { ModelAndView("redirect:/base") },
                )

            assertEquals(observed, result)
            assertFalse(dispatched)
        }

        @Test
        fun `throws 404 when the path is not in the routing map`() {
            val result =
                assertThrows<ResponseStatusException> {
                    JourneyStepDispatcher.handleUninitialisableRequest(
                        rawStepPath = "/unknown",
                        createRoutingMap = { emptyMap() },
                        dispatch = { ModelAndView("view") },
                        getRedirect = { ModelAndView("redirect:/base") },
                    )
                }
            assertEquals(404, result.statusCode.value())
        }

        @Test
        fun `redirects when no journey exists`() {
            val expected = ModelAndView("redirect:/base")

            val result =
                JourneyStepDispatcher.handleUninitialisableRequest(
                    rawStepPath = "/task-route",
                    createRoutingMap = { throw NoSuchJourneyException("none") },
                    dispatch = { ModelAndView("view") },
                    getRedirect = { expected },
                )

            assertEquals(expected, result)
        }

        @Test
        fun `throws 404 without invoking the fallback redirect when the path is empty`() {
            var redirectCalled = false
            val result =
                assertThrows<ResponseStatusException> {
                    JourneyStepDispatcher.handleUninitialisableRequest(
                        rawStepPath = "/",
                        createRoutingMap = { throw NoSuchJourneyException("none") },
                        dispatch = { ModelAndView("view") },
                        getRedirect = {
                            redirectCalled = true
                            ModelAndView("redirect:/base")
                        },
                    )
                }
            assertEquals(404, result.statusCode.value())
            assertEquals(false, redirectCalled)
        }
    }
}
