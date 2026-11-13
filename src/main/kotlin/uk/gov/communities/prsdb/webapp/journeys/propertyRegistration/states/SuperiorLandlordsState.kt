package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

interface SuperiorLandlordsState :
    JourneyState,
    SuperiorLandlordsListState {
    val anyoneInOwnershipChainStep: AnyoneInOwnershipChainStep
    val superiorLandlordsDetailsTask: SuperiorLandlordsDetailsTask
    val superiorLandlordsBackRoutingStep: SuperiorLandlordsBackRoutingStep
}

// Only call this once the ownership type is known to require superior landlord info, as otherwise the step is never asked.
val SuperiorLandlordsState.anyoneInOwnershipChain: Boolean
    get() =
        anyoneInOwnershipChainStep.formModelIfReachableOrNull?.anyoneInOwnershipChain
            ?: throw UnrecoverableJourneyStateException(
                journeyId,
                "The ownership chain was read before the anyone in ownership chain step was answered",
            )

// Only call this once anyoneInOwnershipChain is known to be true, as otherwise no superior landlords are collected.
fun SuperiorLandlordsState.getSuperiorLandlords(): Map<Int, SuperiorLandlordDataModel> =
    superiorLandlordsMap?.takeIf { it.isNotEmpty() }
        ?: throw UnrecoverableJourneyStateException(
            journeyId,
            "The superior landlords were read with anyone in ownership chain answered yes, but none were collected",
        )
