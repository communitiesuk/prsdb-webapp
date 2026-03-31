package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep

class GasCertExpiredFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${GasCertExpiredStep.ROUTE_SEGMENT}") {
    val mainHeading = Heading(page.locator("h1"))
    val sectionHeading = Heading(page.locator("section").locator("h2"))
    val changeIssueDateLink = Link.byText(page.locator("html"), "Change the issue date")
    val form = Form(page)

    // Warning component only present in the occupied variant
    val warning = Warning.default(page)

    val submitButton = Button.default(page.locator("form"))
}
