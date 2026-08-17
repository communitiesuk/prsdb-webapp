package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

// Owns the collected governing body members. Held by whichever outer task or journey embeds the
// reusable OrgGovBodyMembersTask, and read/written by that task via its injected dependencies. This
// lets the members list live in the outer task so an internal routing step there can inspect it.
interface GovBodyMembersListState {
    var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>?
    var nextGoverningBodyMemberId: Int?
    var editingGovBodyMemberId: Int?

    val editingGovBodyMember: GoverningBodyMemberDataModel?
        get() = editingGovBodyMemberId?.let { governingBodyMembersMap?.get(it) }
}
