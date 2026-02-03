package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class FeedbackPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$FEEDBACK_PATH_SEGMENT",
    ) {
    val surveyNowLink = Link.byText(page, "Start survey")
    val skipSurveyButton = Button.byText(page, "Continue to complete compliance")
    val surveyLaterButton = Button.byText(page, "Complete survey later")
}
