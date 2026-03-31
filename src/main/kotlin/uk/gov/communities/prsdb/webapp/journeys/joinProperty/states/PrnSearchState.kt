package uk.gov.communities.prsdb.webapp.journeys.joinProperty.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PrnNotFoundStep

interface PrnSearchState : JourneyState {
    val findPropertyByPrnStep: FindPropertyByPrnStep
    val prnNotFoundStep: PrnNotFoundStep
}
