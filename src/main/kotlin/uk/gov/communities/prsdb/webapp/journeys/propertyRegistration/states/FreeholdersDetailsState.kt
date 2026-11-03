package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForFreeholderEditStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask

interface FreeholdersDetailsState :
    FreeholdersState {
    val freeholderProvideDetailsStep: ProvideFreeholderDetailsStep
    val freeholderNameStep: FreeholderNameStep
    val freeholderContactTypeStep: FreeholderContactTypeStep
    val freeholderEmailStep: FreeholderEmailStep
    val freeholderAddressTask: FreeholderAddressTask
    val freeholdersListStep: FreeholdersListStep
    val hasAnyFreeholdersStep: HasAnyFreeholdersStep
    val saveFreeholderStep: SaveFreeholderStep
    val setStateForFreeholderEditStep: SetStateForFreeholderEditStep
    val confirmRemoveFreeholderStep: ConfirmRemoveFreeholderStep
    val allowRemovingLastMember: Boolean
}

class FreeholdersDetailsDependencies(
    // The state slice that owns the collected freeholders. The reusable members task reads and writes the
    // freeholders list through this, so the list can live in the enclosing outer task or journey.
    val listState: FreeholdersListState,
    // TOOD: PDJB-1993: Set this to false on the update journey
    val allowRemovingLastMember: Boolean = true,
)
