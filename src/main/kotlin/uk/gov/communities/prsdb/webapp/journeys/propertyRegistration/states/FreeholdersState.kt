package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
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
    val freeholdersDetailsTask: FreeholdersDetailsTask
    val freeholdersBackRoutingStep: FreeholdersBackRoutingStep
}

// Only call this once the ownership type is known to require freeholder records, as otherwise none are collected.
fun FreeholdersState.getFreeholders(): Map<Int, FreeholderDataModel> =
    freeholdersMap?.takeIf { it.isNotEmpty() }
        ?: throw UnrecoverableJourneyStateException(
            journeyId,
            "The freeholders were read for an ownership type requiring freeholder records, but none were collected",
        )
