package uk.gov.communities.prsdb.webapp.journeys.shared

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

interface CheckYourAnswersJourneyState : JourneyState {
    val cyaStep: JourneyStep.RequestableStep<Complete, CheckAnswersFormModel, *>
    val baseJourneyId: String
    val cyaChildJourneyId: String?
    val isCheckingAnswers: Boolean
}

fun <T : CheckYourAnswersJourneyState> JourneyBuilder<T>.checkYourAnswersJourney() {
    configureTagged("checkable") {
        modifyNextDestination { originalDestinationProvider ->
            if (journey.isCheckingAnswers) {
                { Destination.VisitableStep(journey.cyaStep, journey.baseJourneyId) }
            } else {
                originalDestinationProvider
            }
        }
    }
}

fun ConfigurableElement<*>.checkable() = taggedWith("checkable")
