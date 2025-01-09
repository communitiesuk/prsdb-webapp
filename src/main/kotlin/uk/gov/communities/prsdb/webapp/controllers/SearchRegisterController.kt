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
    ): String {
        if (!query.isNullOrBlank()) {
            val results = landlordService.searchForLandlords(query)
            model.addAttribute("searchResults", results)
        }

        model.addAttribute("searchWrapperModel", SearchWrapperModel())
        // TODO: add landlord details page base URL to model
        model.addAttribute("baseLandlordDetailsURL", "")

        return if (query?.isBlank() == true) "redirect:landlord" else "searchLandlord"
    }
}
