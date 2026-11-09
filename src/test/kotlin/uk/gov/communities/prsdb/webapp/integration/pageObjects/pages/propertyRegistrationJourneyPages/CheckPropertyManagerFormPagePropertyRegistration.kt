package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep

class CheckPropertyManagerFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckPropertyManagerStep.ROUTE_SEGMENT}") {
    val form = Form(page)
    val propertyManagerCard = PropertyManagerSummaryCard(page)

    fun clickChangeAndWait() = propertyManagerCard.getAction("Change").link.clickAndWait()

    fun clickRemoveAndWait() = propertyManagerCard.getAction("Remove").link.clickAndWait()

    class PropertyManagerSummaryCard(
        page: Page,
    ) : SummaryCard(page) {
        override val summaryList = PropertyManagerSummaryList(locator)

        class PropertyManagerSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val emailAddressRow = getRow("Email address")
        }
    }
}
