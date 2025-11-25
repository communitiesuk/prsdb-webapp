package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.objectToTypedStringKeyedMap
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey

class SubJourneyBuilderTests {
    @Test
    fun `If no exitStep is set, then getSteps throws as exception`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        val step = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(step.initialisationStage).thenReturn(StepInitialisationStage.UNINITIALISED)
        subJourneyBuilder.subJourneyParent(NoParents())
        subJourneyBuilder.startingStep(
            "segment",
            step,
        ) {
            nextUrl { "url" }
        }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            subJourneyBuilder.buildSteps()
        }
    }

    @Test
    fun `If an exitStep has already been set, then setting it again throws as exception`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        subJourneyBuilder.exitStep {
            parents { NoParents() }
        }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            subJourneyBuilder.exitStep {
                parents { NoParents() }
            }
        }
    }

    @Test
    fun `The exitStep set is passed to the sub journeys parentage`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        val exitStep = subJourneyBuilder.exitStep
        subJourneyBuilder.subJourneyParent(NoParents())

        val step = StepInitialiserTests.mockInitialisableStep()
        subJourneyBuilder.startingStep("segment", step) { nextUrl { "url" } }

        val parent = NoParents()

        // Act
        subJourneyBuilder.exitStep {
            parents { parent }
        }

        subJourneyBuilder.exitInitialiser?.nextUrl { "url" }
        subJourneyBuilder.unreachableStepUrl { "url" }
        val resultingStep = subJourneyBuilder.buildSteps().last()

        // Assert
        assertSame(exitStep, resultingStep)
    }

    @Test
    fun `If no starting step is set, then getSteps throws as exception`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        subJourneyBuilder.exitStep {
            parents { NoParents() }
        }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            subJourneyBuilder.buildSteps()
        }
    }

    @Test
    fun `If a starting step has already been set, then setting it again throws as exception`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        val step = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(step.initialisationStage).thenReturn(StepInitialisationStage.UNINITIALISED)
        subJourneyBuilder.subJourneyParent(NoParents())
        subJourneyBuilder.startingStep(
            "segment",
            step,
        ) {
            nextUrl { "url" }
        }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            subJourneyBuilder.startingStep(
                "segment2",
                mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>(),
            ) {
                nextUrl { "url" }
            }
        }
    }

    @Test
    fun `startingStep sets the first step of the sub-journey`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        val step = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(step.initialisationStage).thenReturn(StepInitialisationStage.UNINITIALISED)
        subJourneyBuilder.subJourneyParent(NoParents())

        // Act
        subJourneyBuilder.startingStep(
            "segment",
            step,
        ) {
            nextUrl { "url" }
        }

        // Assert
        assertSame(step, subJourneyBuilder.firstStep)
    }

    @Test
    fun `subJourneyParent sets the parentage of the sub-journey first step`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        val parentage = NoParents()

        val step = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(step.initialisationStage).thenReturn(StepInitialisationStage.UNINITIALISED)

        mockConstruction(StepInitialiser::class.java) { mock, context ->
        }.use { constructed ->
            // Act
            subJourneyBuilder.subJourneyParent(parentage)
            subJourneyBuilder.startingStep(
                "segment",
                step,
            ) {
                nextUrl { "url" }
            }

            // Assert
            constructed.constructed().first().let {
                val captor = argumentCaptor<() -> Parentage>()
                verify(it).parents(captor.capture())
                assertSame(parentage, captor.firstValue())
            }
        }
    }

    @Test
    fun `subJourneyParent cannot be called twice`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())
        subJourneyBuilder.subJourneyParent(NoParents())

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            subJourneyBuilder.subJourneyParent(NoParents())
        }
    }

    @Test
    fun `If no subJourneyParentage is set, then startingStep throws an exception`() {
        // Arrange
        val subJourneyBuilder = SubJourneyBuilder(mock())

        val step = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(step.initialisationStage).thenReturn(StepInitialisationStage.UNINITIALISED)

        mockConstruction(StepInitialiser::class.java) { mock, context ->
        }.use { constructed ->
            // Act & Assert
            assertThrows<JourneyInitialisationException> {
                subJourneyBuilder.startingStep(
                    "segment",
                    step,
                ) {
                    nextUrl { "url" }
                }
            }
        }
    }
}

