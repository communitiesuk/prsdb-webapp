package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

interface CheckYourAnswersJourneyState : JourneyState {
    val cyaStep: AbstractCheckYourAnswersStep<*>

    var cyaChildJourneyIdIfInitialized: String?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    val isCheckingAnswers: Boolean
        get() = journeyMetadata.baseJourneyId == journeyId

    fun initialiseCyaChildJourney() {
        TODO()
    }

    companion object {
        fun <T : CheckYourAnswersJourneyState> JourneyBuilder<T>.checkYourAnswersJourney() {
            configureTagged(CHECKABLE) {
                if (journey.isCheckingAnswers) {
                    modifyNextDestination {
                        { Destination.VisitableStep(journey.cyaStep, journey.baseJourneyId) }
                    }
                    backDestination { Destination.VisitableStep(journey.cyaStep, journey.baseJourneyId) }
                }
            }
        }

        fun ConfigurableElement<*>.checkable() = taggedWith(CHECKABLE)

        private const val CHECKABLE = ReservedTagValues.CHECKABLE
        private const val CHECK_ANSWERS_JOURNEY_NAME = "checkYourAnswers"
    }
}

interface CheckYourAnswersJourneyState2<TCheckableElements : Enum<TCheckableElements>> : JourneyState {
    val finishCyaStep: FinishCyaJourneyStep<TCheckableElements>
    val cyaStep: JourneyStep.RequestableStep<*, *, *>

    var cyaJourneys: Map<TCheckableElements, String>

    fun getCyaJourneyId(checkableElement: TCheckableElements): String =
        cyaJourneys[checkableElement] ?: throw IllegalStateException("No journey found for checkable element $checkableElement")

    val isCheckingAnswers: Boolean
        get() = checkingAnswersFor != null

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
    }

    fun createChildJourneyState(cyaJourneyId: String): CheckYourAnswersJourneyState2<TCheckableElements>

    companion object {
        fun <T : CheckYourAnswersJourneyState2<*>> JourneyBuilder<T>.checkAnswerTask(task: Task<T>) {
            task(task) {
                initialStep()
                nextStep { journey.finishCyaStep }
            }
        }

        fun <T : CheckYourAnswersJourneyState2<*>, TMode : Enum<TMode>> JourneyBuilder<T>.checkAnswerStep(
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
