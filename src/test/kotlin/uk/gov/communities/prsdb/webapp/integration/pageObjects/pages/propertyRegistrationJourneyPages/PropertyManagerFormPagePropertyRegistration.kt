package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep

// TODO PDJB-2009: replace this placeholder with the real property manager pages
class PropertyManagerFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyManagerStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)

    fun submit() = form.submit()
}
