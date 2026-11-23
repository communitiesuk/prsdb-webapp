package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)

    val reactivationDetailsHeading
        get() = page.locator("h2:has-text(\"Reactivation details\")")

    val jointLandlordsHeading
        get() = page.locator("h3:has-text(\"Joint landlords\")")

    val landlordAccountHeading
        get() = page.locator("h3:has-text(\"Your landlord account\")")

    val landlordAccountText
        get() = page.locator("p:has-text(\"landlord account is now active\")")
}
