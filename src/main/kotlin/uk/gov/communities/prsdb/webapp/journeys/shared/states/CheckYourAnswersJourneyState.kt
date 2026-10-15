package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.EmbedBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.TaskInitialiser
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

interface CheckYourAnswersJourneyState : JourneyState {
    val finishCyaStep: FinishCyaJourneyStep
    val cyaStep: JourneyStep.RequestableStep<*, *, *>

    var originalCyaAnswers: String?

    var cyaJourneys: Map<String, String>

    var cyaUrlPath: String?

    var returnToCyaPageDestination: Destination
        get() = cyaUrlPath?.let { Destination.StepRoute(it, baseJourneyId) } ?: Destination.Nowhere()
        set(destination) {
            cyaUrlPath =
                when (destination) {
                    is Destination.StepRoute -> destination.routeSegment
                    is Destination.VisitableStep -> destination.step.urlPath
                    else -> null
                }
        }

    val stateFactory: ObjectFactory<out CheckYourAnswersJourneyState>

    fun getBaseJourneyState(): CheckYourAnswersJourneyState {
        val id = baseJourneyId
        return stateFactory.getObject().apply { setJourneyId(id) }
    }

    fun createChildJourneyState(childJourneyId: String): CheckYourAnswersJourneyState {
        copyJourneyTo(childJourneyId)
        return stateFactory.getObject().apply { setJourneyId(childJourneyId) }
    }

    val cyaSnapshotExcludedDelegateKeys: Set<String>
        get() = emptySet()

    fun getCyaAnswersSnapshot(): String {
        val stepKeys = cyaStep.parentage.ancestry.mapNotNull { it.getUrlPathOrNull() }
        val delegateKeys =
            cyaSnapshotDelegateKeys
                .filter { unscopedDelegateKey(it) !in cyaSnapshotExcludedDelegateKeys }
                .map { CYA_SNAPSHOT_DELEGATE_KEY_PREFIX + it }
        return serializeCyaAnswers((stepKeys + delegateKeys).toSet())
    }

    private fun unscopedDelegateKey(scopedKey: String) = scopedKey.substringAfterLast('/')

    fun cyaAnswersMatchSnapshot(snapshot: String): Boolean =
        serializeCyaAnswers(CheckAnswersFormModel.deserializeJourneyDataKeys(snapshot)) == snapshot

    private fun serializeCyaAnswers(keys: Set<String>): String {
        val (delegateKeys, stepKeys) = keys.partition { it.startsWith(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX) }
        val stepAnswers = getSubmittedStepData().filterKeys { it in stepKeys }
        val delegateAnswers = delegateKeys.associateWith { getEncodedDelegateValue(it.removePrefix(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX)) }
        return CheckAnswersFormModel.serializeJourneyData((stepAnswers + delegateAnswers).toSortedMap())
    }

    fun getCyaJourneyId(checkableStep: JourneyStep.RequestableStep<*, *, *>): String {
        if (!cyaJourneys.containsKey(checkableStep.urlPath)) {
            cyaJourneys += makePair(checkableStep)
        }
        return cyaJourneys[checkableStep.urlPath]
            ?: throw IllegalStateException("CYA Journey ID should have been created for ${checkableStep.urlPath}")
    }

    private fun makePair(step: JourneyStep.RequestableStep<*, *, *>): Pair<String, String> {
        val urlPath = step.urlPath
        val cyaJourneyId = generateJourneyId("$baseJourneyId-$urlPath")
        val childJourney = createChildJourneyState(cyaJourneyId)
        childJourney.checkingAnswersFor = urlPath
        childJourney.returnToCyaPageDestination = Destination.VisitableStep(cyaStep, baseJourneyId)
        childJourney.originalCyaAnswers = getCyaAnswersSnapshot()

        return (urlPath to cyaJourneyId)
    }

    val isCheckingAnswers: Boolean
        get() = checkingAnswersFor != null

    var checkingAnswersFor: String?

