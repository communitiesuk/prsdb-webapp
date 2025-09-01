package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController

abstract class MaintenancePageTests : IntegrationTestWithImmutableData("data-local.sql")

// The ActiveProfiles annotation is not picked up for inner classes, so we need to create separate classes for each profile
@ActiveProfiles("maintenance-mode")
class MaintenanceModeTests : MaintenancePageTests() {
    @Test
    fun `User is redirected to maintenance page when accessing the landlord dashboard`() {
        navigator.goToPathButExpectRedirectToMaintenancePage(LandlordController.LANDLORD_DASHBOARD_URL)
    }

    @Test
    fun `User is redirected to maintenance page when accessing the local council dashboard`() {
        navigator.goToPathButExpectRedirectToMaintenancePage(LocalAuthorityDashboardController.LOCAL_AUTHORITY_DASHBOARD_URL)
    }
}

class NotMaintenanceModeTests : MaintenancePageTests() {
    @Test
    fun `User can reach the landlord dashboard`() {
        navigator.goToLandlordDashboard()
    }

    @Test
    fun `User can reach the local council dashboard`() {
        navigator.goToLocalAuthorityDashboard()
    }
}
