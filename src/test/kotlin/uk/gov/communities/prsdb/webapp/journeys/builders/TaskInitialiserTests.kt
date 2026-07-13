package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.TaskRouteRedirectStep
import uk.gov.communities.prsdb.webapp.journeys.TaskRouteRedirectStepConfig
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.urlPath
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel

class TaskInitialiserTests {
    @Test
    fun `once a nextStep is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mockTask(), mock())
        builder.parents { mock() }

        // Act
        builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.nextDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `once a nextDestination is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mockTask(), mock())
        builder.parents { mock() }

        // Act
        builder.nextDestination { Destination.ExternalUrl("url") }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.nextDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `a nextStep is passed to the task's exit when built`() {
        // Arrange
        val taskMock = mockTask()

        val nextStepMock = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        val nextStepSegment = "nextStepSegment"
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)
        whenever(nextStepMock.currentJourneyId).thenReturn("journeyId")

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }
        builder.nextStep { _: SubjourneyComplete -> nextStepMock }

        // Act
        builder.build()

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit>()
        verify(taskMock).getTaskSubJourneyBuilder(
            anyOrNull(),
            initCaptor.capture(),
        )

        val initialiser = mock<StepInitialiser<SubjourneyExitStepConfig, JourneyState, SubjourneyComplete>>()
        initCaptor.firstValue.invoke(initialiser)

        val lambdaCaptor = argumentCaptor<(mode: SubjourneyComplete) -> Destination>()
        verify(initialiser).nextDestination(lambdaCaptor.capture())

        val destination = lambdaCaptor.firstValue.invoke(SubjourneyComplete.COMPLETE)
        assertTrue(destination is Destination.VisitableStep)
        with(destination as Destination.VisitableStep) {
            assertEquals(nextStepSegment, step.routeSegment)
            assertEquals("journeyId", step.currentJourneyId)
        }
    }

    @Test
    fun `a nextDestination is passed to the task's exit when built`() {
        // Arrange
        val taskMock = mockTask()

        val nextStepSegment = "nextStepSegment"

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }
        val initiationDestination = Destination.ExternalUrl(nextStepSegment)
        builder.nextDestination { _: SubjourneyComplete -> initiationDestination }

        // Act
        builder.build()

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit>()
        verify(taskMock).getTaskSubJourneyBuilder(
            anyOrNull(),
            initCaptor.capture(),
        )

        val initialiser = mock<StepInitialiser<SubjourneyExitStepConfig, JourneyState, SubjourneyComplete>>()
        initCaptor.firstValue.invoke(initialiser)

        val lambdaCaptor = argumentCaptor<(mode: SubjourneyComplete) -> Destination>()
        verify(initialiser).nextDestination(lambdaCaptor.capture())

        val finalDestination = lambdaCaptor.firstValue.invoke(SubjourneyComplete.COMPLETE)
        assertSame(initiationDestination, finalDestination)
    }

    @Test
    fun `if no destinationProvider is set, an exception is thrown when built`() {
        // Arrange
        val taskMock = mockTask()

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            builder.build()
        }
    }

    @Test
    fun `build binds the task to its route prefix`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { NoParents() }
        builder.nextDestination { Destination.ExternalUrl("url") }
        builder.routeSegment("lead-trustee-address")

        // Act
        builder.build()

        // Assert
        verify(taskMock).bindRoute("lead-trustee-address")
    }

    @Test
    fun `build binds a null route prefix when no route segment is set`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { NoParents() }
        builder.nextDestination { Destination.ExternalUrl("url") }

        // Act
        builder.build()

        // Assert
        verify(taskMock).bindRoute(null)
    }

    @Test
    fun `a parentage cannot be set more than once`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { NoParents() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.parents { NoParents() } }
    }

    @Test
    fun `a parentage is passed to the task when mapped to step initialisers`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        val parentageProvider = { NoParents() }
        builder.nextDestination { mock() }
        builder.parents(parentageProvider)

        val internalBuilder = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(internalBuilder)

        // Act
        builder.build()

        // Assert
        val firstStepInitCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(internalBuilder).configureFirst(firstStepInitCaptor.capture())

        val mockStep = mock<StepInitialiser<*, *, TestEnum>>()
        firstStepInitCaptor.firstValue.invoke(mockStep)
        verify(mockStep).parents(eq(parentageProvider))
    }

    @Test
    fun `if no parentage is set, buildSteps throws an exception`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.nextDestination { mock() }

        val internalBuilder = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(internalBuilder)

        // Act
        builder.build()

        // Assert
        val captor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(internalBuilder).configureFirst(captor.capture())
        assertThrows<JourneyInitialisationException> {
            val mockStep = mock<StepInitialiser<*, *, TestEnum>>()
            captor.firstValue.invoke(mockStep)
        }
    }

    @Test
    fun `a single additional content provider is passed to the taskSubJourney when built`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val firstKey = "firstKey"
        val firstValue = "firstValue"
        val secondKey = "secondKey"
        val secondValue = 177
        builder.withAdditionalContentProperties { mapOf(firstKey to firstValue, secondKey to secondValue) }
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        val configCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(subJourneyBuilderMock).configure(configCaptor.capture())

        val mockConfigurable = mock<ConfigurableElement<SubjourneyComplete>>()
        configCaptor.firstValue.invoke(mockConfigurable)

        val additionalContentCaptor = argumentCaptor<() -> Map<String, Any>>()
        verify(mockConfigurable).withAdditionalContentProperties(additionalContentCaptor.capture())

        val additionalContent = additionalContentCaptor.firstValue()
        assertEquals(mapOf(firstKey to firstValue, secondKey to secondValue), additionalContent)
    }

    @Test
    fun `multiple additional content providers are passed to the taskSubJourney when built`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(
            taskMock.getTaskSubJourneyBuilder(
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val firstKey = "firstKey"
        val firstValue = "firstValue"
        val secondKey = "secondKey"
        val secondValue = 177
        builder.withAdditionalContentProperty { firstKey to firstValue }
        builder.withAdditionalContentProperty { secondKey to secondValue }
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        val configCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(subJourneyBuilderMock).configure(configCaptor.capture())

        val mockConfigurable = mock<ConfigurableElement<SubjourneyComplete>>()
        configCaptor.firstValue.invoke(mockConfigurable)

        val contentCaptor = argumentCaptor<() -> Map<String, Any>>()
        verify(mockConfigurable, times(2)).withAdditionalContentProperties(contentCaptor.capture())

        val allContent = contentCaptor.allValues.map { it() }
        assertTrue(allContent.contains(mapOf(firstKey to firstValue)))
        assertTrue(allContent.contains(mapOf(secondKey to secondValue)))
    }

    @Test
    fun `setting a backDestination on a task sets the back destination on the first step in the task`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(
            taskMock.getTaskSubJourneyBuilder(
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val backDestination = Destination.ExternalUrl("backUrl")
        builder.backDestination { backDestination }
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        val firstStepConfigCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(subJourneyBuilderMock).configureFirst(firstStepConfigCaptor.capture())

        val mockStepInitialiser = mock<StepInitialiser<*, *, SubjourneyComplete>>()
        firstStepConfigCaptor.firstValue.invoke(mockStepInitialiser)

        val backDestCaptor = argumentCaptor<() -> Destination>()
        verify(mockStepInitialiser).backDestination(backDestCaptor.capture())

        val capturedBackDestination = backDestCaptor.firstValue.invoke()
        assertSame(backDestination, capturedBackDestination)
    }

    @Test
    fun `setting a custom exit step on the task initialiser sets it on the task before building`() {
        // Arrange
        val taskMock = mockTask()
        val inOrder = inOrder(taskMock)
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(
            taskMock.getTaskSubJourneyBuilder(
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val customExitStepMock = mock<SubjourneyExitStep>()
        builder.customExitStep(customExitStepMock)
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        inOrder.verify(taskMock).setCustomExitStep(eq(customExitStepMock))
        inOrder.verify(taskMock).getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())
    }

    @Test
    fun `routeSegment sets the task route as the url path prefix on each requestable step`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val stepConfig = RouteTestStepConfig()
        val step = JourneyStep.RequestableStep(stepConfig)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(step))

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("task-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        assertEquals("task-route", stepConfig.urlPathPrefix)
    }

    @Test
    fun `routeSegment prepends the task route to an existing prefix so nested routed tasks compose`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val stepConfig = RouteTestStepConfig()
        stepConfig.urlPathPrefix = "inner-route"
        val step = JourneyStep.RequestableStep(stepConfig)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(step))

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("outer-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        assertEquals("outer-route/inner-route", stepConfig.urlPathPrefix)
    }

    @Test
    fun `a task without a routeSegment does not set a url path prefix on its steps`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val stepConfig = RouteTestStepConfig()
        val step = JourneyStep.RequestableStep(stepConfig)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(step))

        val builder = TaskInitialiser(taskMock, mock())
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        assertNull(stepConfig.urlPathPrefix)
    }

    @Test
    fun `a routed task appends a landing redirect step keyed by the task route`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val realStep = JourneyStep.RequestableStep(RouteTestStepConfig())
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(realStep))
        whenever(taskMock.firstStep).thenReturn(mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>())

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("task-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        val built = builder.build()

        // Assert
        val landingStep = built.filterIsInstance<TaskRouteRedirectStep>().single()
        assertEquals("task-route", landingStep.routeSegment)
        assertNull(landingStep.urlPathPrefix)
    }

    @Test
    fun `the landing step is not double-prefixed by its own task route`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf())
        whenever(taskMock.firstStep).thenReturn(mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>())

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("task-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        val landingStep = builder.build().filterIsInstance<TaskRouteRedirectStep>().single()

        // Assert: urlPath is just the bare route, not "task-route/task-route"
        assertEquals("task-route", landingStep.urlPath)
    }

    @Test
    fun `a non-routed task does not append a landing step`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)
        val realStep = JourneyStep.RequestableStep(RouteTestStepConfig())
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(realStep))

        val builder = TaskInitialiser(taskMock, mock())
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        val built = builder.build()

        // Assert
        assertTrue(built.none { it is TaskRouteRedirectStep })
    }

    @Test
    fun `the landing step redirects to the task's first step`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf())
        val firstStep = mock<JourneyStep.InternalStep<TestEnum, JourneyState>>()
        whenever(taskMock.firstStep).thenReturn(firstStep)

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("task-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }
        val landingStep = builder.build().filterIsInstance<TaskRouteRedirectStep>().single()

        // Act
        val destination = landingStep.getNextDestination()

        // Assert: resolves to a NavigationalStep wrapping the (internal) first step
        assertTrue(destination is Destination.NavigationalStep)
        assertSame(firstStep, (destination as Destination.NavigationalStep).step)
    }

    @Test
    fun `the landing step redirects to a requestable first step as a visitable step`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf())
        val firstStep = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(firstStep.currentJourneyId).thenReturn("journey-id")
        whenever(taskMock.firstStep).thenReturn(firstStep)

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("task-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }
        val landingStep = builder.build().filterIsInstance<TaskRouteRedirectStep>().single()

        // Act
        val destination = landingStep.getNextDestination()

        // Assert: resolves to a VisitableStep wrapping the (requestable) first step
        assertTrue(destination is Destination.VisitableStep)
        assertSame(firstStep, (destination as Destination.VisitableStep).step)
    }

    @Test
    fun `an inner routed task's landing step composes with an outer task route`() {
        // Arrange: simulate an inner routed task whose landing step already carries the inner route,
        // then apply an outer task route on top.
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val innerLandingStep = TaskRouteRedirectStep(TaskRouteRedirectStepConfig())
        innerLandingStep.stepConfig.routeSegment = "inner-route"
        whenever(subJourneyBuilderMock.build(any())).thenReturn(listOf<JourneyStep<*, *, *>>(innerLandingStep))
        whenever(taskMock.firstStep).thenReturn(mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>())

        val builder = TaskInitialiser(taskMock, mock())
        builder.routeSegment("outer-route")
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        assertEquals("outer-route", innerLandingStep.urlPathPrefix)
        assertEquals("outer-route/inner-route", innerLandingStep.urlPath)
    }

    private class RouteTestFormModel : FormModel

    private class RouteTestStepConfig : AbstractRequestableStepConfig<TestEnum, RouteTestFormModel, JourneyState>() {
        override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> = mapOf()

        override fun chooseTemplate(state: JourneyState): String = "template"

        override val formModelClass = RouteTestFormModel::class

        override fun mode(state: JourneyState): TestEnum = TestEnum.ENUM_VALUE
    }

    private fun mockTask(): Task<JourneyState> =
        mock<Task<JourneyState>>().apply {
            whenever(
                getTaskSubJourneyBuilder(
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(mock<SubJourneyBuilder<JourneyState>>())
        }

    // End-to-end checks that the shared DelegateKeyRegistry threaded through JourneyBuilder.buildRoutingMap catches
    // delegate-key collisions ACROSS providers (journey state vs task, task vs task) - the case a single provider's
    // own duplicate-key guard cannot see.
    @Nested
    inner class CollisionRegistryTests {
        @Test
        fun `a route-less task key colliding with a journey state key throws when the journey builds`() {
            val builder = JourneyBuilder(stateRegisteringKey("shared-key"))
            builder.duplicableTask(KeyedSelfStatedTask("shared-key"), routeSegment = null) {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }

            assertThrows<JourneyInitialisationException> { builder.buildRoutingMap() }
        }

        @Test
        fun `two tasks under the same route registering the same key collide when the journey builds`() {
            val builder = JourneyBuilder(mock<JourneyState>())
            builder.duplicableTask(KeyedSelfStatedTask("cached"), "same-route") {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }
            builder.duplicableTask(KeyedSelfStatedTask("cached"), "same-route") {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }

            assertThrows<JourneyInitialisationException> { builder.buildRoutingMap() }
        }

        @Test
        fun `two tasks under distinct routes registering the same key build without collision`() {
            val builder = JourneyBuilder(mock<JourneyState>())
            builder.duplicableTask(KeyedSelfStatedTask("cached"), "route-one") {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }
            builder.duplicableTask(KeyedSelfStatedTask("cached"), "route-two") {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }

            assertDoesNotThrow { builder.buildRoutingMap() }
        }

        @Test
        fun `a nested self-stated task registers its key scoped by its route so a matching state key collides`() {
            // The inner task under route "inner" registers "cached" as "inner/cached"; a root state key of the same
            // scoped form collides, proving the nested task's keys reach the shared registry through the nested build.
            val builder = JourneyBuilder(stateRegisteringKey("inner/cached"))
            builder.task(taskContaining(KeyedSelfStatedTask("cached"), innerRoute = "inner")) {
                parents { NoParents() }
                nextDestination { Destination.ExternalUrl("done") }
            }

            assertThrows<JourneyInitialisationException> { builder.buildRoutingMap() }
        }

        // A journey root state that registers a single delegate key, so its keys can be collided against a task's.
        private fun stateRegisteringKey(key: String): AbstractJourneyState =
            object : AbstractJourneyState(mock()) {
                @Suppress("unused")
                val registeredValue: String? by delegateProvider.nullableDelegate(key)
            }

        // A journey-stated (route-less) task whose sub-journey nests a self-stated task under innerRoute, so the
        // registry threading through the nested build can be exercised.
        private fun taskContaining(
            inner: DuplicableTask<JourneyState>,
            innerRoute: String,
        ): Task<JourneyState> =
            object : Task<JourneyState>() {
                override fun makeSubJourney(state: JourneyState) =
                    subJourney(state) {
                        duplicableTask(inner, innerRoute) {
                            parents { NoParents() }
                            nextDestination { Destination.ExternalUrl("inner-done") }
                        }
                        exitStep { parents { NoParents() } }
                        unreachableStepUrl { "unreachable" }
                    }
            }

        // A minimal self-stated task that registers a single route-scoped delegate key and builds one real step.
        private inner class KeyedSelfStatedTask(
            key: String,
        ) : DuplicableTask<JourneyState>(mock()) {
            @Suppress("unused")
            val cachedValue: String? by delegateProvider.nullableDelegate(key)

            override val taskState: JourneyState get() = this

            override fun makeSubJourney(state: JourneyState) =
                subJourney(state) {
                    step(JourneyStep.RequestableStep(RouteTestStepConfig())) {
                        routeSegment("step")
                        nextUrl { "url" }
                    }
                    exitStep { parents { NoParents() } }
                    unreachableStepUrl { "unreachable" }
                }
        }
    }
}
