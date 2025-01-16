package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
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
            if (page < 1) {
                return "redirect:/search/landlord?query=$query"
            }

            val pagedLandlordList = landlordService.searchForLandlords(query, currentPageNumber = page - 1)

            if (pagedLandlordList.totalPages in 1..<page) {
                return "redirect:/search/landlord?query=$query"
            }

            val urlWithoutPage =
                UriComponentsBuilder
                    .newInstance()
                    .path("/search/landlord")
                    .queryParam("query", query)
                    .build()
                    .toUriString()

            model.addAttribute("unpagedUrl", urlWithoutPage)
            model.addAttribute("searchResults", pagedLandlordList.content)
            model.addAttribute("totalPages", pagedLandlordList.totalPages)
            model.addAttribute("currentPage", page)
        } else {
            model.addAttribute("totalPages", 0)
        }

        model.addAttribute("searchWrapperModel", SearchWrapperModel())
        // TODO PRSD-656: add LA view of landlord details page base URL to model
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
