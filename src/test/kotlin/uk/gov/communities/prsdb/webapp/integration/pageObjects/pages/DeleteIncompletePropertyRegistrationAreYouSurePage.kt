package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class DeleteIncompletePropertyRegistrationAreYouSurePage(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT" +
            "?$CONTEXT_ID_URL_PARAMETER=${urlArguments["contextId"]}",
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
}