class JourneyBuilderTest {
    @Nested
    inner class VisitableStepTests {
        lateinit var mockedStepBuilders: MockedConstruction<StepInitialiser<*, *, *>>

        @BeforeEach
        fun setup() {
            mockedStepBuilders =
                mockConstruction(StepInitialiser::class.java) { mock, context ->
                    val mockedJourneyStep = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
                    whenever(mockedJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)
                    whenever(mockedJourneyStep.routeSegment).thenReturn(context.arguments()[0] as String)
                    whenever((mock as StepInitialiser<*, *, *>).buildSteps()).thenReturn(listOf(mockedJourneyStep))
                    whenever(mock.configureSteps(any())).thenCallRealMethod()
                }
        }

        @AfterEach
        fun tearDown() {
            mockedStepBuilders.close()
        }

        @Test
        fun `journey DSL method creates, inits and builds a journeyBuilder`() {
            // Arrange
            val mapToReturn = mapOf("key" to mock<StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator>())
            mockConstruction(JourneyBuilder::class.java) { mock, context ->
                whenever(mock.buildRoutingMap()).thenReturn(mapToReturn)
                whenever(mock.journey).thenReturn(context.arguments()[0] as JourneyState)
            }.use { mockedBuilders ->

                val state = mock<JourneyState>()

                // Act
                val journey =
                    journey(state) {
                        unreachableStepUrl { "redirect" }
                        step("segment", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
                        step("segment2", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
                    }

                // Assert
                val journeyBuilder = mockedBuilders.constructed().first()
                assertSame(state, journeyBuilder.journey)

                verify(journeyBuilder).unreachableStepUrl(any())
                verify(journeyBuilder, times(2)).step(any(), any(), any())

                verify(journeyBuilder).buildRoutingMap()
                assertEquals(mapToReturn, journey)
            }
        }

        @Test
        fun `an unreachableStepUrl is passed to all built step builders`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            val unreachableRedirect = "redirect"

            // Act
            jb.step("segment", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
            jb.step("segment2", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
            jb.unreachableStepUrl { unreachableRedirect }
            jb.buildRoutingMap()

            // Assert
            val stepInitialiser1 = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            val stepInitialiser2 = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
            val captor = argumentCaptor<() -> Destination>()
            verify(stepInitialiser1).buildSteps()
            verify(stepInitialiser2).buildSteps()

            captor.allValues.forEach {
                val destination = it()
                assert(destination is Destination.ExternalUrl)
                with(destination as Destination.ExternalUrl) {
                    assertEquals("redirect", externalUrl)
                }
            }
        }

        @Test
        fun `an unreachableStepStep is passed to all built step builders`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            val unreachableStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(unreachableStep.currentJourneyId).thenReturn("a-journey-id")

            // Act
            jb.step("segment", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
            jb.step("segment2", mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()) {}
            jb.unreachableStepStep { unreachableStep }
            jb.buildRoutingMap()

            // Assert
            val stepInitialiser1 = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            val stepInitialiser2 = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
            val captor = argumentCaptor<() -> Destination>()
            verify(stepInitialiser1).unreachableStepDestinationIfNotSet(captor.capture())
            verify(stepInitialiser2).unreachableStepDestinationIfNotSet(captor.capture())

            captor.allValues.forEach {
                val destination = it()
                assert(destination is Destination.VisitableStep)
                with(destination as Destination.VisitableStep) {
                    assertSame(unreachableStep, step)
                }
            }
        }

        @Test
        fun `unreachableStepDestination cannot be called after unreachableStepUrl on the same journey builder`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            jb.unreachableStepUrl { "redirect" }

            // Act & Assert
            assertThrows<JourneyInitialisationException> { jb.unreachableStepUrl { "newRedirect" } }
            assertThrows<JourneyInitialisationException> { jb.unreachableStepStep { mock<JourneyStep.RequestableStep<*, *, *>>() } }
        }

        @Test
        fun `unreachableStepStep cannot be called twice on the same journey builder`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            jb.unreachableStepStep { mock<JourneyStep.RequestableStep<*, *, *>>() }

            // Act & Assert
            assertThrows<JourneyInitialisationException> { jb.unreachableStepUrl { "newRedirect" } }
            assertThrows<JourneyInitialisationException> { jb.unreachableStepStep { mock<JourneyStep.RequestableStep<*, *, *>>() } }
        }

        @Test
        fun `step method creates and inits a stepBuilder, which is built and included when the journey is built`() {
            // Arrange 1
            val jb = JourneyBuilder(mock())
            val uninitialisedStep = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()

            // Act 1
            jb.step("segment", uninitialisedStep) {
                backUrl { "back" }
                stepSpecificInitialisation { }
            }

            // Assert 1
            val mockStepInitialiser = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            verify(mockStepInitialiser).backUrl(any())
            verify(mockStepInitialiser).stepSpecificInitialisation(any())

            // Arrange 2
            val builtStep = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
            whenever(mockStepInitialiser.segment).thenReturn("segment")
            whenever(mockStepInitialiser.buildSteps()).thenReturn(listOf(builtStep))
            whenever(builtStep.routeSegment).thenReturn("segment")

            // Act 2
            val map = jb.buildRoutingMap()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            verify(mockStepInitialiser).buildSteps()
            typedMap.entries.single().let {
                assertSame(builtStep, it.value.journeyStep)
            }
        }
    }

    @Nested
    inner class NotionalStepTests {
        lateinit var mockedStepBuilders: MockedConstruction<StepInitialiser<*, *, *>>

        @BeforeEach
        fun setup() {
            mockedStepBuilders =
                mockConstruction(StepInitialiser::class.java) { mock, context ->
                    val mockedJourneyStep = mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>()
                    whenever(mockedJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)
                    whenever((mock as StepInitialiser<*, JourneyState, *>).buildSteps()).thenReturn(listOf(mockedJourneyStep))
                }
        }

        @AfterEach
        fun tearDown() {
            mockedStepBuilders.close()
        }

        @Test
        fun `notionalStep method creates and inits a stepBuilder, which is built and excluded when the journey is built`() {
            // Arrange 1
            val jb = JourneyBuilder(mock())
            val uninitialisedStep = mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>()

            // Act 1
            jb.notionalStep(uninitialisedStep) {
                backUrl { "back" }
                stepSpecificInitialisation { }
            }

            // Assert 1
            val mockStepInitialiser = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            verify(mockStepInitialiser).backUrl(any())
            verify(mockStepInitialiser).stepSpecificInitialisation(any())

            // Arrange 2
            val builtStep = mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>()
            whenever(mockStepInitialiser.buildSteps()).thenReturn(listOf(builtStep))

            // Act 2
            val map = jb.buildRoutingMap()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            verify(mockStepInitialiser).buildSteps()
            assertTrue(typedMap.entries.isEmpty())
        }
    }

    @Test
    fun `task method creates and inits a taskInitialiser, all of whom's steps are built when the journey is built`() {
        // Arrange 1
        val jb = JourneyBuilder(mock())
        val uninitialisedTask = mock<Task<JourneyState>>()

        val builtSteps =
            listOf(
                mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>(),
                mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>(),
                mock<JourneyStep.InternalStep<TestEnum, *, JourneyState>>(),
            )

        mockConstruction(TaskInitialiser::class.java) { mock, context ->
            whenever((mock as TaskInitialiser<JourneyState>).buildSteps()).thenReturn(builtSteps)
        }.use { taskConstruction ->

            // Act 1
            jb.task(uninitialisedTask) {
                parents { NoParents() }
                nextDestination { Destination.NavigationalStep(mock()) }
            }

            // Assert 1
            val mockTaskInitialiser = taskConstruction.constructed().first() as TaskInitialiser<JourneyState>
            verify(mockTaskInitialiser).parents(any())
            verify(mockTaskInitialiser).nextDestination(any())

            // Act 2
            val map = jb.buildRoutingMap()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            verify(mockTaskInitialiser).buildSteps()

            typedMap.values.forEachIndexed { index, orchestrator ->
                assertSame(builtSteps[index], orchestrator.journeyStep)
            }
        }
    }
}
