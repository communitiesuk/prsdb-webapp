package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep

// TODO PDJB-1957: decide which check-your-answers ticket adds the journey answers -> OwnershipType
//  mapping here.
// TODO PDJB-2015: once that mapping exists, OwnershipStructureRoutingStepConfig and
//  PropertyRegistrationDataModelFactory can map and then ask, rather than restating the rules.
interface OwnershipTypeState : JourneyState {
    val ownsPropertyStep: OwnsPropertyStep
    val ownershipKindStep: OwnershipKindStep
    val freeholdTypeStep: FreeholdTypeStep
}
