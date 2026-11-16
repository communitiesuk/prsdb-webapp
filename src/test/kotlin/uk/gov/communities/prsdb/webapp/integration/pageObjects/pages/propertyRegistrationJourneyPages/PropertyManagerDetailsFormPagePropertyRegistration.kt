package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep

class PropertyManagerDetailsFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyManagerDetailsStep.ROUTE_SEGMENT}") {
    val form = PropertyManagerDetailsForm(page)

    fun submitDetails(
        name: String,
        email: String,
    ) {
        form.nameInput.fill(name)
        form.emailInput.fill(email)
        form.submit()
    }

    class PropertyManagerDetailsForm(
        page: Page,
    ) : PostForm(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
    }
}
