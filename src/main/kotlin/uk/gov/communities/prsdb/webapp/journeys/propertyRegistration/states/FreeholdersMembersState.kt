package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForFreeholderEditStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask

interface FreeholdersMembersState :
    JourneyState,
    FreeholdersListState {
    val freeholderProvideDetailsStep: FreeholderProvideDetailsStep
    val freeholderNameStep: FreeholderNameStep
    val freeholderContactTypeStep: FreeholderContactTypeStep
    val freeholderEmailStep: FreeholderEmailStep
    val freeholderAddressTask: FreeholderAddressTask
    val freeholdersListStep: FreeholdersListStep
    val hasAnyFreeholdersStep: HasAnyFreeholdersStep
    val saveFreeholderStep: SaveFreeholderStep
    val setStateForFreeholderEditStep: SetStateForFreeholderEditStep
    val removeFreeholderConfirmationStep: FreeholderRemoveConfirmationStep
    val allowRemovingLastMember: Boolean
}

class FreeholdersMembersDependencies(
    // The state slice that owns the collected freeholders. The reusable members task reads and writes the
    // freeholders list through this, so the list can live in the enclosing outer task or journey.
    val listState: FreeholdersListState,
    val allowRemovingLastMember: Boolean = true,
)
