package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

// TODO PDJB-114: Implement CheckJointLandlords page object
class CheckJointLandlordsFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.CheckJointLandlords.urlPathSegment}",
    ) {
    val form = CheckJointLandlordsForm(page)
    val summaryList = CheckJointLandlordsSummaryList(page)

    class CheckJointLandlordsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val addAnotherButton = SecondaryButton(locator)

        class SecondaryButton(
            parentLocator: Locator,
        ) : Button(parentLocator.locator("css=.govuk-button--secondary"))
    }

    class CheckJointLandlordsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val firstRow = getRow(0)
    }
}
