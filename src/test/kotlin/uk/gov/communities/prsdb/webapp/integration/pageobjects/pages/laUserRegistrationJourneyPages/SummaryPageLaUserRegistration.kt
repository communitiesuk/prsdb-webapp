package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getHeading
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class SummaryPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertThat(getHeading(page)).containsText("Check your answers")
    }
}
