package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

// Minimal TaskWithoutDependencies fixture: makeSubJourney delegates to the base subJourney helper so we can
// capture the constructed SubJourneyBuilder in SubJourneyBuilderBehaviourTests.
private class SubJourneyTestTask(
    journeyStateService: JourneyStateService,
) : TaskWithoutDependencies<JourneyState>(journeyStateService) {
    override fun makeSubJourney(state: JourneyState) = subJourney(state) { }

    override val taskState: JourneyState get() = this
}

// Minimal route-scoped task exposing a single route-scoped cached variable. makeSubJourney is never exercised
// by RouteScopingBehaviourTests, which target the route-scoping and state-delegation behaviour only.
private class RouteScopingTestTask(
    journeyStateService: JourneyStateService,
) : TaskWithoutDependencies<JourneyState>(journeyStateService) {
    var cachedThing: String? by delegateProvider.nullableDelegate("cachedThing")

    override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError("not needed for these tests")

    override val taskState: JourneyState get() = this
}

// The typed contract a task reaches the enclosing state through.
private interface PocDependencies {
    var sharedValue: String?
}

// Stands in for the enclosing journey/sibling state that satisfies the contract.
private class EnclosingStateHolder : PocDependencies {
    override var sharedValue: String? = null
}

// A task that declares a dependency contract. makeSubJourney is never exercised here.
private class PocTask(
    journeyStateService: JourneyStateService,
) : Task<JourneyState, PocDependencies>(journeyStateService) {
    override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError("not needed for these tests")

    override val taskState: JourneyState get() = this
}

class TaskTests {
    @Nested
    inner class SubJourneyBuilderBehaviourTests {
        private val journeyStateService: JourneyStateService = mock()

        private lateinit var subJourneyConstruction: MockedConstruction<SubJourneyBuilder<*>>
        private val firstStepMock = mock<JourneyStep.RequestableStep<*, *, JourneyState>>()
        private val exitStepMock = mock<SubjourneyExitStep>()

        @BeforeEach
        fun setup() {
            // Mock construction of SubJourneyBuilder to capture the init lambda
            subJourneyConstruction =
                mockConstruction(SubJourneyBuilder::class.java) { mock, _ ->
                    whenever(mock.firstStep).thenReturn(firstStepMock)
                    whenever(mock.exitStep).thenReturn(exitStepMock)
                }
        }

        @AfterEach
        fun teardown() {
            subJourneyConstruction.close()
        }

        @Test
        fun `getTaskSubJourneyBuilder inits the sub journey builder and returns the steps from it`() {
            // Arrange
            val task = SubJourneyTestTask(journeyStateService)

            val nextDestinationLambda = { _: SubjourneyComplete -> Destination.ExternalUrl("example.com") }
            val state = mock<JourneyState>()
            val parent = NoParents()

            // Act
            val subJourneyBuilder =
                task.getTaskSubJourneyBuilder(state) {
                    parents { parent }
                    nextDestination(nextDestinationLambda)
                }

            // Assert
            assertSame(subJourneyConstruction.constructed().first(), subJourneyBuilder)
        }

        @Test
        fun `when the first step of a task is not reachable, the taskStatus is CANNOT_START`() {
            whenever(firstStepMock.isStepReachable).thenReturn(false)
            val task = initialisedTask()

            val status = task.taskStatus()

            assertEquals(TaskStatus.CANNOT_START, status)
        }

        @Test
        fun `when the first step of a task is reachable and the first step's outcome is null, the taskStatus is NOT_STARTED`() {
            whenever(firstStepMock.isStepReachable).thenReturn(true)
            whenever(firstStepMock.outcome).thenReturn(null)
            val task = initialisedTask()

            val status = task.taskStatus()

            assertEquals(TaskStatus.NOT_STARTED, status)
        }

        @Test
        fun `when the first step of a task is complete and the task is not complete, the taskStatus is IN_PROGRESS`() {
            whenever(firstStepMock.isStepReachable).thenReturn(true)
            whenever(firstStepMock.outcome).thenReturn(Complete.COMPLETE)
            val task = initialisedTask()

            val status = task.taskStatus()

            assertEquals(TaskStatus.IN_PROGRESS, status)
        }

        @Test
        fun `when the task is complete, the taskStatus is COMPLETED`() {
            whenever(exitStepMock.isStepReachable).thenReturn(true)
            val task = initialisedTask()

            val status = task.taskStatus()

            assertEquals(TaskStatus.COMPLETED, status)
        }

        @Test
        fun `when a taskStatus override is set, it is used instead of the default`() {
            whenever(firstStepMock.isStepReachable).thenReturn(true)
            whenever(firstStepMock.outcome).thenReturn(Complete.COMPLETE)
            val task = initialisedTask()
            whenever(task.subJourneyBuilder.taskStatusOverride).thenReturn { TaskStatus.NOT_STARTED }

            val status = task.taskStatus()

            assertEquals(TaskStatus.NOT_STARTED, status)
        }

        @Test
        fun `exitStep returns a TaskExitStep from the internal task builder`() {
            val task = initialisedTask()

            val step = task.exitStep

            assertSame(exitStepMock, step)
        }

        private fun initialisedTask(): SubJourneyTestTask {
            val task = SubJourneyTestTask(journeyStateService)
            task.getTaskSubJourneyBuilder(mock()) {
                nextUrl { "example.com" }
            }
            return task
        }
    }

