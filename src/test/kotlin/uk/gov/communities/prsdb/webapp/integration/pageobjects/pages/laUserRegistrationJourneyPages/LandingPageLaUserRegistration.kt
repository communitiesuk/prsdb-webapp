package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getHeading
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getSubmitButton
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page, "Register as a Local Authority user") {
    val heading = getHeading(page)
    private val beginButton = getSubmitButton(page)

    fun clickBeginAndAssertNextPage(): NameFormPageLaUserRegistration = clickElementAndAssertNextPage(beginButton)
}
