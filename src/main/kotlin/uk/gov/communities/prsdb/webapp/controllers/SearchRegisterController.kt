package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SEARCH_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController.Companion.SEARCH_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.URIQueryBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.LandlordSearchRequestModel
import uk.gov.communities.prsdb.webapp.models.requestModels.searchModels.PropertySearchRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels.LandlordFilterPanelViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.filterPanelModels.PropertyFilterPanelViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(SEARCH_ROUTE)
@PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
class SearchRegisterController(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/$LANDLORD_PATH_SEGMENT")
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

        model.addAttribute("searchRequest", searchRequest)
        model.addAttribute("filterPanelViewModel", LandlordFilterPanelViewModel(searchRequest, httpServletRequest))
        model.addAttribute("backURL", LOCAL_AUTHORITY_DASHBOARD_URL)

        if (searchRequest.searchTerm == null) {
            return "searchLandlord"
        }

        val pagedLandlordList =
            landlordService.searchForLandlords(
                searchRequest.searchTerm!!,
                principal.name,
                searchRequest.restrictToLA ?: false,
                requestedPageIndex = page - 1,
            )

        if (isPageOutOfBounds(pagedLandlordList, page)) {
            return getRedirectForPageOutOfBounds(httpServletRequest)
        }

        model.addAttribute("searchResults", pagedLandlordList.content)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedLandlordList.totalPages, httpServletRequest),
        )
        model.addAttribute("propertySearchURL", SEARCH_PROPERTY_URL)

        return "searchLandlord"
    }

    @GetMapping("/$PROPERTY_PATH_SEGMENT")
    fun searchForProperties(
        model: Model,
        principal: Principal,
        httpServletRequest: HttpServletRequest,
        searchRequest: PropertySearchRequestModel,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
    ): String {
        if (searchRequest.searchTerm?.isBlank() == true) {
            return "redirect:property"
        }

        model.addAttribute("searchRequest", searchRequest)
        model.addAttribute("filterPanelViewModel", PropertyFilterPanelViewModel(searchRequest, httpServletRequest))
        model.addAttribute("backURL", LOCAL_AUTHORITY_DASHBOARD_URL)

        if (searchRequest.searchTerm == null) {
            return "searchProperty"
        }

        val pagedSearchResults =
            propertyOwnershipService.searchForProperties(
                searchRequest.searchTerm!!,
                principal.name,
                searchRequest.restrictToLA ?: false,
                searchRequest.restrictToLicenses ?: LicensingType.entries,
                requestedPageIndex = page - 1,
            )

        if (isPageOutOfBounds(pagedSearchResults, page)) {
            return getRedirectForPageOutOfBounds(httpServletRequest)
        }

        model.addAttribute("searchResults", pagedSearchResults.content)
        model.addAttribute(
            "paginationViewModel",
            PaginationViewModel(page, pagedSearchResults.totalPages, httpServletRequest),
        )
        model.addAttribute("landlordSearchURL", "landlord")

        return "searchProperty"
    }

    private fun isPageOutOfBounds(
        pagedList: Page<out Any>,
        page: Int,
    ) = pagedList.totalPages != 0 && pagedList.totalPages < page

    private fun getRedirectForPageOutOfBounds(httpServletRequest: HttpServletRequest) =
        "redirect:${
            URIQueryBuilder.fromHTTPServletRequest(httpServletRequest).removeParam("page").build().toUriString()
        }"

    companion object {
        const val SEARCH_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$SEARCH_PATH_SEGMENT"
        const val SEARCH_LANDLORD_URL = "$SEARCH_ROUTE/$LANDLORD_PATH_SEGMENT"
        const val SEARCH_PROPERTY_URL = "$SEARCH_ROUTE/$PROPERTY_PATH_SEGMENT"
    }
}