    @Nested
    inner class RouteScopingBehaviourTests {
        private val session = mutableMapOf<String, Any?>()
        private lateinit var journeyStateService: JourneyStateService

        @BeforeEach
        fun setUp() {
            // Back getValue/setValue with a real map so we can observe exactly which keys the cached-variable delegates
            // read and write.
            journeyStateService = mock()
            whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
            doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
                .whenever(journeyStateService)
                .setValue(any(), anyOrNull())
        }

        private fun taskFor(route: String?): RouteScopingTestTask = RouteScopingTestTask(journeyStateService).apply { bindRoute(route) }

        @Test
        fun `a bound route prefixes the cached variable key`() {
            val task = taskFor("some-route")

            task.cachedThing = "10 Downing Street"

            assertTrue(session.containsKey("some-route/cachedThing"))
        }

        @Test
        fun `a null route leaves the cached variable key bare`() {
            val task = taskFor(null)

            task.cachedThing = "10 Downing Street"

            assertTrue(session.containsKey("cachedThing"))
        }

        @Test
        fun `two instances on different routes keep their cached variables separate`() {
            val leadTrusteeAddress = taskFor("lead-trustee-address")
            val ownAddress = taskFor(null)

            leadTrusteeAddress.cachedThing = "10 Downing Street"

            assertEquals("10 Downing Street", leadTrusteeAddress.cachedThing)
            assertNull(ownAddress.cachedThing)
        }

        @Test
        fun `addStepData stores under a bare key even when a route is bound`() {
            val task = taskFor("some-route")

            task.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

            // The route must NOT scope step data - it is delegated straight to the service under the bare key.
            verify(journeyStateService).addSingleStepData(eq("lookup-address"), any())
        }

        @Test
        fun `getStepData reads a bare key even when a route is bound`() {
            whenever(journeyStateService.getSubmittedStepData())
                .thenReturn(mapOf("lookup-address" to mapOf("postcode" to "SW1A 2AA")))
            val task = taskFor("some-route")

            assertEquals("SW1A 2AA", task.getStepData("lookup-address")?.get("postcode"))
            assertNull(task.getStepData("some-route/lookup-address"))
        }

        @Test
        fun `getSubmittedStepData passes through unscoped`() {
            val stepData = mapOf("lookup-address" to mapOf("postcode" to "SW1A 2AA"))
            whenever(journeyStateService.getSubmittedStepData()).thenReturn(stepData)
            val task = taskFor("some-route")

            assertEquals(stepData, task.getSubmittedStepData())
        }

        @Test
        fun `bindKeyRegistry registers the task's cached variable key in its route-scoped form`() {
            val registry = DelegateKeyRegistry()
            val task = taskFor("some-route")

            task.bindKeyRegistry(registry)

            // The registry now owns the route-scoped key, so re-registering it collides.
            assertThrows<JourneyInitialisationException> { registry.register("some-route/cachedThing") }
        }

        @Test
        fun `bindKeyRegistry registers a bare cached variable key when no route is bound`() {
            val registry = DelegateKeyRegistry()
            val task = taskFor(null)

            task.bindKeyRegistry(registry)

            assertThrows<JourneyInitialisationException> { registry.register("cachedThing") }
        }
    }

    @Nested
    inner class DependenciesBehaviourTests {
        private val session = mutableMapOf<String, Any?>()
        private lateinit var journeyStateService: JourneyStateService

        @BeforeEach
        fun setUp() {
            journeyStateService = mock()
            whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
            doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
                .whenever(journeyStateService)
                .setValue(any(), anyOrNull())
        }

        @Test
        fun `reading a bound dependency reflects later mutations to the enclosing state`() {
            val enclosing = EnclosingStateHolder()
            val task = PocTask(journeyStateService).apply { bindDependencies(enclosing) }

            enclosing.sharedValue = "mutated-after-binding"

            assertEquals("mutated-after-binding", task.dependencies.sharedValue)
        }

        @Test
        fun `writing through a bound dependency updates the enclosing state`() {
            val enclosing = EnclosingStateHolder()
            val task = PocTask(journeyStateService).apply { bindDependencies(enclosing) }

            task.dependencies.sharedValue = "written-through-task"

            assertEquals("written-through-task", enclosing.sharedValue)
        }

        @Test
        fun `writing through a dependency does not create a task-route-scoped session key`() {
            val enclosing = EnclosingStateHolder()
            val task =
                PocTask(journeyStateService).apply {
                    bindRoute("poc-route")
                    bindDependencies(enclosing)
                }

            task.dependencies.sharedValue = "no-session-write"

            assertFalse(session.keys.any { it.contains("sharedValue") })
        }

        @Test
        fun `binding dependencies twice throws`() {
            val task = PocTask(journeyStateService).apply { bindDependencies(EnclosingStateHolder()) }

            assertThrows<JourneyInitialisationException> { task.bindDependencies(EnclosingStateHolder()) }
        }

        @Test
        fun `accessing dependencies before binding throws`() {
            val task = PocTask(journeyStateService)

            assertThrows<UninitializedPropertyAccessException> { task.dependencies }
        }

        @Test
        fun `a with-dependencies task requires binding`() {
            assertTrue(PocTask(journeyStateService).requiresDependencies)
        }

        @Test
        fun `a plain TaskWithoutDependencies does not require dependencies`() {
            val plain =
                object : TaskWithoutDependencies<JourneyState>(journeyStateService) {
                    override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError()

                    override val taskState: JourneyState
                        get() = this
                }

            assertFalse(plain.requiresDependencies)
        }
    }
}
