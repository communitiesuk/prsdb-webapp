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
import org.mockito.kotlin.anyOrNull
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
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.subJourney

class JourneyBuilderTest {
    @Nested
    inner class VisitableStepTests {
        lateinit var mockedStepBuilders: MockedConstruction<StepInitialiser<*, *, *>>

        @BeforeEach
        fun setup() {
            mockedStepBuilders =
                mockConstruction(StepInitialiser::class.java) { mock, context ->
                    val mockedJourneyStep = mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()
                    whenever(mockedJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)
                    whenever(mockedJourneyStep.routeSegment).thenReturn(context.arguments()[0] as String)
                    whenever((mock as StepInitialiser<*, JourneyState, *>).build(anyOrNull(), anyOrNull())).thenReturn(mockedJourneyStep)
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
                whenever(mock.build()).thenReturn(mapToReturn)
                whenever(mock.journey).thenReturn(context.arguments()[0] as JourneyState)
            }.use { mockedBuilders ->

                val state = mock<JourneyState>()

                // Act
                val journey =
                    journey(state) {
                        unreachableStepUrl { "redirect" }
                        step("segment", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
                        step("segment2", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
                    }

                // Assert
                val journeyBuilder = mockedBuilders.constructed().first()
                assertSame(state, journeyBuilder.journey)

                verify(journeyBuilder).unreachableStepUrl(any())
                verify(journeyBuilder, times(2)).step(any(), any(), any())

                verify(journeyBuilder).build()
                assertEquals(mapToReturn, journey)
            }
        }

        @Test
        fun `subJourney DSL method creates, inits but does not build a journeyBuilder`() {
            // Arrange
            val listToReturn = listOf(mock<StepInitialiser<*, JourneyState, *>>())
            mockConstruction(JourneyBuilder::class.java) { mock, context ->
                whenever(mock.getStepInitialisers()).thenReturn(listToReturn)
                whenever(mock.journey).thenReturn(context.arguments()[0] as JourneyState)
            }.use { mockedBuilders ->

                val state = mock<JourneyState>()

                // Act
                val unbuiltJourney =
                    subJourney(state) {
                        unreachableStepUrl { "redirect" }
                        step("segment", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
                        step("segment2", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
                    }

                // Assert
                val journeyBuilder = mockedBuilders.constructed().first()
                assertSame(state, journeyBuilder.journey)

                verify(journeyBuilder).unreachableStepUrl(any())
                verify(journeyBuilder, times(2)).step(any(), any(), any())

                // Zero calls to build expected here
                verify(journeyBuilder, times(0)).build()
                assertEquals(listToReturn, unbuiltJourney)
            }
        }

        @Test
        fun `an unreachableStepUrl is passed to all built step builders`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            val unreachableRedirect = "redirect"

            // Act
            jb.step("segment", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
            jb.step("segment2", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
            jb.unreachableStepUrl { unreachableRedirect }
            jb.build()

            // Assert
            val stepInitialiser1 = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            val stepInitialiser2 = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
            val captor = argumentCaptor<() -> Destination>()
            verify(stepInitialiser1).build(any(), captor.capture())
            verify(stepInitialiser2).build(any(), captor.capture())

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
            val unreachableStep = mock<JourneyStep.VisitableStep<*, *, *>>()
            whenever(unreachableStep.currentJourneyId).thenReturn("a-journey-id")

            // Act
            jb.step("segment", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
            jb.step("segment2", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}
            jb.unreachableStepStep { unreachableStep }
            jb.build()

            // Assert
            val stepInitialiser1 = mockedStepBuilders.constructed().first() as StepInitialiser<*, JourneyState, *>
            val stepInitialiser2 = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
            val captor = argumentCaptor<() -> Destination>()
            verify(stepInitialiser1).build(any(), captor.capture())
            verify(stepInitialiser2).build(any(), captor.capture())

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
            assertThrows<JourneyInitialisationException> { jb.unreachableStepStep { mock<JourneyStep.VisitableStep<*, *, *>>() } }
        }

        @Test
        fun `unreachableStepStep cannot be called twice on the same journey builder`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            jb.unreachableStepStep { mock<JourneyStep.VisitableStep<*, *, *>>() }

            // Act & Assert
            assertThrows<JourneyInitialisationException> { jb.unreachableStepUrl { "newRedirect" } }
            assertThrows<JourneyInitialisationException> { jb.unreachableStepStep { mock<JourneyStep.VisitableStep<*, *, *>>() } }
        }

        @Test
        fun `step method creates and inits a stepBuilder, which is built and included when the journey is built`() {
            // Arrange 1
            val jb = JourneyBuilder(mock())
            val uninitialisedStep = mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()

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
            val builtStep = mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()
            whenever(mockStepInitialiser.segment).thenReturn("segment")
            whenever(mockStepInitialiser.build(anyOrNull(), anyOrNull())).thenReturn(builtStep)
            whenever(builtStep.routeSegment).thenReturn("segment")

            // Act 2
            val map = jb.build()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            verify(mockStepInitialiser).build(anyOrNull(), anyOrNull())
            typedMap.entries.single().let {
                assertSame(builtStep, it.value.journeyStep)
            }
        }

        @Test
        fun `steps must only have potential parents that are initialised before them in the journey dsl`() {
            // Arrange
            val jb = JourneyBuilder(mock())
            jb.step("A", mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()) {}

            val stepInitialiser = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
            val mockJourneyStep = mock<JourneyStep.VisitableStep<TestEnum, *, JourneyState>>()
            whenever(mockJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.PARTIALLY_INITIALISED)
            whenever(mockJourneyStep.getRouteSegmentOrNull()).thenReturn("route")
            whenever(stepInitialiser.potentialParents).thenReturn(listOf(mockJourneyStep))

            // Act & Assert
            assertThrows<JourneyInitialisationException> { jb.build() }
        }
    }

    @Nested
    inner class NotionalStepTests {
        lateinit var mockedStepBuilders: MockedConstruction<StepInitialiser<*, *, *>>

        @BeforeEach
        fun setup() {
            mockedStepBuilders =
                mockConstruction(StepInitialiser::class.java) { mock, context ->
                    val mockedJourneyStep = mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>()
                    whenever(mockedJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)
                    whenever((mock as StepInitialiser<*, JourneyState, *>).build(anyOrNull(), anyOrNull())).thenReturn(mockedJourneyStep)
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
            val uninitialisedStep = mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>()

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
            val builtStep = mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>()
            whenever(mockStepInitialiser.build(anyOrNull(), anyOrNull())).thenReturn(builtStep)

            // Act 2
            val map = jb.build()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            verify(mockStepInitialiser).build(anyOrNull(), anyOrNull())
            assertTrue(typedMap.entries.isEmpty())
        }
    }

    @Test
    fun `task method creates and inits a taskInitialiser, all of whom's steps are built when the journey is built`() {
        // Arrange 1
        val jb = JourneyBuilder(mock())
        val uninitialisedTask = mock<Task<TestEnum, JourneyState>>()
        val steps =
            listOf(
                mock<StepInitialiser<*, JourneyState, *>>(),
                mock<StepInitialiser<*, JourneyState, *>>(),
                mock<StepInitialiser<*, JourneyState, *>>(),
            )
        mockConstruction(TaskInitialiser::class.java) { mock, context ->
            whenever((mock as TaskInitialiser<TestEnum, JourneyState>).mapToStepInitialisers(any())).thenReturn(steps)
        }.use { taskConstruction ->

            // Act 1
            jb.task(uninitialisedTask) {
                parents { NoParents() }
                redirectToDestination { Destination.NavigationalStep(mock()) }
            }

            // Assert 1
            val mockTaskInitialiser = taskConstruction.constructed().first() as TaskInitialiser<TestEnum, JourneyState>
            verify(mockTaskInitialiser).parents(any())
            verify(mockTaskInitialiser).redirectToDestination(any())

            // Arrange 2
            val builtSteps =
                listOf(
                    mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>(),
                    mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>(),
                    mock<JourneyStep.NotionalStep<TestEnum, *, JourneyState>>(),
                )

            steps.forEachIndexed { index, stepInitialiser ->
                whenever(stepInitialiser.build(anyOrNull(), anyOrNull())).thenReturn(builtSteps[index])
            }

            // Act 2
            val map = jb.build()

            // Assert 2
            val typedMap = objectToTypedStringKeyedMap<StepLifecycleOrchestrator>(map)!!
            steps.forEach {
                verify(it).build(anyOrNull(), anyOrNull())
            }

            typedMap.values.forEachIndexed { index, orchestrator ->
                assertSame(builtSteps[index], orchestrator.journeyStep)
            }
        }
    }
}
