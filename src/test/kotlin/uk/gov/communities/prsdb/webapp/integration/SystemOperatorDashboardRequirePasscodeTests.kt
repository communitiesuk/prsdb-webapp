package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.GeneratePasscodePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test

@ActiveProfiles("require-passcode")
class SystemOperatorDashboardRequirePasscodeTests : IntegrationTestWithMutableData("data-local.sql") {
    @Test
    fun `the generate passcode button links to the generate passcode page`(page: Page) {
        val dashboard = navigator.goToSystemOperatorDashboard()
        dashboard.generatePasscodeButton.clickAndWait()
        assertPageIs(page, GeneratePasscodePage::class)
    }
}
