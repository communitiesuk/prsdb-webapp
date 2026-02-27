package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep

interface CheckYourAnswersJourneyState<TCheckableElements : Enum<TCheckableElements>> : JourneyState {
    val finishCyaStep: FinishCyaJourneyStep<TCheckableElements>
    val cyaStep: JourneyStep.RequestableStep<*, *, *>

    var cyaJourneys: Map<TCheckableElements, String>

    var returnToCyaPageDestination: Destination

    fun getCyaJourneyId(checkableElement: TCheckableElements): String =
        cyaJourneys[checkableElement] ?: throw IllegalStateException("No journey found for checkable element $checkableElement")

    var checkingAnswersFor: TCheckableElements?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    fun initialiseCyaChildJourney(
        cyaJourneyId: String,
        checkableElement: TCheckableElements,
    ) {
        cyaJourneys += (checkableElement to cyaJourneyId)
        val childJourney = createChildJourneyState(cyaJourneyId)
        childJourney.checkingAnswersFor = checkableElement
        childJourney.returnToCyaPageDestination = Destination.VisitableStep(cyaStep, baseJourneyId)
    }

    fun createChildJourneyState(cyaJourneyId: String): CheckYourAnswersJourneyState<TCheckableElements>

    companion object {
        fun <T : CheckYourAnswersJourneyState<*>> JourneyBuilder<T>.checkAnswerTask(task: Task<T>) {
            task(task) {
                initialStep()
                nextStep { journey.finishCyaStep }
            }
        }

        fun <T : CheckYourAnswersJourneyState<*>, TMode : Enum<TMode>> JourneyBuilder<T>.checkAnswerStep(
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
