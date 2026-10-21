package uk.gov.communities.prsdb.webapp.journeys.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyMetadata
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.CYA_SNAPSHOT_DELEGATE_KEY_PREFIX
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

class CheckYourAnswersJourneyStateTests {
    @Nested
    inner class IsCheckingAnswers {
        @Test
        fun `returns true when checkingAnswersFor is not null`() {
            val state = createTestState(checkingAnswersFor = "some-step")
            assertTrue(state.isCheckingAnswers)
        }

        @Test
        fun `returns false when checkingAnswersFor is null`() {
            val state = createTestState(checkingAnswersFor = null)
            assertFalse(state.isCheckingAnswers)
        }
    }

    @Nested
    inner class BaseJourneyId {
        @Test
        fun `returns baseJourneyId from journeyMetadata when it is set`() {
            val state = createTestState(journeyMetadata = JourneyMetadata.createNew("dataKey", baseJourneyId = "base-id"))
            assertEquals("base-id", state.baseJourneyId)
        }

        @Test
        fun `returns journeyId when journeyMetadata baseJourneyId is null`() {
            val state = createTestState(journeyId = "current-id", journeyMetadata = JourneyMetadata.createNew("dataKey"))
            assertEquals("current-id", state.baseJourneyId)
        }
    }

    @Nested
    inner class ReturnToCyaPageDestination {
        @Test
        fun `getter returns StepRoute when cyaRouteSegment is set`() {
            val state =
                createTestState(
                    cyaRouteSegment = "check-answers",
                    journeyMetadata = JourneyMetadata.createNew("dataKey", baseJourneyId = "base-id"),
                )
            val destination = state.returnToCyaPageDestination

            assertTrue(destination is Destination.StepRoute)
            with(destination as Destination.StepRoute) {
                assertEquals("check-answers", routeSegment)
                assertEquals("base-id", journeyId)
            }
        }

        @Test
        fun `getter returns Nowhere when cyaRouteSegment is null`() {
            val state = createTestState(cyaRouteSegment = null)
            assertTrue(state.returnToCyaPageDestination is Destination.Nowhere)
        }

        @Test
        fun `setter extracts routeSegment from a StepRoute destination`() {
            val state = createTestState()
            state.returnToCyaPageDestination = Destination.StepRoute("my-route", "some-journey-id")
            assertEquals("my-route", state.cyaUrlPath)
        }

        @Test
        fun `setter extracts routeSegment from a VisitableStep destination`() {
            val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(mockStep.urlPath).thenReturn("step-route")

            val state = createTestState()
            state.returnToCyaPageDestination = Destination.VisitableStep(mockStep, "some-journey-id")
            assertEquals("step-route", state.cyaUrlPath)
        }

        @Test
        fun `setter sets cyaRouteSegment to null for other destination types`() {
            val state = createTestState(cyaRouteSegment = "existing-route")
            state.returnToCyaPageDestination = Destination.Nowhere()
            assertEquals(null, state.cyaUrlPath)
        }
    }

    @Nested
    inner class GetCyaJourneyId {
        @Test
        fun `returns existing journey ID if one already exists for the step`() {
            val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(mockStep.urlPath).thenReturn("existing-step")

            val state = createTestState(cyaJourneys = mutableMapOf("existing-step" to "existing-cya-journey-id"))
            val result = state.getCyaJourneyId(mockStep)

            assertEquals("existing-cya-journey-id", result)
        }

        @Test
        fun `creates a new journey ID if one does not exist for the step`() {
            val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(mockStep.urlPath).thenReturn("new-step")

            val mockCyaStep = mockCyaStep()

            val state = createTestState(cyaJourneys = mutableMapOf(), cyaStep = mockCyaStep)
            val result = state.getCyaJourneyId(mockStep)

            assertNotNull(result)
            assertTrue(state.cyaJourneys.containsKey("new-step"))
        }

        @Test
        fun `sets checkingAnswersFor on the child journey state`() {
            val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(mockStep.urlPath).thenReturn("new-step")

            val mockCyaStep = mockCyaStep()

            val state = createTestState(cyaJourneys = mutableMapOf(), cyaStep = mockCyaStep)
            state.getCyaJourneyId(mockStep)

            assertEquals("new-step", state.childState?.checkingAnswersFor)
        }

        @Test
        fun `sets returnToCyaPageDestination on the child journey state`() {
            val mockStep = mock<JourneyStep.RequestableStep<*, *, *>>()
            whenever(mockStep.urlPath).thenReturn("new-step")

            val mockCyaStep = mockCyaStep()

            val originalJourneyId = "base-id"

            val state =
                createTestState(
                    journeyId = originalJourneyId,
                    cyaJourneys = mutableMapOf(),
                    cyaStep = mockCyaStep,
                    journeyMetadata = JourneyMetadata.createNew("dataKey", baseJourneyId = originalJourneyId),
                )
            state.getCyaJourneyId(mockStep)

            val childDestination = state.childState?.returnToCyaPageDestination
            assertTrue(childDestination is Destination.StepRoute)
            with(childDestination as Destination.StepRoute) {
                assertEquals(mockCyaStep.urlPath, routeSegment)
                assertEquals(originalJourneyId, journeyId)
            }
        }

        @Test
        fun `sets originalCyaAnswers on the child journey state to the answers shown on the CYA page`() {
            val state =
                createTestState(
                    cyaJourneys = mutableMapOf(),
                    cyaStep = mockCyaStep(mockRequestableStep("step-a")),
                    submittedStepData = mapOf("step-a" to mapOf("field" to "a"), "check-answers" to mapOf<String, Any?>()),
                )

            state.getCyaJourneyId(mockRequestableStep("step-a"))

            val expectedSnapshot = CheckAnswersFormModel.serializeJourneyDataHashes(mapOf("step-a" to mapOf("field" to "a")))
            assertEquals(expectedSnapshot, state.childState?.originalCyaAnswers)
        }
    }

