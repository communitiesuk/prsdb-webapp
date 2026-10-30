package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnySuperiorLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveSuperiorLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForSuperiorLandlordEditStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordAddressTask

interface SuperiorLandlordsMembersState :
    JourneyState,
    SuperiorLandlordsListState {
    val superiorLandlordProvideDetailsStep: PersonProvideDetailsStep
    val superiorLandlordNameStep: PersonNameStep
    val superiorLandlordContactTypeStep: PersonContactTypeStep
    val superiorLandlordEmailStep: PersonEmailStep
    val superiorLandlordAddressTask: SuperiorLandlordAddressTask
    val superiorLandlordsListStep: SuperiorLandlordsListStep
    val hasAnySuperiorLandlordsStep: HasAnySuperiorLandlordsStep
    val saveSuperiorLandlordStep: SaveSuperiorLandlordStep
    val setStateForSuperiorLandlordEditStep: SetStateForSuperiorLandlordEditStep
    val removeSuperiorLandlordStep: PersonRemoveConfirmationStep
    val allowRemovingLastMember: Boolean
}

class SuperiorLandlordsMembersDependencies(
    // The state slice that owns the collected superior landlords. The reusable members task reads and writes the
    // superior landlords list through this, so the list can live in the enclosing outer task or journey.
    val listState: SuperiorLandlordsListState,
    val allowRemovingLastMember: Boolean = true,
)
