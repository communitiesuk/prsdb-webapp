package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersMembersTask

interface FreeholdersState :
    JourneyState,
    FreeholdersListState,
    FreeholdersDependencies {
    val freeholdersInfoStep: FreeholdersInfoStep
    val freeholdersMembersTask: FreeholdersMembersTask
    val freeholdersBackRoutingStep: FreeholdersBackRoutingStep
}
