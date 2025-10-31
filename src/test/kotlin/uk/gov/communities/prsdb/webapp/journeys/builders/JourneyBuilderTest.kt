package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.Destination

class JourneyBuilderTest {
    lateinit var mockedStepBuilders: MockedConstruction<StepInitialiser<*, *, *>>

    @BeforeEach
    fun setup() {
        mockedStepBuilders =
            mockConstruction(StepInitialiser::class.java) { mock, context ->
                val mockedJourneyStep = mock<JourneyStep<TestEnum, *, JourneyState>>()
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
        val mapToReturn = mapOf<String, StepLifecycleOrchestrator>("key" to mock())
        mockConstruction(JourneyBuilder::class.java) { mock, context ->
            whenever(mock.build()).thenReturn(mapToReturn)
            whenever(mock.journey).thenReturn(context.arguments()[0] as JourneyState)
        }.use { mockedBuilders ->

            val state = mock<JourneyState>()

            // Act
            val journey =
                journey(state) {
                    unreachableStepUrl { "redirect" }
                    step("segment", mock()) {}
                    step("segment2", mock()) {}
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
    fun `an unreachableStepUrl is passed to all built step builders`() {
        // Arrange
        val jb = JourneyBuilder(mock())
        val redirectLambda = { "redirect" }

        // Act
        jb.step("segment", mock()) {}
        jb.step("segment2", mock()) {}
        jb.unreachableStepUrl(redirectLambda)
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
    fun `unreachableStepRedirect cannot be called twice on the same journey builder`() {
        // Arrange
        val jb = JourneyBuilder(mock())
        jb.unreachableStepUrl { "redirect" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { jb.unreachableStepUrl { "newRedirect" } }
    }

    @Test
    fun `step method creates and inits a stepBuilder, which is built when the journey is built`() {
        // Arrange 1
        val jb = JourneyBuilder(mock())
        val uninitialisedStep = mock<JourneyStep<TestEnum, *, JourneyState>>()

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
        val builtStep = mock<JourneyStep<TestEnum, *, JourneyState>>()
        whenever(mockStepInitialiser.build(anyOrNull(), anyOrNull())).thenReturn(builtStep)

        // Act 2
        val map = jb.build()

        // Assert 2
        verify(mockStepInitialiser).build(anyOrNull(), anyOrNull())
        map.entries.single().let {
            assertSame(builtStep, it.value.journeyStep)
        }
    }

    @Test
    fun `steps must only have potential parents that are initialised before them in the journey dsl`() {
        // Arrange
        val jb = JourneyBuilder(mock())
        jb.step("A", mock<JourneyStep<TestEnum, *, JourneyState>>()) {}

        val stepInitialiser = mockedStepBuilders.constructed().last() as StepInitialiser<*, JourneyState, *>
        val mockJourneyStep = mock<JourneyStep<TestEnum, *, JourneyState>>()
        whenever(mockJourneyStep.initialisationStage).thenReturn(StepInitialisationStage.PARTIALLY_INITIALISED)
        whenever(mockJourneyStep.routeSegment).thenReturn("route")
        whenever(stepInitialiser.potentialParents).thenReturn(listOf(mockJourneyStep))

        // Act & Assert
        assertThrows<JourneyInitialisationException> { jb.build() }
    }
}
