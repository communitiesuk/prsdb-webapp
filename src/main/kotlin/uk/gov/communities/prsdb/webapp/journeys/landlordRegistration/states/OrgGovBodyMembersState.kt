package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.HasAnyGovBodyMembersStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.RemoveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SaveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SetStateForGovBodyMemberEditStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

interface OrgGovBodyMembersState : JourneyState {
    val orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep
    val orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep
    val orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep
    val govBodyMemberAddressTask: AddressTask
    val orgGovBodyMemberListStep: OrgGovBodyMemberListStep
    val hasAnyGovBodyMembersStep: HasAnyGovBodyMembersStep
    val saveGovBodyMemberStep: SaveGovBodyMemberStep
    val setStateForGovBodyMemberEditStep: SetStateForGovBodyMemberEditStep
    val removeGovBodyMemberStep: RemoveGovBodyMemberStep
    var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>?
    var nextGoverningBodyMemberId: Int?
    var editingGovBodyMemberId: Int?

    val editingGovBodyMember: GoverningBodyMemberDataModel?
        get() = editingGovBodyMemberId?.let { governingBodyMembersMap?.get(it) }
}

class OrgGovBodyMembersDependencies(
    // The destination the members sub-journey backs out to when at its start (the intro/first step, the who-to-provide
    // step with no members yet, or after removing the last member). It's injected because this reusable task is embedded
    // at different points (registration, standalone update, CYA change) that each back out to a different step, and don't
    // necessarily use back behaviour - e.g. routing to it from an action.
    val govBodyMembersIntroBackDestination: () -> Destination,
)
