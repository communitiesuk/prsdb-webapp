package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.LandlordSearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LandlordSearchResultViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.searchModels.LandlordFilterPanelViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@Controller
@RequestMapping("/search")
@PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
class SearchRegisterController(
    private val landlordService: LandlordService,
) {
    @GetMapping("/landlord")
    fun searchForLandlords(
        model: Model,
        principal: Principal,
        httpServletRequest: HttpServletRequest,
        searchRequest: LandlordSearchRequestModel,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
    ): String {
        if (searchRequest.searchTerm?.isBlank() == true) {
            return "redirect:landlord"
        }

        val pagedLandlordList =
            if (searchRequest.searchTerm != null) {
                landlordService.searchForLandlords(
                    searchRequest.searchTerm!!,
                    principal.name,
                    searchRequest.restrictToLA,
                    currentPageNumber = page - 1,
                )
            } else {
                null
            }

        if (isPageOutOfBounds(pagedLandlordList, page)) {
            return "redirect:${
                URIQueryBuilder.fromHTTPServletRequest(httpServletRequest).removeParam("page").build().toUriString()
            }"
        }

        model.addAttribute("searchResults", pagedLandlordList?.content)
        pagedLandlordList?.totalPages?.let {
            model.addAttribute("paginationViewModel", PaginationViewModel(page, pagedLandlordList.totalPages, httpServletRequest))
        }

        model.addAttribute("searchRequest", searchRequest)
        model.addAttribute("filterPanelViewModel", LandlordFilterPanelViewModel(searchRequest, httpServletRequest))

        model.addAttribute("baseLandlordDetailsURL", "/landlord-details")
        // TODO PRSD-659: add LA property search base URL to model
        model.addAttribute("propertySearchURL", "property")
        // TODO PRSD-647: Set backURL to LA landing page
        model.addAttribute("backURL", "")

        return "searchLandlord"
    }

    private fun isPageOutOfBounds(
        pagedList: Page<LandlordSearchResultViewModel>?,
        page: Int,
    ) = pagedList != null && pagedList.totalPages != 0 && pagedList.totalPages < page

    // TODO PRSD-659: implement property search endpoint
    @GetMapping("/property")
    fun searchForProperties() = "error/404"
}
