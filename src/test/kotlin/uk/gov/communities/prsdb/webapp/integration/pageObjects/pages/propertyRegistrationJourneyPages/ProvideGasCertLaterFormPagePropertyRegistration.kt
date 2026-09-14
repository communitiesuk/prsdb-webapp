package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep

class ProvideGasCertLaterFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ProvideGasCertLaterStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val subheading = Heading(page.locator("main section h2"))
    val paragraphs = page.locator("main section p.govuk-body")
    val gasSafetyLink = Link.byText(page, "gas safety for landlords (opens in new tab)")
    val form = PostForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    // This will only be populated for the occupied variant of this page
    val insetText = InsetText(page)
}
