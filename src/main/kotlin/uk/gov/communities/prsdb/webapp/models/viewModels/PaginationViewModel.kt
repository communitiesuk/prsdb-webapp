package uk.gov.communities.prsdb.webapp.models.viewModels

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder

data class PaginationViewModel(
    val currentPage: Int,
    val totalPages: Int,
    private val httpServletRequest: HttpServletRequest,
) {
    fun getPageLink(pageNumber: Int) =
        URIQueryBuilder
            .fromHTTPServletRequest(httpServletRequest)
            .updateParam("page", pageNumber)
            .build()
            .toUriString()

    fun getPreviousPageLink() = getPageLink(currentPage - 1)

    fun getNextPageLink() = getPageLink(currentPage + 1)
}
