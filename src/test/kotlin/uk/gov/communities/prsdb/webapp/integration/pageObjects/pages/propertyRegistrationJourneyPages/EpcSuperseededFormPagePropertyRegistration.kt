package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep

class EpcSuperseededFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcSuperseededStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = EpcSuperseededForm(page)
    val sectionHeader = SectionHeader(page.locator("html"))

    fun submitContinueWithLatest() {
        form.submit()
    }

    fun clickSearchAgain() {
        page.locator(".govuk-button-group a.govuk-link").click()
    }

    class EpcSuperseededForm(
        page: Page,
    ) : PostForm(page)
}
