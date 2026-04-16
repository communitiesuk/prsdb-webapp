package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SecondaryButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep

class CheckGasCertUploadsFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckGasCertUploadsStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))

    val form = CheckUploadsForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    val table = CheckUploadsTable(page)

    class CheckUploadsForm(
        page: Page,
    ) : PostForm(page) {
        val addAnotherButton = SecondaryButton(locator)
    }

    class CheckUploadsTable(
        page: Page,
    ) : Table(page)
}
