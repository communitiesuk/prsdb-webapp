package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class DeleteIncompletePropertyRegistrationAreYouSurePage(
    page: Page,
    urlArguments: Map<String, String>,
) : AreYouSureFormBasePage(
        page,
        "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT" +
            "?$CONTEXT_ID_URL_PARAMETER=${urlArguments["contextId"]}",
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
}
