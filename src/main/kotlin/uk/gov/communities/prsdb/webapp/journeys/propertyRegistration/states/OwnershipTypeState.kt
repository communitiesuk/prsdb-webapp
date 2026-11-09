package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.helpers.converters.OwnershipTypeAnswersConverter
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep

// TODO PDJB-2015: PropertyRegistrationDataModelFactory can now map and then ask, rather than
//  restating the rules. OwnershipStructureRoutingStepConfig already does.
interface OwnershipTypeState : JourneyState {
    val ownsPropertyStep: OwnsPropertyStep
    val ownershipKindStep: OwnershipKindStep
    val freeholdTypeStep: FreeholdTypeStep

    // The journey's answers expressed as the OwnershipType we persist.
    // TODO PDJB-2015: add service layer methods to convert a stored OwnershipType back into the
    //  three question answers. Nothing repopulates the questions from a stored value yet.
    val ownershipType: OwnershipType?
        get() =
            OwnershipTypeAnswersConverter.convert(
                ownsPropertyStep.outcome,
                ownershipKindStep.outcome,
                freeholdTypeStep.outcome,
            )

    // Whether the landlord is themselves one of several freeholders, and so should be added to the
    // freeholders list.
    val isJointFreeholder: Boolean
        get() = freeholdTypeStep.outcome in setOf(FreeholdType.JOINT_TENANTS, FreeholdType.TENANTS_IN_COMMON)
}
