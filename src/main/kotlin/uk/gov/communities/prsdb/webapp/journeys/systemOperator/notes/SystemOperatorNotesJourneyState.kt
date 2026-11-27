package uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes

import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService

interface SystemOperatorNotesJourneyState : JourneyState {
    val notesStep: AbstractSystemOperatorNotesStep
    val completeNotesUpdateStep: AbstractCompleteSystemOperatorNotesUpdateStep
    val recordId: Long
    val lastModifiedDate: String
    var notesInitialised: Boolean?
}

abstract class AbstractSystemOperatorNotesJourney(
    journeyStateService: JourneyStateService,
    journeyName: String,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    SystemOperatorNotesJourneyState {
    override var recordId: Long by delegateProvider.requiredImmutableDelegate("recordId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var notesInitialised: Boolean? by delegateProvider.nullableDelegate("notesInitialised")
}
