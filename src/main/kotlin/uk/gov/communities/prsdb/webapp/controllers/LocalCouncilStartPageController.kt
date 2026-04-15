package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL

@PrsdbController
@RequestMapping("/$LOCAL_COUNCIL_PATH_SEGMENT/$START_PAGE_PATH_SEGMENT")
class LocalCouncilStartPageController {
    @GetMapping
    fun getStartPage(model: Model): String {
        model.addAttribute("localCouncilDashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)
        return "localCouncilStartPage"
    }

    companion object {
        const val LOCAL_COUNCIL_START_PAGE_ROUTE = "/$LOCAL_COUNCIL_PATH_SEGMENT/$START_PAGE_PATH_SEGMENT"
    }
}
