package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep

interface CheckYourAnswersJourneyState : JourneyState {
    val finishCyaStep: FinishCyaJourneyStep
    val cyaStep: JourneyStep.RequestableStep<*, *, *>

    var cyaJourneys: Map<String, String>

    var returnToCyaPageDestination: Destination

    fun getBaseJourneyState(): CheckYourAnswersJourneyState

    fun getCyaJourneyId(checkableStep: JourneyStep.RequestableStep<*, *, *>): String =
        cyaJourneys[checkableStep.routeSegment]
            ?: throw IllegalStateException("No journey found for checkable element ${checkableStep.routeSegment}")

    val isCheckingAnswers: Boolean
        get() = checkingAnswersFor != null

    var checkingAnswersFor: String?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    fun initialiseCyaChildJourneys(vararg checkableSteps: JourneyStep.RequestableStep<*, *, *>) {
        val newMap =
            checkableSteps
                .map { step ->
                    val routeSegment = step.routeSegment
                    val cyaJourneyId = generateJourneyId("$routeSegment for $journeyId")
                    val childJourney = createChildJourneyState(cyaJourneyId)
                    childJourney.checkingAnswersFor = step.routeSegment
                    childJourney.returnToCyaPageDestination = Destination.VisitableStep(cyaStep, baseJourneyId)
                    routeSegment to cyaJourneyId
                }.associate { it }
        cyaJourneys = newMap
    }

    fun createChildJourneyState(cyaJourneyId: String): CheckYourAnswersJourneyState

    companion object {
        fun <T : CheckYourAnswersJourneyState> JourneyBuilder<T>.checkAnswerTask(task: Task<T>) {
            task(task) {
                initialStep()
                nextStep { journey.finishCyaStep }
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
    }
}
