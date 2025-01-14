package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.models.wrapperModels.SearchWrapperModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

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
        @RequestParam(value = "page", required = false) page: Int = 1,
    ): String {
        if (!query.isNullOrBlank()) {
            val pagedLandlordList = landlordService.searchForLandlords(query, currentPageNumber = page - 1)

            if (pagedLandlordList.totalPages < page) {
                return "redirect:/search/landlord?query=$query"
            }

            model.addAttribute("searchResults", pagedLandlordList)
            model.addAttribute("totalPages", pagedLandlordList.totalPages)
            model.addAttribute("currentPage", page)
        }

        model.addAttribute("searchWrapperModel", SearchWrapperModel())
        // TODO PRSD-656: add LA view of landlord details page base URL to model
        model.addAttribute("baseLandlordDetailsURL", "/landlord-details")

        return if (query?.isBlank() == true) "redirect:landlord" else "searchLandlord"
    }
}
