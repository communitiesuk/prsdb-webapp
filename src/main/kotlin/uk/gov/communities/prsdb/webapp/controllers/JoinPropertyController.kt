package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.JOIN_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL

@PreAuthorize("hasAnyRole('LANDLORD')")
@PrsdbController
@RequestMapping(JOIN_PROPERTY_ROUTE)
class JoinPropertyController {
    @GetMapping("/$START_PAGE_PATH_SEGMENT")
    fun getStartPage(model: Model): String {
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)
        // TODO: PDJB-274 - Update continueUrl when next step is implemented
        model.addAttribute("continueUrl", "#")

        return "joinPropertyStartPage"
    }

    companion object {
        const val JOIN_PROPERTY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$JOIN_PROPERTY_PATH_SEGMENT"
        const val JOIN_PROPERTY_START_PAGE_ROUTE = "$JOIN_PROPERTY_ROUTE/$START_PAGE_PATH_SEGMENT"
    }
}
