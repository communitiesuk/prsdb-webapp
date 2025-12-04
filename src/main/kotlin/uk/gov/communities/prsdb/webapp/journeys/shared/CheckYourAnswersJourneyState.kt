package uk.gov.communities.prsdb.webapp.journeys.shared

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

interface CheckYourAnswersJourneyState : JourneyState {
    val cyaStep: JourneyStep.RequestableStep<Complete, CheckAnswersFormModel, *>
    val cyaChildJourneyId: String?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    val isCheckingAnswers: Boolean
        get() = journeyMetadata.childJourneyName != null

    companion object {
        fun <T> JourneyBuilder<T>.checkYourAnswersJourney() where T : JourneyState, T : CheckYourAnswersJourneyState {
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

        private const val CHECKABLE = "checkable"
    }
}
