package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep

class OrgMainContactFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgMainContactStep.ROUTE_SEGMENT}") {
    val form = OrgMainContactForm(page)

    fun submit(
        name: String,
        email: String,
        phoneNumber: String,
    ) {
        form.nameInput.fill(name)
        form.emailInput.fill(email)
        form.phoneNumberInput.fill(phoneNumber)
        form.submit()
    }

    class OrgMainContactForm(
        page: Page,
    ) : PostForm(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
        val phoneNumberInput = TextInput.textByFieldName(locator, "phoneNumber")
    }
}
