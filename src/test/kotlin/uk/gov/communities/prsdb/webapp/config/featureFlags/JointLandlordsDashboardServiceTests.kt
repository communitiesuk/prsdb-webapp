package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.ConcurrentModel
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.JointLandlordsDashboardService

class JointLandlordsDashboardServiceTests : FeatureFlagTest() {
    @Autowired
    lateinit var service: JointLandlordsDashboardService

    @Test
    fun `when feature is disabled addJointLandlordsDashboardAttributes does not add joinPropertyUrl to the model`() {
        featureFlagManager.disableFeature(JOINT_LANDLORDS)
        val model = ConcurrentModel()

        service.addJointLandlordsDashboardAttributes(model)

        assertFalse(model.containsAttribute("joinPropertyUrl"))
    }

    @Test
    fun `when feature is enabled addJointLandlordsDashboardAttributes adds joinPropertyUrl to the model`() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        val model = ConcurrentModel()

        service.addJointLandlordsDashboardAttributes(model)

        assertTrue(model.containsAttribute("joinPropertyUrl"))
        assertEquals(JOIN_PROPERTY_ROUTE, model.getAttribute("joinPropertyUrl"))
    }
}
