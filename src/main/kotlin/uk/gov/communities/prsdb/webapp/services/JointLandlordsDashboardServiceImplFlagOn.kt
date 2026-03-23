package uk.gov.communities.prsdb.webapp.services

import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.services.interfaces.JointLandlordsDashboardService

@PrsdbWebService("joint-landlords-dashboard-flag-on")
class JointLandlordsDashboardServiceImplFlagOn : JointLandlordsDashboardService {
    override fun addJointLandlordsDashboardAttributes(model: Model) {
        model.addAttribute("joinPropertyUrl", JOIN_PROPERTY_ROUTE)
    }
}
