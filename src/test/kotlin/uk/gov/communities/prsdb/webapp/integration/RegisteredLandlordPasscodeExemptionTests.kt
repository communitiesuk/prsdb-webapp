package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

@ActiveProfiles("require-passcode")
class RegisteredLandlordPasscodeExemptionTests :
    IntegrationTestWithMutableData("data-mockuser-landlord-with-no-properties.sql") {
    @Test
    fun `Registered landlords can access restricted landlord pages outside registration without a passcode`(page: Page) {
        navigator.navigateToLandlordDashboard()
        assertPageIs(page, LandlordDashboardPage::class)
    }
}
