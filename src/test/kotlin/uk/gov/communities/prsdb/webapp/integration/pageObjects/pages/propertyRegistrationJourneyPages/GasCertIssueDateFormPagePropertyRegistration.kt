package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep

class GasCertIssueDateFormPagePropertyRegistration(
    page: Page,
) : DateFormPage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${GasCertIssueDateStep.ROUTE_SEGMENT}") {
    val renewingHeading = Heading(page.locator("main h2.govuk-heading-m"))
    val expiryDetailsSummary = page.locator(".govuk-details__summary-text")
}
