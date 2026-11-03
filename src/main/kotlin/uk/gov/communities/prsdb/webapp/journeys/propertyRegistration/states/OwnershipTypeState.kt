package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep

interface OwnershipTypeState : JourneyState {
    val ownsPropertyStep: OwnsPropertyStep
    val ownershipKindStep: OwnershipKindStep
    val freeholdTypeStep: FreeholdTypeStep

    // TODO: PDJB-1957: Reassess this ownership-type mapping and centralise the freeholder/joint-freeholder/superior-landlord
    // checks in one place. This logic is currently duplicated by the journey wiring and should be
    // consolidated when the single source of truth is introduced.
    val isJointFreeholder: Boolean
        get() = freeholdTypeStep.outcome in setOf(FreeholdType.JOINT_TENANTS, FreeholdType.TENANTS_IN_COMMON)
}
