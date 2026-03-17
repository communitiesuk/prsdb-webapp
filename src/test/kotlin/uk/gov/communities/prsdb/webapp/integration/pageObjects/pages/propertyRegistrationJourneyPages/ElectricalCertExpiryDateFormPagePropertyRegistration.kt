package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep

// TODO PDJB-649: Implement Electrical Cert Issue Date page object
class ElectricalCertExpiryDateFormPagePropertyRegistration(
    page: Page,
) : DateFormPage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ElectricalCertExpiryDateStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
}