    @Nested
    inner class GetCyaAnswersSnapshot {
        @Test
        fun `contains only the submitted data of the CYA step's ancestors, sorted by key`() {
            val state =
                createTestState(
                    cyaStep = mockCyaStep(mockRequestableStep("step-b"), mockRequestableStep("step-a")),
                    submittedStepData =
                        mapOf(
                            "step-b" to mapOf("field" to "b"),
                            "check-answers" to mapOf<String, Any?>(),
                            "step-a" to mapOf("field" to "a"),
                            "payment-summary" to mapOf("field" to "later progress"),
                        ),
                )

            val expectedSnapshot =
                CheckAnswersFormModel.serializeJourneyDataHashes(
                    linkedMapOf("step-a" to mapOf("field" to "a"), "step-b" to mapOf("field" to "b")),
                )
            assertEquals(expectedSnapshot, state.getCyaAnswersSnapshot())
        }

        @Test
        fun `ignores ancestors that are internal steps`() {
            val state =
                createTestState(
                    cyaStep = mockCyaStep(mock<JourneyStep.InternalStep<*, *>>(), mockRequestableStep("step-a")),
                    submittedStepData = mapOf("step-a" to mapOf("field" to "a")),
                )

            val expectedSnapshot = CheckAnswersFormModel.serializeJourneyDataHashes(mapOf("step-a" to mapOf("field" to "a")))
            assertEquals(expectedSnapshot, state.getCyaAnswersSnapshot())
        }

        @Test
        fun `contains the encoded values of CYA snapshot delegates, including unset ones, sorted by key`() {
            val state =
                createTestState(
                    cyaStep = mockCyaStep(mockRequestableStep("step-a")),
                    submittedStepData = mapOf("step-a" to mapOf("field" to "a")),
                    delegateKeys = setOf("task/unsetAnswer", "task/answer"),
                    delegateValues = mapOf("task/answer" to "{\"1\":\"value\"}", "paymentQuote" to "later progress"),
                )

            val expectedSnapshot =
                CheckAnswersFormModel.serializeJourneyDataHashes(
                    linkedMapOf(
                        "${CYA_SNAPSHOT_DELEGATE_KEY_PREFIX}task/answer" to "{\"1\":\"value\"}",
                        "${CYA_SNAPSHOT_DELEGATE_KEY_PREFIX}task/unsetAnswer" to null,
                        "step-a" to mapOf("field" to "a"),
                    ),
                )
            assertEquals(expectedSnapshot, state.getCyaAnswersSnapshot())
        }

        @Test
        fun `leaves out delegates whose full key is excluded by the journey`() {
            val state =
                createTestState(
                    cyaStep = mockCyaStep(mockRequestableStep("step-a")),
                    submittedStepData = mapOf("step-a" to mapOf("field" to "a")),
                    delegateKeys = setOf("task/answer", "paymentQuote", "other-task/paymentReference", "paymentReference"),
                    cyaSnapshotExcludedDelegateKeys = setOf("paymentQuote", "other-task/paymentReference"),
                    delegateValues =
                        mapOf(
                            "task/answer" to "{\"1\":\"value\"}",
                            "paymentQuote" to "later progress",
                            "other-task/paymentReference" to "\"reference\"",
                            "paymentReference" to "\"another reference\"",
                        ),
                )

            val expectedSnapshot =
                CheckAnswersFormModel.serializeJourneyDataHashes(
                    linkedMapOf(
                        "${CYA_SNAPSHOT_DELEGATE_KEY_PREFIX}paymentReference" to "\"another reference\"",
                        "${CYA_SNAPSHOT_DELEGATE_KEY_PREFIX}task/answer" to "{\"1\":\"value\"}",
                        "step-a" to mapOf("field" to "a"),
                    ),
                )
            assertEquals(expectedSnapshot, state.getCyaAnswersSnapshot())
        }

        @Test
        fun `throws when an excluded key isn't a delegate key in the journey`() {
            val state =
                createTestState(
                    cyaStep = mockCyaStep(mockRequestableStep("step-a")),
                    delegateKeys = setOf("other-task/paymentReference"),
                    cyaSnapshotExcludedDelegateKeys = setOf("paymentReference"),
                )

            assertThrows<JourneyInitialisationException> { state.getCyaAnswersSnapshot() }
        }
    }

