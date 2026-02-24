package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

class CheckAnswersPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${AbstractCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    fun confirmAndSubmit() = form.submit()

    val form = PostForm(page)

    val sectionHeader = SectionHeader(page.locator("html"))

    val summaryList = CheckAnswersLandlordRegistrationSummaryList(page)

    class CheckAnswersLandlordRegistrationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val emailRow = getRow("Email address")
    }
}
