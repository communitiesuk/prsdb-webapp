package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class OrgCharityNumberFormPageLandlordRegistration(
    page: Page,
    routeSegment: String,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/$routeSegment") {
    val heading: Locator = page.locator("h1")
    val hint: Locator = page.locator(".govuk-hint")
    val bodyHeading: Locator = page.locator("h2.govuk-heading-s")
    val bodyLink: Locator = page.locator("h2.govuk-heading-s + p a.govuk-link")
    val form = CharityNumberForm(page)

    fun submitCharityNumber(charityNumber: String) {
        form.charityNumberInput.fill(charityNumber)
        form.submit()
    }

    class CharityNumberForm(
        page: Page,
    ) : PostForm(page) {
        val charityNumberInput = TextInput.textByFieldName(locator, "charityNumber")
    }
}