    @Nested
    inner class CyaAnswersMatchSnapshot {
        private val snapshot = CheckAnswersFormModel.serializeJourneyDataHashes(mapOf("step-a" to mapOf("field" to "a")))

        @Test
        fun `returns true when only data outside the snapshot has changed`() {
            val state =
                createTestState(
                    submittedStepData =
                        mapOf(
                            "step-a" to mapOf("field" to "a"),
                            "check-answers" to mapOf<String, Any?>(),
                            "payment-summary" to mapOf("field" to "later progress"),
                        ),
                    delegateValues = mapOf("paymentQuote" to "later progress"),
                )

            assertTrue(state.cyaAnswersMatchSnapshot(snapshot))
        }

        @Test
        fun `returns true when the CYA snapshot delegates are unchanged`() {
            val snapshotWithDelegates = createStateWithSnapshotDelegate("{\"1\":\"value\"}").getCyaAnswersSnapshot()
            val state = createStateWithSnapshotDelegate("{\"1\":\"value\"}")

            assertTrue(state.cyaAnswersMatchSnapshot(snapshotWithDelegates))
        }

        @Test
        fun `returns false when a CYA snapshot delegate has changed`() {
            val snapshotWithDelegates = createStateWithSnapshotDelegate("{\"1\":\"value\"}").getCyaAnswersSnapshot()
            val state = createStateWithSnapshotDelegate("{\"1\":\"value\",\"2\":\"another value\"}")

            assertFalse(state.cyaAnswersMatchSnapshot(snapshotWithDelegates))
        }

        @Test
        fun `returns false when an unset CYA snapshot delegate has been set`() {
            val snapshotWithDelegates = createStateWithSnapshotDelegate(null).getCyaAnswersSnapshot()
            val state = createStateWithSnapshotDelegate("{\"1\":\"value\"}")

            assertFalse(state.cyaAnswersMatchSnapshot(snapshotWithDelegates))
        }

        private fun createStateWithSnapshotDelegate(answerValue: String?) =
            createTestState(
                cyaStep = mockCyaStep(mockRequestableStep("step-a")),
                submittedStepData = mapOf("step-a" to mapOf("field" to "a")),
                delegateKeys = setOf("task/answer"),
                delegateValues = mapOf("task/answer" to answerValue),
            )

        @Test
        fun `returns false when an answer in the snapshot has changed`() {
            val state = createTestState(submittedStepData = mapOf("step-a" to mapOf("field" to "changed")))

            assertFalse(state.cyaAnswersMatchSnapshot(snapshot))
        }

        @Test
        fun `returns false when an answer in the snapshot has been removed`() {
            val state = createTestState(submittedStepData = emptyMap())

            assertFalse(state.cyaAnswersMatchSnapshot(snapshot))
        }
    }

    private fun mockRequestableStep(urlPath: String): JourneyStep.RequestableStep<*, *, *> {
        val step = mock<JourneyStep.RequestableStep<*, *, *>>()
        whenever(step.urlPath).thenReturn(urlPath)
        whenever(step.getUrlPathOrNull()).thenReturn(urlPath)
        return step
    }

    private fun mockCyaStep(vararg ancestors: JourneyStep<*, *, *>): JourneyStep.RequestableStep<*, *, *> {
        val parentage = mock<Parentage>()
        whenever(parentage.ancestry).thenReturn(ancestors.toList())
        val cyaStep = mockRequestableStep("check-answers")
        whenever(cyaStep.parentage).thenReturn(parentage)
        return cyaStep
    }

