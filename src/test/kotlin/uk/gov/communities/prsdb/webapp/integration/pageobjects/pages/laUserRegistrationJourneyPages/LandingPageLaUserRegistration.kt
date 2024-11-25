package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getHeading
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getSubmitButton
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    private val beginButton = getSubmitButton(page)

    override fun validate() {
        assertThat(getHeading(page)).containsText("Registering as a local authority user")
    }

    fun clickBeginAndAssertNextPage(): NameFormPageLaUserRegistration = clickElementAndAssertNextPage(beginButton)
}
