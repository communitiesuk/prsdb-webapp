package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        "$LANDLORD_PATH_SEGMENT/$DEREGISTER_PROPERTY_JOURNEY_URL/${urlArguments["propertyOwnershipId"]}/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val goToDashboardButton = Button.byText(page, "Go to Dashboard")
}
