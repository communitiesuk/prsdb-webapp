package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

// Owns the collected freeholders. Held by whichever outer task or journey embeds the reusable
// FreeholdersMembersTask, and read/written by that task via its injected dependencies. This lets the
// freeholders list live in the outer task so an internal routing step there can inspect it.
interface FreeholdersListState {
    var freeholdersMap: Map<Int, FreeholderDataModel>?
    var nextFreeholderId: Int?
    var editingFreeholderId: Int?

    val editingFreeholder: FreeholderDataModel?
        get() = editingFreeholderId?.let { freeholdersMap?.get(it) }
}
