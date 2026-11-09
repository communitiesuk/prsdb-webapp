package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// TODO PDJB-2015: PropertyRegistrationDataModelFactory can now map and then ask, rather than
//  restating the rules. OwnershipStructureRoutingStepConfig already does.
interface OwnershipTypeState : JourneyState {
    val ownsPropertyStep: OwnsPropertyStep
    val ownershipKindStep: OwnershipKindStep
    val freeholdTypeStep: FreeholdTypeStep

    // The journey's answers expressed as the OwnershipType we persist. Null until enough of the
    // ownership type task has been answered to determine it, which is exactly when the task is
    // complete (see OwnershipTypeTask.exitStep).
    // TODO PDJB-2015: add service layer methods to convert a stored OwnershipType back into the
    //  three question answers. Nothing repopulates the questions from a stored value yet.
    val ownershipType: OwnershipType?
        get() =
            when (ownsPropertyStep.outcome) {
                YesOrNo.NO -> OwnershipType.NONE
                YesOrNo.YES ->
                    when (ownershipKindStep.outcome) {
                        OwnershipKind.LEASEHOLD_ONLY -> OwnershipType.LEASEHOLD_ONLY
                        OwnershipKind.FREEHOLD_ONLY ->
                            when (freeholdTypeStep.outcome) {
                                FreeholdType.SOLE_OWNERSHIP -> OwnershipType.SOLE_FREEHOLD
                                FreeholdType.JOINT_TENANTS -> OwnershipType.JOINT_TENANTS
                                FreeholdType.TENANTS_IN_COMMON -> OwnershipType.TENANTS_IN_COMMON
                                FreeholdType.COMMONHOLD -> OwnershipType.COMMONHOLD
                                null -> null
                            }
                        OwnershipKind.FREEHOLD_AND_LEASEHOLD ->
                            when (freeholdTypeStep.outcome) {
                                FreeholdType.SOLE_OWNERSHIP -> OwnershipType.SHARE_OF_SOLE_FREEHOLD
                                FreeholdType.JOINT_TENANTS -> OwnershipType.SHARE_OF_JOINT_TENANTS
                                FreeholdType.TENANTS_IN_COMMON -> OwnershipType.SHARE_OF_TENANTS_IN_COMMON
                                FreeholdType.COMMONHOLD -> OwnershipType.SHARE_OF_COMMONHOLD
                                null -> null
                            }
                        null -> null
                    }
                null -> null
            }

    // Whether the landlord is themselves one of several freeholders, and so should be added to the
    // freeholders list.
    val isJointFreeholder: Boolean
        get() = freeholdTypeStep.outcome in setOf(FreeholdType.JOINT_TENANTS, FreeholdType.TENANTS_IN_COMMON)
}
