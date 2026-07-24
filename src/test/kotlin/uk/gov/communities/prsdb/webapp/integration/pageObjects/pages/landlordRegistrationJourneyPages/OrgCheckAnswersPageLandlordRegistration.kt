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

    val governingBodyMemberCard = SummaryCard(page, "1. Director")

    val mainContactCard = SummaryCard(page, "Main contact")

    val landlordDetails = LandlordDetailsSummaryList(page)

    class LandlordDetailsSummaryList(
        page: Page,
    ) : SummaryList(page, index = 1) {
        val landlordTypeRow = getRow("Landlord type")
        val organisationNameRow = getRow("Organisation name")
        val organisationAddressRow = getRow("Organisation address")
        val organisationEmailRow = getRow("Organisation email")
        val organisationPhoneRow = getRow("Organisation phone number")
        val organisationTypeRow = getRow("Organisation type")
        val registeredCharityRow = getRow("Registered charity")
        val charityCommissionRow = getRow("Charity commission")
        val charityNumberRow = getRow("Charity number")
        val registeredWithCompaniesHouseRow = getRow("Registered with Companies House")
        val companiesHouseNumberRow = getRow("Companies House number")
    }
}
