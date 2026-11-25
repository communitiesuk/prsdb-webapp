package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AddLoggedInLandlordFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

interface FreeholdersState :
    JourneyState,
    FreeholdersListState,
    FreeholdersDependencies {
    val freeholdersInfoStep: FreeholdersInfoStep
    val addLoggedInLandlordFreeholderStep: AddLoggedInLandlordFreeholderStep
    val freeholdersDetailsTask: FreeholdersDetailsTask
    val freeholdersBackRoutingStep: FreeholdersBackRoutingStep

    // Set once the logged-in landlord's own freeholder card has been added, so it isn't added again if they move
    // back and forth. It's ignored once the list has been emptied, so restarting the task adds them again.
    var loggedInLandlordFreeholderInitialised: Boolean?
}

// Null if none were collected, which is an impossible state for an ownership type requiring freeholder records.
fun FreeholdersState.getFreeholdersOrNull(): Map<Int, FreeholderDataModel>? = freeholdersMap?.takeIf { it.isNotEmpty() }