    fun clearCyaFields() {
        checkingAnswersFor = null
        originalCyaAnswers = null
        cyaUrlPath = null
    }

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    companion object {
        const val CYA_SNAPSHOT_DELEGATE_KEY_PREFIX = "delegate:"

        @Suppress("ktlint:standard:max-line-length")
        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState> JourneyBuilder<TJourneyState>.checkAnswerTask(
            task: Task<TTaskState, *>,
            route: String? = null,
        ) {
            task(task) {
                route?.let { routeSegment(it) }
                initialStep()
                backDestination { journey.returnToCyaPageDestination }
                nextStep { journey.finishCyaStep }
            }
        }

        @Suppress("ktlint:standard:max-line-length")
        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState, TDependencies : Any> JourneyBuilder<TJourneyState>.checkAnswerTask(
            task: Task<TTaskState, TDependencies>,
            dependencies: () -> TDependencies,
            route: String? = null,
        ) {
            task(task) {
                route?.let { routeSegment(it) }
                withDependencies(dependencies)
                initialStep()
                backDestination { journey.returnToCyaPageDestination }
                nextStep { journey.finishCyaStep }
            }
        }

        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState> JourneyBuilder<TJourneyState>.checkAnswerTask(
            task: Task<TTaskState, *>,
            route: String? = null,
            configure: TaskInitialiser<TTaskState, *>.() -> Unit,
        ) {
            task(task) {
                route?.let { routeSegment(it) }
                initialStep()
                backDestination { journey.returnToCyaPageDestination }
                nextStep { journey.finishCyaStep }
                this.configure()
            }
        }

        @Suppress("ktlint:standard:max-line-length")
        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState> JourneyBuilder<TJourneyState>.checkAnswerTaskFrom(
            task: Task<TTaskState, *>,
            startStep: JourneyStep<*, *, *>,
            route: String? = null,
        ) {
            checkAnswerTask(task, route)
            configureStep(startStep) {
                backDestination { journey.returnToCyaPageDestination }
            }
        }

        @Suppress("ktlint:standard:max-line-length")
        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState, TDependencies : Any> JourneyBuilder<TJourneyState>.checkAnswerTaskFrom(
            task: Task<TTaskState, TDependencies>,
            dependencies: () -> TDependencies,
            startStep: JourneyStep<*, *, *>,
            route: String? = null,
        ) {
            checkAnswerTask(task, dependencies, route)
            configureStep(startStep) {
                backDestination { journey.returnToCyaPageDestination }
            }
        }

        @Suppress("ktlint:standard:max-line-length")
        fun <TJourneyState : CheckYourAnswersJourneyState, TTaskState : JourneyState> JourneyBuilder<TJourneyState>.checkAnswerTaskFrom(
            task: Task<TTaskState, *>,
            startStep: JourneyStep<*, *, *>,
            route: String? = null,
            configure: TaskInitialiser<TTaskState, *>.() -> Unit,
        ) {
            checkAnswerTask(task, route, configure)
            configureStep(startStep) {
                backDestination { journey.returnToCyaPageDestination }
            }
        }

        fun <T : CheckYourAnswersJourneyState, TMode : Enum<TMode>> JourneyBuilder<T>.checkAnswerStep(
            step: JourneyStep<TMode, *, T>,
            route: String,
        ) {
            step(step) {
                initialStep()
                nextStep { journey.finishCyaStep }
                routeSegment(route)
            }
        }

        @Suppress("ktlint:standard:max-line-length")
        fun <TEmbeddedState : JourneyState, TOuterState : CheckYourAnswersJourneyState, TMode : Enum<TMode>> EmbedBuilder<TEmbeddedState, TOuterState>.checkAnswerStep(
            step: JourneyStep<TMode, *, TEmbeddedState>,
            route: String,
        ) {
            step(step) {
                initialStep()
                nextStep { journey.finishCyaStep }
                routeSegment(route)
            }
        }

        fun <T : CheckYourAnswersJourneyState, TMode : Enum<TMode>> JourneyBuilder<T>.checkAnswerStep(
            step: JourneyStep<TMode, *, T>,
            route: String,
            configure: StepInitialiser<*, T, TMode>.() -> Unit,
        ) {
            step(step) {
                initialStep()
                nextStep { journey.finishCyaStep }
                routeSegment(route)
                this.configure()
            }
        }
    }
}
