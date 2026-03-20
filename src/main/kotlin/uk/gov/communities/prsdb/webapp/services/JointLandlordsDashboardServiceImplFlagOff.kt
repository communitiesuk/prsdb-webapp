package uk.gov.communities.prsdb.webapp.services
import org.springframework.context.annotation.Primary
import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.services.interfaces.JointLandlordsDashboardService

@Primary
@PrsdbWebService("joint-landlords-dashboard-flag-off")
class JointLandlordsDashboardServiceImplFlagOff : JointLandlordsDashboardService {
    override fun addJointLandlordsDashboardAttributes(model: Model) {
        // No-op: joint landlords section is not shown when the feature is disabled
    }
}
