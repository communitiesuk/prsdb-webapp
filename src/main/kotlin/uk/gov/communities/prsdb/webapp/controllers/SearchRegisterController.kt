package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Min
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.models.viewModels.PaginationViewModel
import uk.gov.communities.prsdb.webapp.models.wrapperModels.SearchWrapperModel
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
        @RequestParam(required = false) query: String?,
        @RequestParam(value = "page", required = false) @Min(1) page: Int = 1,
        principal: Principal,
        httpServletRequest: HttpServletRequest,
    ): String {
        var totalPages = 0

        if (!query.isNullOrBlank()) {
            val pagedLandlordList =
                landlordService.searchForLandlords(query, principal.name, currentPageNumber = page - 1)

            if (pagedLandlordList.totalPages < page) {
                return "redirect:/search/landlord?query=$query"
            }

            totalPages = pagedLandlordList.totalPages

            model.addAttribute("searchResults", pagedLandlordList.content)
        }

        model.addAttribute("paginationViewModel", PaginationViewModel(page, totalPages, httpServletRequest))

        model.addAttribute("searchWrapperModel", SearchWrapperModel())
        model.addAttribute("baseLandlordDetailsURL", "/landlord-details")
        // TODO PRSD-659: add LA property search base URL to model
        model.addAttribute("propertySearchURL", "property")
        // TODO PRSD-647: Set backURL to LA landing page
        model.addAttribute("backURL", "")

        return if (query?.isBlank() == true) "redirect:landlord" else "searchLandlord"
    }

    // TODO PRSD-659: implement property search endpoint
    @GetMapping("/property")
    fun searchForProperties() = "error/404"
}
