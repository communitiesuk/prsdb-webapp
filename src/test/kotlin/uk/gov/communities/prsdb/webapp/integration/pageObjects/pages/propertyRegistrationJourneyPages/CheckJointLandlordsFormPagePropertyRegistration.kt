package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SecondaryButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep

class CheckJointLandlordsFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckJointLandlordsStep.ROUTE_SEGMENT}",
    ) {
    val title = Heading(page.locator("h1"))

    val form = CheckJointLandlordsForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    val summaryList = CheckJointLandlordsSummaryList(page)

    class CheckJointLandlordsForm(
        page: Page,
    ) : PostForm(page) {
        val addAnotherButton = SecondaryButton(locator)
    }

    class CheckJointLandlordsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val firstRow = getRow(0)

        fun getRowByIndex(number: Int) = getRow(number)
    }
}
