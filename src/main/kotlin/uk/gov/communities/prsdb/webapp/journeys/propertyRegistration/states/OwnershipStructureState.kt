package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipStructureRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipTypeTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask

interface OwnershipStructureState :
    JourneyState,
    FreeholdersDependencies {
    val ownershipTypeTask: OwnershipTypeTask
    val ownershipStructureRoutingStep: OwnershipStructureRoutingStep
    val freeholdersTask: FreeholdersTask
    val superiorLandlordsTask: SuperiorLandlordsTask
}
