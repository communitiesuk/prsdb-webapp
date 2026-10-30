package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

// Owns the collected superior landlords. Held by whichever outer task or journey embeds the reusable
// SuperiorLandlordsMembersTask, and read/written by that task via its injected dependencies. This lets the
// superior landlords list live in the outer task so an internal routing step there can inspect it.
interface SuperiorLandlordsListState {
    var superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>?
    var nextSuperiorLandlordId: Int?
    var editingSuperiorLandlordId: Int?

    val editingSuperiorLandlord: SuperiorLandlordDataModel?
        get() = editingSuperiorLandlordId?.let { superiorLandlordsMap?.get(it) }
}
