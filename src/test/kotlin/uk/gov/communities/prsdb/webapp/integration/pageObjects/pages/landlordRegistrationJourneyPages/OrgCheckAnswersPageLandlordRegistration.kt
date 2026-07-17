package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgLandlordRegistrationCyaStep

class OrgCheckAnswersPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgLandlordRegistrationCyaStep.ROUTE_SEGMENT}",
    ) {
    fun confirmAndSubmit() = form.submit()

    val form = PostForm(page)

    val sectionHeader = SectionHeader(page.locator("main"))

    val yourDetailsCard = SummaryCard(page, "Your details")

    val leadTrusteeCard = SummaryCard(page, "Lead trustee")

    val mainContactCard = SummaryCard(page, "Main contact")

    // The flat "Landlord details" summary list is the second summary list on the page
    // (after the "Your details" card's list).
    val landlordDetails = LandlordDetailsSummaryList(page)

    class LandlordDetailsSummaryList(
        page: Page,
    ) : SummaryList(page, index = 1) {
        val landlordTypeRow = getRow("Landlord type")
        val organisationNameRow = getRow("Organisation name")
        val organisationTypeRow = getRow("Organisation type")
    }
}
