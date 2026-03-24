package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Primary
import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE

interface JointLandlordsDashboardService {
    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-dashboard-flag-on")
    fun addJointLandlordsDashboardAttributes(model: Model)
}

@Primary
@PrsdbWebService("joint-landlords-dashboard-flag-off")
class JointLandlordsDashboardServiceImplFlagOff : JointLandlordsDashboardService {
    override fun addJointLandlordsDashboardAttributes(model: Model) {}
}

@PrsdbWebService("joint-landlords-dashboard-flag-on")
class JointLandlordsDashboardServiceImplFlagOn : JointLandlordsDashboardService {
    override fun addJointLandlordsDashboardAttributes(model: Model) {
        model.addAttribute("joinPropertyUrl", JOIN_PROPERTY_ROUTE)
    }
}
