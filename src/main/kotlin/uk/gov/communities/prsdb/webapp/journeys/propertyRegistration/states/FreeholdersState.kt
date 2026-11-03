package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDetailsTask

interface FreeholdersState :
    JourneyState,
    FreeholdersListState {
    val freeholdersInfoStep: FreeholdersInfoStep
    val freeholdersDetailsTask: FreeholdersDetailsTask
    val freeholdersBackRoutingStep: FreeholdersBackRoutingStep
    val isJointFreeholder: Boolean
}
