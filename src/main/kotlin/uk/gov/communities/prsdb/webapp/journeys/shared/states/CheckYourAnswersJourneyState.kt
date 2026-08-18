package uk.gov.communities.prsdb.webapp.journeys.shared.states

import kotlinx.datetime.Instant
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

interface CheckYourAnswersJourneyState : JourneyState {
    val finishCyaStep: FinishCyaJourneyStep
    val cyaStep: JourneyStep.RequestableStep<*, *, *>

    var originalJourneyUpdated: Instant?

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
        childJourney.originalJourneyUpdated = journeyMetadata.lastUpdated

        return (urlPath to cyaJourneyId)
    }

    val isCheckingAnswers: Boolean
        get() = checkingAnswersFor != null

    var checkingAnswersFor: String?

    fun clearCyaFields() {
        checkingAnswersFor = null
        originalJourneyUpdated = null
        cyaUrlPath = null
    }

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    companion object {
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
