package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep

class GasCertIssueDateFormPagePropertyRegistration(
    page: Page,
) : DateFormPage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${GasCertIssueDateStep.ROUTE_SEGMENT}") {
    // TODO PDJB-1926: Use a fixed expectedTitleHeading when GAS_CERTIFICATE_14_MONTH_EXPIRY is permanently enabled.
    override val expectedTitleHeading: String?
        get() =
            when (page.locator("h1").innerText()) {
                "When was this certificate issued?" -> "When was the gas safety certificate issued?"
                else -> null
            }

    val renewingHeading = Heading(page.locator("main h2.govuk-heading-m"))
    val expiryDetailsSummary = page.locator(".govuk-details__summary-text")
}
