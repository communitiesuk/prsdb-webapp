package uk.gov.communities.prsdb.webapp.models.requestModels.searchModels

import kotlin.reflect.full.memberProperties

abstract class SearchRequestModel {
    var searchTerm: String? = null

    var showFilter: Boolean = true

    fun getFilterPropertyNameValuePairs() =
        this::class
            .memberProperties
            .filterNot { it.name in listOf("searchTerm", "showFilter") }
            .map { Pair(it.name, it.getter.call(this)) }
}
