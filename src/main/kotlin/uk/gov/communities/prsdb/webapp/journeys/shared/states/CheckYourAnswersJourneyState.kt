package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.springframework.beans.factory.ObjectFactory
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

    var cyaRouteSegment: String?

    var returnToCyaPageDestination: Destination
        get() = cyaRouteSegment?.let { Destination.StepRoute(it, baseJourneyId) } ?: Destination.Nowhere()
        set(destination) {
            cyaRouteSegment =
                when (destination) {
                    is Destination.StepRoute -> destination.routeSegment
                    is Destination.VisitableStep -> destination.step.routeSegment
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
        if (!cyaJourneys.containsKey(checkableStep.routeSegment)) {
            cyaJourneys += makePair(checkableStep)
        }
        return cyaJourneys[checkableStep.routeSegment]
            ?: throw IllegalStateException("CYA Journey ID should have been created for ${checkableStep.routeSegment}")
    }

    private fun makePair(step: JourneyStep.RequestableStep<*, *, *>): Pair<String, String> {
        val routeSegment = step.routeSegment
        val cyaJourneyId = generateJourneyId("$routeSegment for $journeyId")
        val childJourney = createChildJourneyState(cyaJourneyId)
        childJourney.checkingAnswersFor = routeSegment
        childJourney.returnToCyaPageDestination = Destination.VisitableStep(cyaStep, baseJourneyId)
        return (routeSegment to cyaJourneyId)
    }

    val isCheckingAnswers: Boolean
        get() = checkingAnswersFor != null

    var checkingAnswersFor: String?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

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
