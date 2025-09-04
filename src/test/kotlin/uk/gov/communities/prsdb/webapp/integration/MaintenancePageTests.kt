package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.MaintenancePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

abstract class MaintenancePageTests : IntegrationTestWithImmutableData("data-local.sql")

// The ActiveProfiles annotation is not picked up for inner classes, so we need to create separate classes with and without the maintenance-mode profile
@ActiveProfiles("maintenance-mode")
class MaintenanceModeTests : MaintenancePageTests() {
    @Test
    fun `User is redirected to maintenance page when accessing the landlord dashboard`(page: Page) {
        navigator.navigate(LandlordController.LANDLORD_DASHBOARD_URL)
        assertPageIs(page, MaintenancePage::class)
    }

    @Test
    fun `User is redirected to maintenance page when accessing the local council dashboard`(page: Page) {
        navigator.navigate(LocalAuthorityDashboardController.LOCAL_AUTHORITY_DASHBOARD_URL)
        assertPageIs(page, MaintenancePage::class)
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
