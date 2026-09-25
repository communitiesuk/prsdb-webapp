package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail.UpdateCorrespondenceEmailCyaStep

class CorrespondenceEmailCyaPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateCorrespondenceEmailController.getUpdateCorrespondenceEmailRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateCorrespondenceEmailCyaStep.ROUTE_SEGMENT}",
    ) {
    val form = Form(page)
    val heading = page.locator("h1")
    val summaryHeading = page.locator("#summary-name")
    val summaryList = CorrespondenceEmailSummaryList(page)
    val backLink = BackLink.default(page)

    fun confirm() = form.submit()

    class CorrespondenceEmailSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val emailRow = getRow("Email address")
    }
}
