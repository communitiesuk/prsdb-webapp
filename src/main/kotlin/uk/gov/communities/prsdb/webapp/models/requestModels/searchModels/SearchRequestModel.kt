package uk.gov.communities.prsdb.webapp.models.requestModels.searchModels

abstract class SearchRequestModel {
    var query: String? = null

    var showFilter: Boolean = true
}
