package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.CheckAnswers.urlPathSegment}") {
    fun confirm() = form.submit()

    val form = CheckAnswersPropertyRegistration(page)

    class CheckAnswersPropertyRegistration(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val summaryList = CheckAnswersPropertyRegistrationSummaryList(locator)
    }

    class CheckAnswersPropertyRegistrationSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val ownershipRow = getRow("Ownership type")
        val licensingRow = getRow("Licensing type")
    }
}