    private fun createTestState(
        journeyId: String = "test-journey-id",
        journeyMetadata: JourneyMetadata = JourneyMetadata.createNew("dataKey"),
        checkingAnswersFor: String? = null,
        cyaRouteSegment: String? = null,
        cyaJourneys: MutableMap<String, String> = mutableMapOf(),
        cyaStep: JourneyStep.RequestableStep<*, *, *> = mockCyaStep(),
        submittedStepData: Map<String, Any?> = emptyMap(),
        delegateKeys: Set<String> = emptySet(),
        cyaSnapshotExcludedDelegateKeys: Set<String> = emptySet(),
        delegateValues: Map<String, Any?> = emptyMap(),
    ): TestCheckYourAnswersJourneyState =
        TestCheckYourAnswersJourneyState(
            testJourneyId = journeyId,
            testJourneyMetadata = journeyMetadata,
            initialCheckingAnswersFor = checkingAnswersFor,
            initialCyaRouteSegment = cyaRouteSegment,
            initialCyaJourneys = cyaJourneys,
            testCyaStep = cyaStep,
            testSubmittedStepData = submittedStepData,
            testDelegateKeys = delegateKeys,
            testCyaSnapshotExcludedDelegateKeys = cyaSnapshotExcludedDelegateKeys,
            testDelegateValues = delegateValues,
        )

    class TestCheckYourAnswersJourneyState(
        private val testJourneyId: String = "test-journey-id",
        private val testJourneyMetadata: JourneyMetadata = JourneyMetadata.createNew("dataKey"),
        initialCheckingAnswersFor: String? = null,
        initialCyaRouteSegment: String? = null,
        initialCyaJourneys: MutableMap<String, String> = mutableMapOf(),
        private val testCyaStep: JourneyStep.RequestableStep<*, *, *> = mock(),
        private val testSubmittedStepData: Map<String, Any?> = emptyMap(),
        private val testDelegateKeys: Set<String> = emptySet(),
        private val testCyaSnapshotExcludedDelegateKeys: Set<String> = emptySet(),
        private val testDelegateValues: Map<String, Any?> = emptyMap(),
    ) : CheckYourAnswersJourneyState {
        override val finishCyaStep: FinishCyaJourneyStep = mock()
        override val cyaStep: JourneyStep.RequestableStep<*, *, *> = testCyaStep
        override var originalCyaAnswers: String? = null
        override var cyaJourneys: Map<String, String> = initialCyaJourneys
        override var cyaUrlPath: String? = initialCyaRouteSegment
        override var checkingAnswersFor: String? = initialCheckingAnswersFor

        override val journeyId: String = testJourneyId
        override val journeyMetadata: JourneyMetadata = testJourneyMetadata
        override val stateFactory: ObjectFactory<TestCheckYourAnswersJourneyState> =
            mock<ObjectFactory<TestCheckYourAnswersJourneyState>>().apply {
                whenever(getObject()).thenAnswer {
                    val child =
                        TestCheckYourAnswersJourneyState(
                            testJourneyMetadata = JourneyMetadata.createNew("dataKey", baseJourneyId = journeyId),
                            testCyaStep = testCyaStep,
                        )
                    childState = child
                    child
                }
            }

        var childState: TestCheckYourAnswersJourneyState? = null

        override fun getBaseJourneyState(): CheckYourAnswersJourneyState = this

        override fun createChildJourneyState(childJourneyId: String): CheckYourAnswersJourneyState {
            val child =
                TestCheckYourAnswersJourneyState(
                    testJourneyId = childJourneyId,
                    testJourneyMetadata = JourneyMetadata.createNew("dataKey", baseJourneyId = journeyId),
                    testCyaStep = testCyaStep,
                )
            childState = child
            return child
        }

        override fun generateJourneyId(seed: Any?): String = "generated-${seed.hashCode()}"

        override fun getStepData(key: String) = null

        override fun addStepData(
            key: String,
            value: Map<String, Any?>,
        ) {}

        override fun clearStepData(key: String) {}

        override fun getSubmittedStepData(): Map<String, Any?> = testSubmittedStepData

        override val delegateKeys: Set<String> = testDelegateKeys

        override val cyaSnapshotExcludedDelegateKeys: Set<String> = testCyaSnapshotExcludedDelegateKeys

        override fun getEncodedDelegateValue(key: String): Any? = testDelegateValues[key]

        override fun deleteJourney() {}

        override fun initializeState(seed: Any?): String = "state-id"

        override fun initializeOrRestoreState(seed: Any?): String = "state-id"

        override fun save() = mock<uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState>()

        override fun setJourneyId(newJourneyId: String) {}

        override fun copyJourneyTo(newJourneyId: String) {}

        override fun bindKeyRegistry(registry: DelegateKeyRegistry) {}
    }
}
