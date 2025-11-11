package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.CheckAnswers.urlPathSegment}") {
    fun confirm() = form.submit()

    val form = PostForm(page)

    val sectionHeader = SectionHeader(page.locator("html"))

    val heading = Heading(page.locator("h1"))

    val summaryList = CheckAnswersPropertyRegistrationSummaryList(page)

    class CheckAnswersPropertyRegistrationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val ownershipRow = getRow("Ownership type")
        val licensingRow = getRow("Licensing type")
    }
}
