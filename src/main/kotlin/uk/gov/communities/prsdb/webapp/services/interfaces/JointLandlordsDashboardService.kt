package uk.gov.communities.prsdb.webapp.services.interfaces

import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS

interface JointLandlordsDashboardService {
    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-dashboard-flag-on")
    fun addJointLandlordsDashboardAttributes(model: Model)
}
